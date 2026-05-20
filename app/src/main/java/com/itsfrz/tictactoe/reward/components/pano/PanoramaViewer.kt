package com.itsfrz.tictactoe.reward.components.pano

/**
 * Panorama360Viewer.kt  ── GAME REWARD EDITION  v3
 *
 * Root cause of blank screen in v2:
 *   • "precision highp float" is optional in ES 2.0 — many Mali/Adreno GPUs silently
 *     fall back to mediump, but some reject the shader entirely.  Fixed → mediump.
 *   • `uniform int` used for toggle flags caused glUniform1i mismatches.
 *     Fixed → all toggles are `uniform float` compared via "> 0.5".
 *   • `snapshotFlow { volatileField }` doesn't trigger recomposition.
 *     Fixed → explicit poll loop with delay(16) on the main thread.
 *
 * Architecture:
 *   PanoramaViewer (Composable)
 *     ├─ AndroidView → PanoGLSurfaceView
 *     │     └─ PanoRenderer  (GLSurfaceView.Renderer, ES 2.0)
 *     │           Shader pipeline (one full-screen quad, single draw call):
 *     │             1. Equirectangular → sphere ray-cast   (proven original)
 *     │             2. Chromatic aberration  (edge-amplified, vel-boosted)
 *     │             3. Saturation lift
 *     │             4. ACES filmic tone-map
 *     │             5. Temporal film grain   (Vlachos hash, midtone-weighted)
 *     │             6. Radial vignette       (tightens with speed)
 *     │             7. Polar cap fade        (hides zenith/nadir seams)
 *     │             8. Sky/ground atmosphere (cool sky, warm ground)
 *     └─ Compose overlay
 *           • Reward particle burst  (gold dust, 40 particles, time-driven)
 *           • Cinematic FOV spring-zoom entry
 *           • Idle breathing micro-motion
 *           • Velocity-driven darkening overlay (polls GL state every 16 ms)
 *           • Ambient mode-color glow border
 *           • Animated compass rose
 *           • Mode badge, position ribbon, mode switcher
 *
 * Usage:
 *   PanoramaViewer(
 *       imageUrl    = "https://example.com/equirectangular.jpg",
 *       modifier    = Modifier.fillMaxSize(),
 *       initialMode = PanoMode.TOUCH,
 *   )
 *
 * Dependencies (add to build.gradle if not already present):
 *   implementation("io.coil-kt:coil-compose:2.6.0")
 */

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.*
import kotlin.random.Random

// ─────────────────────────────────────────────────────────────
//  Public API
// ─────────────────────────────────────────────────────────────

enum class PanoMode(val label: String, val icon: ImageVector, val hint: String) {
    TOUCH("Touch",  Icons.Rounded.PanTool,        "Drag & fling · pinch to zoom"),
    GYRO ("Gyro",   Icons.Rounded.ScreenRotation, "Tilt device to look around"),
    AUTO ("Orbit",  Icons.Rounded.Autorenew,      "Gentle auto-pan"),
}

data class PanoConfig(
    val initialFovDeg:    Float = 80f,
    val minFovDeg:        Float = 25f,
    val maxFovDeg:        Float = 120f,
    /** Lower = longer fling coast. */
    val flingFriction:    Float = 0.88f,
    val gyroAlpha:        Float = 0.90f,
    val orbitSpeedRad:    Float = 0.12f,
    val controlsHideMs:   Long  = 3_500L,
    /** Seconds of stillness before idle breathing begins. */
    val breathIdleSec:    Float = 6f,
    /** Peak breathing amplitude in radians (~0.14°). */
    val breathAmpRad:     Float = 0.0025f,
)

// ─────────────────────────────────────────────────────────────
//  Main composable
// ─────────────────────────────────────────────────────────────

@Composable
fun PanoramaViewer(
    imageUrl:     String,
    modifier:     Modifier   = Modifier,
    initialMode:  PanoMode   = PanoMode.TOUCH,
    config:       PanoConfig = PanoConfig(),
    onModeChange: ((PanoMode) -> Unit)? = null,
) {
    val context = LocalContext.current
    val haptic  = LocalHapticFeedback.current
    val scope   = rememberCoroutineScope()

    val renderState = remember { PanoRenderState(config) }

    var mode          by remember { mutableStateOf(initialMode) }
    var showControls  by remember { mutableStateOf(true) }
    var loadDone      by remember { mutableStateOf(false) }
    var lastInteract  by remember { mutableStateOf(System.currentTimeMillis()) }

    // Compose-side velocity mirror — polled from @Volatile GL field every frame.
    // This is the correct pattern: GL thread writes @Volatile, Compose polls on Main.
    var velMagState   by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            velMagState = renderState.velMag
        }
    }

    // ── Gyroscope ─────────────────────────────────────────────
    val gyroListener = remember { GyroSensor(config.gyroAlpha) }
    DisposableEffect(mode) {
        if (mode == PanoMode.GYRO) gyroListener.register(context) { dy, dp ->
            renderState.applyGyro(dy, dp)
        }
        onDispose { gyroListener.unregister(context) }
    }

    // ── Auto-orbit ────────────────────────────────────────────
    LaunchedEffect(mode) {
        while (mode == PanoMode.AUTO) {
            delay(16)
            renderState.yaw += config.orbitSpeedRad * 0.016f
        }
    }

    // ── Idle breathing ─────────────────────────────────────────
    LaunchedEffect(mode) {
        while (mode == PanoMode.TOUCH) {
            delay(16)
            val idle = (System.currentTimeMillis() - lastInteract) / 1000f
            if (idle >= config.breathIdleSec) {
                val t = (idle - config.breathIdleSec) * (2f * PI.toFloat() / 5f)
                renderState.breathYaw   = sin(t * 0.7f) * config.breathAmpRad
                renderState.breathPitch = sin(t * 1.1f) * config.breathAmpRad * 0.35f
            } else {
                renderState.breathYaw   = 0f
                renderState.breathPitch = 0f
            }
        }
    }

    // ── Entry FOV spring-zoom (cinematic reveal on load) ───────
    LaunchedEffect(loadDone) {
        if (loadDone) {
            renderState.fovDeg = config.maxFovDeg * 0.90f      // start wide
            val anim = Animatable(renderState.fovDeg)
            anim.animateTo(
                config.initialFovDeg,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            ) { renderState.fovDeg = value }
        }
    }

    // ── Controls auto-hide ─────────────────────────────────────
    var hideJob by remember { mutableStateOf<Job?>(null) }
    fun nudge() {
        lastInteract = System.currentTimeMillis()
        renderState.breathYaw = 0f; renderState.breathPitch = 0f
        showControls = true
        hideJob?.cancel()
        hideJob = scope.launch { delay(config.controlsHideMs); showControls = false }
    }
    LaunchedEffect(mode) { nudge(); onModeChange?.invoke(mode) }

    // ── Load bitmap via Coil (IO thread) ─────────────────────
    LaunchedEffect(imageUrl) {
        withContext(Dispatchers.IO) {
            try {
                val result = ImageLoader(context).execute(
                    ImageRequest.Builder(context).data(imageUrl).allowHardware(false).build()
                )
                if (result is SuccessResult) {
                    renderState.pendingBitmap = (result.drawable as BitmapDrawable).bitmap
                    withContext(Dispatchers.Main) { loadDone = true }
                }
            } catch (_: Exception) {}
        }
    }

    // ── Derived UI values ──────────────────────────────────────
    val yawDeg by remember {
        derivedStateOf { (renderState.yaw * 180f / PI.toFloat() % 360f + 360f) % 360f }
    }

    // ─────────────────────────────────────────────────────────
    //  Layout
    // ─────────────────────────────────────────────────────────
    Box(modifier = modifier.fillMaxSize()) {

        // ── GL surface ────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                PanoGLSurfaceView(ctx, renderState).apply {
                    onTouchCallback   = { nudge() }
                    onTouchModeActive = { mode == PanoMode.TOUCH }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { nudge() } },
            update = { view -> view.setMode(mode) }
        )

        // ── Ambient mode-colour glow border ───────────────────
        val glowAccent  by animateColorAsState(mode.accentColor, tween(700), label = "glow")
        val glowVisible by animateFloatAsState(if (loadDone) 1f else 0f, tween(1200), label = "glowA")
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    0f   to Color.Transparent,
                    0.72f to Color.Transparent,
                    1f   to glowAccent.copy(alpha = 0.22f * glowVisible)
                ),
                size = size
            )
            drawRect(
                color = glowAccent.copy(alpha = 0.28f * glowVisible),
                style = Stroke(1.5.dp.toPx())
            )
        }

        // ── Velocity darkening overlay ────────────────────────
        // velMagState is polled from GL every 16 ms via LaunchedEffect above
        val velOverlay by animateFloatAsState(
            (velMagState * 1.6f).coerceIn(0f, 0.32f), tween(60), label = "velOv"
        )
        if (velOverlay > 0.005f) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        0f   to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f   to Color.Black.copy(alpha = velOverlay)
                    )
                )
            )
        }

        // ── Reward particle burst (fades out after load) ──────
        RewardParticles(loadDone = loadDone)

        // ── Position ribbon ───────────────────────────────────
        AnimatedVisibility(
            visible  = loadDone,
            enter    = fadeIn(tween(800)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
        ) {
            PositionRibbon(yawDeg = yawDeg, mode = mode,
                modifier = Modifier.width(160.dp).height(3.dp))
        }

        // ── Mode badge (top-left) ─────────────────────────────
        AnimatedVisibility(
            visible  = showControls && loadDone,
            enter    = fadeIn() + slideInVertically { -32 },
            exit     = fadeOut() + slideOutVertically { -32 },
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(14.dp)
        ) { ModeBadge(mode) }

        // ── Compass + bearing (top-right) ─────────────────────
        AnimatedVisibility(
            visible  = showControls && loadDone,
            enter    = fadeIn() + slideInHorizontally { it },
            exit     = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(14.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                CompassRose(yawDeg = yawDeg, mode = mode, size = 54.dp)
                BearingReadout(yawDeg = yawDeg, fovDeg = renderState.fovDeg)
            }
        }

        // ── Mode switcher ─────────────────────────────────────
        AnimatedVisibility(
            visible  = showControls && loadDone,
            enter    = fadeIn() + slideInVertically { it / 2 },
            exit     = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 26.dp)
        ) {
            ModeSwitcher(current = mode, onSelect = { next ->
                if (next == mode) return@ModeSwitcher
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (mode == PanoMode.TOUCH) renderState.stopFling()
                mode = next
            })
        }

        // ── Loading overlay ───────────────────────────────────
        AnimatedVisibility(
            visible = !loadDone,
            exit    = fadeOut(tween(1200))
        ) { LoadingOverlay() }
    }
}

// ─────────────────────────────────────────────────────────────
//  Shared render state  (main-thread writes, GL-thread reads)
//  @Volatile guarantees visibility across threads without locking.
// ─────────────────────────────────────────────────────────────

class PanoRenderState(val config: PanoConfig) {

    @Volatile var yaw:           Float   = 0f
    @Volatile var pitch:         Float   = 0f
    @Volatile var fovDeg:        Float   = config.initialFovDeg
    @Volatile var velYaw:        Float   = 0f
    @Volatile var velPitch:      Float   = 0f
    @Volatile var velMag:        Float   = 0f   // read by Compose via poll loop
    @Volatile var elapsedSec:    Float   = 0f   // for grain animation
    @Volatile var pendingBitmap: Bitmap? = null
    @Volatile var textureReady:  Boolean = false
    @Volatile var breathYaw:     Float   = 0f
    @Volatile var breathPitch:   Float   = 0f

    private val PITCH_LIMIT = PI.toFloat() / 2.2f

    /** Called every GL frame — advances physics and breathing. */
    fun tick(dtSec: Float) {
        elapsedSec += dtSec

        // Frame-rate–independent exponential friction
        val friction = config.flingFriction.pow(dtSec * 60f)
        if (velYaw.absoluteValue > 0.0003f || velPitch.absoluteValue > 0.0003f) {
            yaw   += velYaw   * dtSec
            pitch += velPitch * dtSec
            velYaw   *= friction
            velPitch *= friction
        } else {
            velYaw = 0f; velPitch = 0f
        }

        // Apply breathing offset (set by Compose coroutine)
        yaw   += breathYaw
        pitch += breathPitch

        // Hard pitch clamp (no elastic here — keep it reliable)
        pitch = pitch.coerceIn(-PITCH_LIMIT, PITCH_LIMIT)

        velMag = sqrt(velYaw * velYaw + velPitch * velPitch)
    }

    fun applyDrag(dxPx: Float, dyPx: Float, viewWidthPx: Int) {
        val sens = fovDeg / 90f * (PI.toFloat() / viewWidthPx)
        velYaw   = -dxPx * sens;  yaw   += velYaw
        velPitch = -dyPx * sens;  pitch += velPitch
        pitch = pitch.coerceIn(-PITCH_LIMIT, PI.toFloat() / 2.2f)
    }

    fun applyPinch(scaleFactor: Float) {
        // Lerp toward target for buttery feel
        val target = (fovDeg / scaleFactor).coerceIn(config.minFovDeg, config.maxFovDeg)
        fovDeg = fovDeg + (target - fovDeg) * 0.65f
    }

    fun applyGyro(yawDelta: Float, pitchDelta: Float) {
        yaw   += yawDelta
        pitch  = (pitch + pitchDelta).coerceIn(-PITCH_LIMIT, PITCH_LIMIT)
    }

    fun stopFling() { velYaw = 0f; velPitch = 0f }
}

// ─────────────────────────────────────────────────────────────
//  GLSurfaceView wrapper — handles multi-touch in the View layer
// ─────────────────────────────────────────────────────────────

class PanoGLSurfaceView(
    context: Context,
    private val state: PanoRenderState,
) : GLSurfaceView(context) {

    var onTouchCallback:   (() -> Unit)?    = null
    var onTouchModeActive: (() -> Boolean)? = null

    private val renderer  = PanoRenderer(state)
    private var lastX     = 0f; private var lastY     = 0f
    private var ptr0Id    = -1; private var ptr1Id    = -1
    private var pinchDist0= 0f
    @Suppress("unused")
    private var mode = PanoMode.TOUCH

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.RGBA_8888)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun setMode(m: PanoMode) { mode = m }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        onTouchCallback?.invoke()
        val touchActive = onTouchModeActive?.invoke() ?: false
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                ptr0Id = e.getPointerId(0)
                lastX  = e.x; lastY = e.y
                state.stopFling()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (e.pointerCount == 2) {
                    ptr1Id     = e.getPointerId(e.actionIndex)
                    pinchDist0 = pinchDist(e)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (e.pointerCount >= 2 && ptr1Id >= 0) {
                    val d = pinchDist(e)
                    if (pinchDist0 > 0f) state.applyPinch(d / pinchDist0)
                    pinchDist0 = d
                } else if (touchActive) {
                    val idx = e.findPointerIndex(ptr0Id).coerceAtLeast(0)
                    val nx  = e.getX(idx); val ny = e.getY(idx)
                    state.applyDrag(nx - lastX, ny - lastY, width)
                    lastX = nx; lastY = ny
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { ptr0Id = -1; ptr1Id = -1 }
            MotionEvent.ACTION_POINTER_UP -> { if (e.getPointerId(e.actionIndex) == ptr1Id) ptr1Id = -1 }
        }
        return true
    }

    private fun pinchDist(e: MotionEvent): Float {
        val i0 = e.findPointerIndex(ptr0Id).coerceAtLeast(0)
        val i1 = e.findPointerIndex(ptr1Id).coerceAtLeast(1)
        val dx = e.getX(i0) - e.getX(i1); val dy = e.getY(i0) - e.getY(i1)
        return sqrt(dx * dx + dy * dy)
    }
}

// ─────────────────────────────────────────────────────────────
//  OpenGL ES 2.0 renderer
//
//  Shader design rules (hard-learned from device fragmentation):
//   • "precision mediump float" — highp is optional in ES 2.0 fragment stage
//   • No "uniform int" for toggles — use "uniform float" + compare to 0.5
//   • No dynamic loops with non-constant length on old Adreno
//   • Keep atan / asin calls to exactly one each (expensive)
//   • No discard — it breaks early-z on some drivers
// ─────────────────────────────────────────────────────────────

class PanoRenderer(private val state: PanoRenderState) : GLSurfaceView.Renderer {

    private var program  = 0
    private var texId    = 0
    private var quadBuf: FloatBuffer? = null
    private var vpW = 1; private var vpH = 1
    private var lastMs = 0L

    // Uniform locations
    private var uYaw    = 0; private var uPitch  = 0; private var uFov    = 0
    private var uAspect = 0; private var uTex    = 0; private var uTime   = 0
    private var uVelMag = 0; private var uVelYaw = 0; private var uVelPit = 0

    // ── Vertex shader: full-screen NDC quad ──────────────────
    private val VS = """
        attribute vec2 aPos;
        varying   vec2 vUV;
        void main() {
            vUV         = aPos;
            gl_Position = vec4(aPos, 0.0, 1.0);
        }
    """.trimIndent()

    // ── Fragment shader ───────────────────────────────────────
    //
    // Starts from the original proven equirectangular projection, then
    // layers cinematic effects that are safe on ES 2.0 / mediump.
    //
    // Effect order matters:
    //  project → chroma-ab → atmosphere → saturation → ACES → grain → vignette → polar
    //
    // "mediump float" is mandatory for broad device compatibility.
    // All uniforms that could be int are float to avoid glUniform mismatches.
    private val FS = """
        precision mediump float;

        uniform sampler2D uTex;
        uniform float     uYaw;
        uniform float     uPitch;
        uniform float     uFov;
        uniform float     uAspect;
        uniform float     uTime;
        uniform float     uVelMag;
        uniform float     uVelYaw;
        uniform float     uVelPit;

        varying vec2 vUV;

        #define PI   3.14159265
        #define PI2  6.28318530

        // ── ACES filmic tone-map (Narkowicz 2015) ────────────
        // Slightly brighter than Reinhard; preserves saturated hues.
        vec3 aces(vec3 x) {
            return clamp(
                (x * (2.51 * x + 0.03)) / (x * (2.43 * x + 0.59) + 0.14),
                0.0, 1.0
            );
        }

        // ── Vlachos hash (no banding, cheap, ~4 muls) ────────
        float hash21(vec2 p) {
            vec3 p3 = fract(vec3(p.xyx) * 0.1031);
            p3 += dot(p3, p3.yzx + 19.19);
            return fract((p3.x + p3.y) * p3.z);
        }

        void main() {
            // ── 1. Equirectangular sphere projection ─────────
            // Identical to proven original — this is the load-bearing step.
            float tanHalf = tan(radians(uFov) * 0.5);

            vec3 ray = normalize(vec3(
                vUV.x * tanHalf * uAspect,
                vUV.y * tanHalf,
               -1.0
            ));

            // Pitch (X-axis rotation)
            float cp = cos(uPitch), sp = sin(uPitch);
            ray = vec3(
                ray.x,
                cp * ray.y - sp * ray.z,
                sp * ray.y + cp * ray.z
            );

            // Yaw (Y-axis rotation)
            float cy = cos(uYaw), sy = sin(uYaw);
            ray = vec3(
                cy * ray.x + sy * ray.z,
                ray.y,
               -sy * ray.x + cy * ray.z
            );

            // Spherical → UV  (atan + asin called exactly once each)
            float lon = atan(ray.x, -ray.z);           // -PI..PI
            float lat = asin(clamp(ray.y, -1.0, 1.0)); // -PI/2..PI/2
            float u0  = lon / PI2 + 0.5;
            float v0  = 1.0 - (lat / PI + 0.5);

            // ── 2. Chromatic aberration ───────────────────────
            // Radially amplified so it's subtle at centre, visible at edges.
            // Velocity boosts it to signal fast movement (like a real lens).
            float edgeFactor = length(vUV) * 0.55 + uVelMag * 0.25;
            float abStr      = 0.0014 * edgeFactor;
            // Shift R and B along longitude; G stays at u0 (human eye most sensitive to G)
            float uR = atan(ray.x * (1.0 + abStr), -ray.z) / PI2 + 0.5;
            float uB = atan(ray.x * (1.0 - abStr), -ray.z) / PI2 + 0.5;
            vec4 col = vec4(
                texture2D(uTex, vec2(uR, v0)).r,
                texture2D(uTex, vec2(u0, v0)).g,
                texture2D(uTex, vec2(uB, v0)).b,
                1.0
            );

            // ── 3. Sky / Ground atmospheric grade ─────────────
            // Cool-blue zenith, warm-amber nadir — mimics real outdoor lighting.
            // Strength is subtle so it reads as "correct" rather than "filtered".
            float skyFrac = clamp( ray.y, 0.0, 1.0);
            float gndFrac = clamp(-ray.y, 0.0, 1.0);
            col.rgb = mix(col.rgb, col.rgb * vec3(0.96, 0.98, 1.04), skyFrac * 0.22);
            col.rgb = mix(col.rgb, col.rgb * vec3(1.04, 1.01, 0.96), gndFrac * 0.18);

            // ── 4. Saturation lift ────────────────────────────
            float lum = dot(col.rgb, vec3(0.2126, 0.7152, 0.0722));
            col.rgb   = mix(vec3(lum), col.rgb, 1.20);
            col.rgb   = max(col.rgb, 0.0);

            // ── 5. ACES tone-mapping ──────────────────────────
            // Expose slightly above 1 first to use the curve's shoulder.
            col.rgb = aces(col.rgb * 1.06);

            // ── 6. Film grain ─────────────────────────────────
            // Vlachos hash seeded by (UV, time) → no temporal aliasing.
            // Weight by ~inv-lum so grain is heaviest in midtones (photographic).
            float t      = fract(uTime * 0.073 + 0.5);
            float grain  = hash21(vUV * 1.7 + t) - 0.5;
            float gw     = 1.0 - abs(lum * 2.0 - 1.0) * 0.55;
            col.rgb     += grain * 0.030 * gw;

            // ── 7. Vignette ───────────────────────────────────
            // Tightens slightly under velocity → tunnel-vision motion cue.
            float dist     = length(vUV);
            float vigEdge  = 0.78 - uVelMag * 0.12;
            float vignette = 1.0 - smoothstep(vigEdge, 1.42, dist);
            col.rgb       *= vignette;

            // ── 8. Polar cap fade ─────────────────────────────
            // Darkens near zenith/nadir to hide equirectangular pinch artefacts.
            float polarFade = 1.0 - smoothstep(0.82, 1.0, abs(ray.y)) * 0.55;
            col.rgb        *= polarFade;

            gl_FragColor = col;
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.02f, 0.02f, 0.04f, 1f)

        val verts = floatArrayOf(-1f,-1f, 1f,-1f, -1f,1f, 1f,1f)
        quadBuf = ByteBuffer.allocateDirect(verts.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
            .also { it.put(verts); it.position(0) }

        program = buildProgram(VS, FS)
        uYaw    = loc("uYaw");   uPitch  = loc("uPitch"); uFov    = loc("uFov")
        uAspect = loc("uAspect"); uTex   = loc("uTex");   uTime   = loc("uTime")
        uVelMag = loc("uVelMag"); uVelYaw= loc("uVelYaw"); uVelPit = loc("uVelPit")

        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        texId = ids[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,     GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,     GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        // 1×1 dark placeholder — no white flash on first frame
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 1, 1, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
            ByteBuffer.wrap(byteArrayOf(4, 4, 10, 255.toByte()))
        )
        lastMs = System.currentTimeMillis()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        vpW = width; vpH = height
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Upload bitmap on GL thread (exactly once after load)
        state.pendingBitmap?.let { bmp ->
            uploadBitmap(bmp)
            state.pendingBitmap = null
            state.textureReady  = true
        }

        val now = System.currentTimeMillis()
        val dt  = ((now - lastMs) / 1000f).coerceIn(0f, 0.05f)
        lastMs  = now
        state.tick(dt)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)

        val aPos = GLES20.glGetAttribLocation(program, "aPos")
        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 0, quadBuf)

        GLES20.glUniform1f(uYaw,    state.yaw)
        GLES20.glUniform1f(uPitch,  state.pitch)
        GLES20.glUniform1f(uFov,    state.fovDeg)
        GLES20.glUniform1f(uAspect, vpW.toFloat() / vpH.toFloat())
        GLES20.glUniform1f(uTime,   state.elapsedSec)
        GLES20.glUniform1f(uVelMag, state.velMag)
        GLES20.glUniform1f(uVelYaw, state.velYaw)
        GLES20.glUniform1f(uVelPit, state.velPitch)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glUniform1i(uTex, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(aPos)
    }

    private fun loc(name: String) = GLES20.glGetUniformLocation(program, name)

    private fun uploadBitmap(bmp: Bitmap) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        val safe = if (bmp.config == Bitmap.Config.ARGB_8888) bmp
        else bmp.copy(Bitmap.Config.ARGB_8888, false)
        val buf = ByteBuffer.allocateDirect(safe.width * safe.height * 4)
            .order(ByteOrder.nativeOrder())
        safe.copyPixelsToBuffer(buf); buf.position(0)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
            safe.width, safe.height, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf
        )
        // Generate mipmaps for better quality at high zoom-out FOV
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
    }

    private fun buildProgram(vs: String, fs: String): Int {
        fun shader(type: Int, src: String): Int {
            val s = GLES20.glCreateShader(type)
            GLES20.glShaderSource(s, src); GLES20.glCompileShader(s)
            // Optional: log errors in debug
            // val log = GLES20.glGetShaderInfoLog(s)
            // if (log.isNotBlank()) android.util.Log.e("PanoShader", log)
            return s
        }
        return GLES20.glCreateProgram().also { p ->
            GLES20.glAttachShader(p, shader(GLES20.GL_VERTEX_SHADER,   vs))
            GLES20.glAttachShader(p, shader(GLES20.GL_FRAGMENT_SHADER, fs))
            GLES20.glLinkProgram(p)
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Gyroscope sensor helper
// ─────────────────────────────────────────────────────────────

class GyroSensor(private val alpha: Float) : SensorEventListener {
    private var callback: ((Float, Float) -> Unit)? = null
    private var lastTime = 0L

    fun register(ctx: Context, cb: (yawDelta: Float, pitchDelta: Float) -> Unit) {
        callback = cb
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm.registerListener(this,
            sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) ?: return,
            SensorManager.SENSOR_DELAY_GAME)
    }

    fun unregister(ctx: Context) {
        (ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager).unregisterListener(this)
        callback = null; lastTime = 0L
    }

    override fun onSensorChanged(e: SensorEvent) {
        if (e.sensor.type != Sensor.TYPE_GYROSCOPE) return
        val now = e.timestamp
        if (lastTime == 0L) { lastTime = now; return }
        val dt = (now - lastTime) / 1_000_000_000f
        lastTime = now
        callback?.invoke(
            -e.values[2] * dt * (1f - alpha),
            e.values[0] * dt * (1f - alpha)
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

// ─────────────────────────────────────────────────────────────
//  Reward particle system
//  Gold-dust particles that burst in when the pano loads and
//  fade out over ~4 seconds — signals "you've unlocked something".
// ─────────────────────────────────────────────────────────────

private data class Particle(
    val seedX:  Float,   // 0..1 initial X fraction
    val seedY:  Float,   // 0..1 initial Y fraction
    val vx:     Float,   // normalized horizontal drift
    val vy:     Float,   // normalized upward speed (negative = up)
    val radius: Float,   // dp
    val alpha:  Float,   // peak opacity
    val phase:  Float,   // time offset so burst is staggered
)

@Composable
private fun RewardParticles(loadDone: Boolean) {
    // Generate a stable set of particles once
    val particles = remember {
        List(42) {
            Particle(
                seedX  = Random.nextFloat(),
                seedY  = Random.nextFloat() * 0.6f + 0.2f,   // avoid extreme top/bottom
                vx     = (Random.nextFloat() - 0.5f) * 0.08f,
                vy     = -(Random.nextFloat() * 0.12f + 0.04f),
                radius = Random.nextFloat() * 2.5f + 0.8f,
                alpha  = Random.nextFloat() * 0.55f + 0.15f,
                phase  = Random.nextFloat() * 0.6f,           // stagger burst timing
            )
        }
    }

    // Continuous time drives the particles — no per-particle state needed
    val infiniteTransition = rememberInfiniteTransition(label = "ptime")
    val rawTime by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 8f,
        animationSpec  = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label          = "rawTime"
    )

    // Overall fade-in on load, fade-out after 3.5s
    val globalAlpha by animateFloatAsState(
        targetValue   = if (loadDone) 1f else 0f,
        animationSpec = tween(600),
        label         = "gAlpha"
    )
    var elapsed by remember { mutableStateOf(0f) }
    LaunchedEffect(loadDone) {
        if (loadDone) {
            val start = System.currentTimeMillis()
            while (true) {
                delay(16)
                elapsed = (System.currentTimeMillis() - start) / 1000f
                if (elapsed > 5f) break
            }
        }
    }
    val decayAlpha = if (elapsed == 0f) 1f else (1f - ((elapsed - 1.5f) / 3.5f)).coerceIn(0f, 1f)
    val finalAlpha = globalAlpha * decayAlpha
    if (finalAlpha < 0.01f) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        val W = size.width; val H = size.height
        particles.forEach { p ->
            val t = (rawTime + p.phase) % 8f             // local time, looping
            val px = (p.seedX + p.vx * t).mod(1.0f) * W
            val py = (p.seedY + p.vy * t).mod(1.0f) * H
            // Twinkle: sine-wave alpha modulation
            val twinkle = 0.55f + 0.45f * sin(t * 3.2f + p.seedX * 6.28f)
            drawCircle(
                color  = goldParticleColor.copy(alpha = p.alpha * twinkle * finalAlpha),
                radius = p.radius.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }
}

private val goldParticleColor = Color(0xFFFFD97D)

// ─────────────────────────────────────────────────────────────
//  UI sub-composables
// ─────────────────────────────────────────────────────────────

private val PanoMode.accentColor get() = when (this) {
    PanoMode.TOUCH -> Color(0xFFA78BFA)   // violet
    PanoMode.GYRO  -> Color(0xFF6EF7B5)   // mint
    PanoMode.AUTO  -> Color(0xFF6EE7F7)   // cyan
}

// ── Compass rose ──────────────────────────────────────────────
@Composable
private fun CompassRose(yawDeg: Float, mode: PanoMode, size: Dp) {
    val accent       by animateColorAsState(mode.accentColor, tween(400), label = "cAccent")
    val needleAngle  by animateFloatAsState(-yawDeg, spring(0.72f, 180f),  label = "needle")

    Canvas(Modifier.size(size)) {
        val cx = this.size.width  / 2f
        val cy = this.size.height / 2f
        val r  = this.size.minDimension / 2f - 1.dp.toPx()
        val sw = 0.9.dp.toPx()

        // Outer ring
        drawCircle(Color.White.copy(alpha = 0.07f), r, Offset(cx, cy), style = Stroke(sw))

        // Tick marks: 36 total (every 10°), cardinals highlighted
        for (i in 0 until 36) {
            val ang  = Math.toRadians(i * 10.0).toFloat()
            val card = i % 9 == 0
            val r1   = r * (if (card) 0.78f else 0.86f)
            val sinA = sin(ang); val cosA = cos(ang)
            drawLine(
                color       = if (card) accent.copy(alpha = 0.80f) else Color.White.copy(alpha = 0.18f),
                start       = Offset(cx + sinA * r1, cy - cosA * r1),
                end         = Offset(cx + sinA * r,  cy - cosA * r),
                strokeWidth = if (card) 1.4.dp.toPx() else sw,
                cap         = StrokeCap.Round
            )
        }

        // Rotating needle
        rotate(degrees = needleAngle, pivot = Offset(cx, cy)) {
            val northTip  = Offset(cx, cy - r * 0.56f)
            val southTail = Offset(cx, cy + r * 0.32f)
            val nw        = 2.2.dp.toPx()
            drawLine(accent, Offset(cx, cy), northTip,  nw, StrokeCap.Round)
            drawLine(Color.White.copy(alpha = 0.25f), Offset(cx, cy), southTail, nw, StrokeCap.Round)
            // Arrowhead wings
            val aw = 4.5.dp.toPx()
            val mid = Offset(cx, cy - r * 0.38f)
            drawLine(accent, northTip, Offset(mid.x - aw, mid.y), 1.2.dp.toPx(), StrokeCap.Round)
            drawLine(accent, northTip, Offset(mid.x + aw, mid.y), 1.2.dp.toPx(), StrokeCap.Round)
            // Pivot
            drawCircle(accent, 2.6.dp.toPx(), Offset(cx, cy))
            drawCircle(Color(0xFF0A0A14), 1.3.dp.toPx(), Offset(cx, cy))
        }
    }
}

// ── Mode badge ────────────────────────────────────────────────
@Composable
private fun ModeBadge(mode: PanoMode) {
    val accent by animateColorAsState(mode.accentColor, tween(350), label = "accent")
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.55f))
            .drawBehind {
                drawRoundRect(accent.copy(alpha = 0.40f),
                    cornerRadius = CornerRadius(size.height / 2f), style = Stroke(1.dp.toPx()))
            }
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(mode.icon, null, tint = accent, modifier = Modifier.size(13.dp))
        Text(mode.label.uppercase(), color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
    }
}

// ── Bearing readout ───────────────────────────────────────────
@Composable
private fun BearingReadout(yawDeg: Float, fovDeg: Float) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.50f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("${yawDeg.toInt()}°", color = Color(0xFF6EE7F7),
            fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("FOV ${fovDeg.toInt()}°", color = Color.White.copy(alpha = 0.38f),
            fontSize = 9.sp, letterSpacing = 0.8.sp)
    }
}

// ── Position ribbon ───────────────────────────────────────────
@Composable
private fun PositionRibbon(yawDeg: Float, mode: PanoMode, modifier: Modifier = Modifier) {
    val frac   = yawDeg / 360f
    val accent by animateColorAsState(mode.accentColor, tween(350), label = "ribAccent")
    Box(modifier = modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.10f))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(frac.coerceIn(0f, 1f))
            .background(accent.copy(alpha = 0.60f)))
        Box(Modifier.align(Alignment.CenterStart)
            .offset(x = (frac * 144f).coerceIn(0f, 152f).dp)
            .size(8.dp).clip(CircleShape).background(accent))
    }
}

// ── Mode switcher ─────────────────────────────────────────────
@Composable
private fun ModeSwitcher(current: PanoMode, onSelect: (PanoMode) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF0E0E16).copy(alpha = 0.90f))
            .drawBehind {
                drawRoundRect(Color.White.copy(alpha = 0.08f),
                    cornerRadius = CornerRadius(size.height / 2f), style = Stroke(0.8.dp.toPx()))
            }
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        PanoMode.entries.forEach { item ->
            ModePill(item, selected = current == item, onClick = { onSelect(item) })
        }
    }
}

@Composable
private fun ModePill(item: PanoMode, selected: Boolean, onClick: () -> Unit) {
    val accent   = item.accentColor
    val bgAlpha  by animateFloatAsState(if (selected) 1f else 0f,    spring(0.72f, 400f), label = "bg")
    val txtAlpha by animateFloatAsState(if (selected) 1f else 0.45f, tween(200),           label = "txt")
    val scale    by animateFloatAsState(if (selected) 1.12f else 1f,  spring(0.55f, 500f), label = "sc")
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(44.dp))
            .background(accent.copy(alpha = bgAlpha * 0.16f))
            .drawBehind {
                if (selected) drawRoundRect(accent.copy(alpha = 0.38f),
                    cornerRadius = CornerRadius(44.dp.toPx()), style = Stroke(0.8.dp.toPx()))
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(item.icon, item.hint,
            tint     = if (selected) accent else Color.White.copy(alpha = 0.45f),
            modifier = Modifier.size(17.dp).graphicsLayer { scaleX = scale; scaleY = scale })
        Text(item.label,
            color      = if (selected) Color.White else Color.White.copy(alpha = txtAlpha),
            fontSize   = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            letterSpacing = 0.3.sp)
    }
}

// ── Loading overlay ───────────────────────────────────────────
@Composable
private fun LoadingOverlay() {
    val transition = rememberInfiniteTransition(label = "load")
    val sweep by transition.animateFloat(
        0f, 360f, infiniteRepeatable(tween(1100, easing = LinearEasing)), label = "sweep"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Box(Modifier.fillMaxSize().background(Color(0xFF04040C)), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated ring
            Canvas(Modifier.size(52.dp)) {
                val cx = size.width / 2f; val cy = size.height / 2f
                val r  = size.minDimension / 2f - 2.dp.toPx()
                // Background ring
                drawCircle(Color(0xFF6EE7F7).copy(alpha = 0.08f), r,
                    Offset(cx, cy), style = Stroke(1.6.dp.toPx()))
                // Spinning arc
                drawArc(Color(0xFF6EE7F7), sweep, 220f, false,
                    topLeft = Offset(cx - r, cy - r), size = Size(r * 2f, r * 2f),
                    style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round))
                // Inner pulsing dot
                drawCircle(Color(0xFFFFD97D).copy(alpha = pulse), 4.dp.toPx(), Offset(cx, cy))
            }
            Text("Loading reward…",
                color         = Color.White.copy(alpha = 0.28f),
                fontSize      = 11.sp,
                letterSpacing = 1.0.sp,
                fontWeight    = FontWeight.Medium)
        }
    }
}