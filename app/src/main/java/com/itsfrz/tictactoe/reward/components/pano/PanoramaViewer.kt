package com.itsfrz.tictactoe.reward.components.pano


/**
 * Panorama360Viewer.kt
 *
 * True spherical 360° panorama viewer for Jetpack Compose.
 *
 * Architecture:
 *   ┌─ PanoramaViewer (Composable) ────────────────────────────┐
 *   │   ├─ AndroidView → GLSurfaceView                         │
 *   │   │     └─ PanoRenderer (GLSurfaceView.Renderer)         │
 *   │   │           • OpenGL ES 2.0 inverse-equirectangular    │
 *   │   │             fragment shader (true sphere projection)  │
 *   │   │           • Pinch-to-zoom via GLSurfaceView touch    │
 *   │   │           • Drag + inertial fling                    │
 *   │   └─ Compose overlay (controls, badge, position ribbon)  │
 *   └──────────────────────────────────────────────────────────┘
 *
 * Dependencies (already in most projects):
 *   implementation("io.coil-kt:coil-compose:2.6.0")
 *   // No extra OpenGL dep needed — android.opengl is part of the SDK
 *
 * Usage:
 *   PanoramaViewer(
 *       imageUrl = "https://example.com/equirectangular.jpg",
 *       modifier = Modifier.fillMaxSize()
 *   )
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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

// ─────────────────────────────────────────────────────────────
//  Public API
// ─────────────────────────────────────────────────────────────

enum class PanoMode(val label: String, val icon: ImageVector, val hint: String) {
    TOUCH("Touch",  Icons.Rounded.PanTool,        "Drag & fling · pinch to zoom"),
    GYRO ("Gyro",   Icons.Rounded.ScreenRotation, "Tilt device to look around"),
    AUTO ("Orbit",  Icons.Rounded.Autorenew,      "Gentle auto-pan"),
}

data class PanoConfig(
    /** Initial horizontal field-of-view in degrees. */
    val initialFovDeg: Float    = 80f,
    /** Minimum FOV (max zoom-in). */
    val minFovDeg: Float        = 25f,
    /** Maximum FOV (max zoom-out). */
    val maxFovDeg: Float        = 120f,
    /** Inertial fling friction per frame (0..1; lower = longer coast). */
    val flingFriction: Float    = 0.88f,
    /** Gyroscope low-pass alpha (higher = smoother but laggier). */
    val gyroAlpha: Float        = 0.90f,
    /** Auto-orbit angular speed in radians per second. */
    val orbitSpeedRad: Float    = 0.12f,
    /** How long controls stay visible after last interaction (ms). */
    val controlsHideMs: Long    = 3_500L,
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

    // ── shared render state (written on main thread, read on GL thread) ──
    val renderState = remember { PanoRenderState(config) }

    // ── UI state ────────────────────────────────────────────
    var mode         by remember { mutableStateOf(initialMode) }
    var showControls by remember { mutableStateOf(true) }
    var loadDone     by remember { mutableStateOf(false) }

    // ── Gyroscope ────────────────────────────────────────────
    val gyroListener = remember { GyroSensor(config.gyroAlpha) }
    DisposableEffect(mode) {
        if (mode == PanoMode.GYRO) gyroListener.register(context) { yawDelta, pitchDelta ->
            renderState.applyGyro(yawDelta, pitchDelta)
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

    // ── Controls hide ─────────────────────────────────────────
    var hideJob by remember { mutableStateOf<Job?>(null) }
    fun nudge() {
        showControls = true
        hideJob?.cancel()
        hideJob = scope.launch {
            delay(config.controlsHideMs)
            showControls = false
        }
    }
    LaunchedEffect(mode) { nudge(); onModeChange?.invoke(mode) }

    // ── Load bitmap via Coil on IO ────────────────────────────
    LaunchedEffect(imageUrl) {
        withContext(Dispatchers.IO) {
            try {
                val loader  = ImageLoader(context)
                val request = ImageRequest.Builder(context).data(imageUrl).allowHardware(false).build()
                val result  = loader.execute(request)
                if (result is SuccessResult) {
                    val bmp = (result.drawable as BitmapDrawable).bitmap
                    renderState.pendingBitmap = bmp
                    withContext(Dispatchers.Main) { loadDone = true }
                }
            } catch (_: Exception) { /* surfaced in UI */ }
        }
    }

    // ── Derived UI values (read by controls) ──────────────────
    val yawDeg by remember { derivedStateOf { (renderState.yaw * 180f / PI.toFloat() % 360f + 360f) % 360f } }

    Box(modifier = modifier.fillMaxSize()) {

        // ── GL Surface ────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                PanoGLSurfaceView(ctx, renderState).apply {
                    onTouchCallback = { nudge() }
                    onTouchModeActive = { mode == PanoMode.TOUCH }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { nudge() } },
            update = { view ->
                view.setMode(mode)
            }
        )

        // ── Position ribbon (always visible once loaded) ──────
        AnimatedVisibility(
            visible  = loadDone,
            enter    = fadeIn(tween(600)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
        ) {
            PositionRibbon(
                yawDeg  = yawDeg,
                mode    = mode,
                modifier = Modifier.width(160.dp).height(3.dp)
            )
        }

        // ── Mode badge (top-left) ─────────────────────────────
        AnimatedVisibility(
            visible  = showControls && loadDone,
            enter    = fadeIn() + slideInVertically { -32 },
            exit     = fadeOut() + slideOutVertically { -32 },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(14.dp)
        ) { ModeBadge(mode) }

        // ── FOV / bearing readout (top-right) ─────────────────
        AnimatedVisibility(
            visible  = showControls && loadDone,
            enter    = fadeIn() + slideInHorizontally { it },
            exit     = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(14.dp)
        ) { BearingReadout(yawDeg = yawDeg, fovDeg = renderState.fovDeg) }

        // ── Bottom mode switcher ──────────────────────────────
        AnimatedVisibility(
            visible  = showControls && loadDone,
            enter    = fadeIn() + slideInVertically { it / 2 },
            exit     = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 26.dp)
        ) {
            ModeSwitcher(
                current  = mode,
                onSelect = { next ->
                    if (next == mode) return@ModeSwitcher
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (mode == PanoMode.TOUCH) renderState.stopFling()
                    mode = next
                }
            )
        }

        // ── Loading shimmer overlay ───────────────────────────
        AnimatedVisibility(
            visible = !loadDone,
            exit    = fadeOut(tween(800))
        ) { LoadingOverlay() }
    }
}

// ─────────────────────────────────────────────────────────────
//  Shared render state  (main ↔ GL thread bridge)
//  All fields are @Volatile so GL thread sees latest writes.
// ─────────────────────────────────────────────────────────────

class PanoRenderState(val config: PanoConfig) {
    @Volatile var yaw:           Float   = 0f
    @Volatile var pitch:         Float   = 0f
    @Volatile var fovDeg:        Float   = config.initialFovDeg
    @Volatile var velYaw:        Float   = 0f
    @Volatile var velPitch:      Float   = 0f
    @Volatile var pendingBitmap: Bitmap? = null
    @Volatile var textureReady:  Boolean = false

    /** Called every GL frame to advance physics. */
    fun tick(dtSec: Float) {
        if (velYaw.absoluteValue > 0.001f || velPitch.absoluteValue > 0.001f) {
            yaw   += velYaw   * dtSec
            pitch += velPitch * dtSec
            velYaw   *= config.flingFriction.pow(dtSec * 60f)
            velPitch *= config.flingFriction.pow(dtSec * 60f)
        }
        pitch = pitch.coerceIn(-PI.toFloat() / 2.2f, PI.toFloat() / 2.2f)
    }

    fun applyDrag(dxPx: Float, dyPx: Float, viewWidthPx: Int) {
        val sens = fovDeg / 90f * (PI.toFloat() / viewWidthPx)
        val dy   = -dxPx * sens
        val dp   = -dyPx * sens
        yaw   += dy;  velYaw   = dy
        pitch += dp;  velPitch = dp
        pitch = pitch.coerceIn(-PI.toFloat() / 2.2f, PI.toFloat() / 2.2f)
    }

    fun applyPinch(scaleFactor: Float) {
        fovDeg = (fovDeg / scaleFactor).coerceIn(config.minFovDeg, config.maxFovDeg)
    }

    fun applyGyro(yawDelta: Float, pitchDelta: Float) {
        yaw   += yawDelta
        pitch  = (pitch + pitchDelta).coerceIn(-PI.toFloat() / 2.2f, PI.toFloat() / 2.2f)
    }

    fun stopFling() { velYaw = 0f; velPitch = 0f }
}

// ─────────────────────────────────────────────────────────────
//  GLSurfaceView wrapper (handles touch in-view)
// ─────────────────────────────────────────────────────────────

class PanoGLSurfaceView(
    context: Context,
    private val state: PanoRenderState,
) : GLSurfaceView(context) {

    var onTouchCallback:  (() -> Unit)?      = null
    var onTouchModeActive: (() -> Boolean)?  = null

    private val renderer = PanoRenderer(state)
    private var lastX = 0f; private var lastY = 0f
    private var ptr0Id = -1; private var ptr1Id = -1
    private var pinchDist0 = 0f; private var pinchFov0  = 0f
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
                lastX = e.x; lastY = e.y
                state.stopFling()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (e.pointerCount == 2) {
                    ptr1Id = e.getPointerId(e.actionIndex)
                    pinchDist0 = pinchDist(e)
                    pinchFov0  = state.fovDeg
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (e.pointerCount >= 2 && ptr1Id >= 0) {
                    // pinch zoom – always enabled
                    val dist = pinchDist(e)
                    if (pinchDist0 > 0f) state.applyPinch(dist / pinchDist0)
                    pinchDist0 = dist; pinchFov0 = state.fovDeg
                } else if (touchActive) {
                    val idx = e.findPointerIndex(ptr0Id).coerceAtLeast(0)
                    val nx  = e.getX(idx); val ny = e.getY(idx)
                    state.applyDrag(nx - lastX, ny - lastY, width)
                    lastX = nx; lastY = ny
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                ptr0Id = -1; ptr1Id = -1
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (e.getPointerId(e.actionIndex) == ptr1Id) ptr1Id = -1
            }
        }
        return true
    }

    private fun pinchDist(e: MotionEvent): Float {
        val i0 = e.findPointerIndex(ptr0Id).coerceAtLeast(0)
        val i1 = e.findPointerIndex(ptr1Id).coerceAtLeast(1)
        val dx = e.getX(i0) - e.getX(i1)
        val dy = e.getY(i0) - e.getY(i1)
        return sqrt(dx * dx + dy * dy)
    }
}

// ─────────────────────────────────────────────────────────────
//  OpenGL ES 2.0 renderer
//  True equirectangular → sphere projection in fragment shader.
// ─────────────────────────────────────────────────────────────

class PanoRenderer(private val state: PanoRenderState) : GLSurfaceView.Renderer {

    private var program    = 0
    private var texId      = 0
    private var quadBuf: FloatBuffer? = null
    private var uYaw       = 0; private var uPitch = 0
    private var uFov       = 0; private var uAspect = 0; private var uTex = 0
    private var vpW        = 1; private var vpH = 1
    private var lastMs     = 0L

    /* ── Vertex shader: full-screen quad in NDC ──────────────── */
    private val VS = """
        attribute vec2 aPos;
        varying   vec2 vUV;
        void main() {
            vUV = aPos;
            gl_Position = vec4(aPos, 0.0, 1.0);
        }
    """.trimIndent()

    /*
     * ── Fragment shader: inverse equirectangular projection ────
     *
     * For every screen pixel we shoot a ray into the sphere,
     * rotate by (yaw, pitch), then convert to lat/lon → UV.
     *
     * Yaw   = horizontal rotation (−π..π)
     * Pitch = vertical tilt      (−π/2..π/2)
     */
    private val FS = """
        precision mediump float;
        uniform sampler2D uTex;
        uniform float uYaw;
        uniform float uPitch;
        uniform float uFov;
        uniform float uAspect;
        varying vec2 vUV;

        #define PI  3.14159265358979323846
        #define PI2 6.28318530717958647692

        void main() {
            float tanHalf = tan(radians(uFov) * 0.5);

            // Ray in camera space (right-handed, looking down -Z)
            vec3 ray = normalize(vec3(
                vUV.x * tanHalf * uAspect,
                vUV.y * tanHalf,
               -1.0
            ));

            // Rotate by pitch (around X)
            float cp = cos(uPitch), sp = sin(uPitch);
            ray = vec3(
                ray.x,
                cp * ray.y - sp * ray.z,
                sp * ray.y + cp * ray.z
            );

            // Rotate by yaw (around Y)
            float cy = cos(uYaw), sy = sin(uYaw);
            ray = vec3(
                cy * ray.x + sy * ray.z,
                ray.y,
               -sy * ray.x + cy * ray.z
            );

            // Spherical coords → equirectangular UV
            float lon = atan(ray.x, -ray.z);          // −π..π
            float lat = asin(clamp(ray.y, -1.0, 1.0)); // −π/2..π/2
            float u   = lon / PI2 + 0.5;
            float v   = lat / PI  + 0.5;

            gl_FragColor = texture2D(uTex, vec2(u, 1.0 - v));
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.02f, 0.02f, 0.04f, 1f)

        // Full-screen quad (two triangles as triangle-strip)
        val verts = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
        quadBuf = ByteBuffer.allocateDirect(verts.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
            .also { it.put(verts); it.position(0) }

        program = buildProgram(VS, FS)
        uYaw    = GLES20.glGetUniformLocation(program, "uYaw")
        uPitch  = GLES20.glGetUniformLocation(program, "uPitch")
        uFov    = GLES20.glGetUniformLocation(program, "uFov")
        uAspect = GLES20.glGetUniformLocation(program, "uAspect")
        uTex    = GLES20.glGetUniformLocation(program, "uTex")

        // Reserve one texture slot
        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        texId = ids[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,     GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,     GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        // 1×1 dark placeholder so first frame isn't blank-white
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 1, 1, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
            ByteBuffer.wrap(byteArrayOf(5, 5, 12, 255.toByte()))
        )
        lastMs = System.currentTimeMillis()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        vpW = width; vpH = height
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // ── upload pending bitmap (once, on GL thread) ────────
        state.pendingBitmap?.let { bmp ->
            uploadBitmap(bmp)
            state.pendingBitmap = null
            state.textureReady  = true
        }

        // ── physics tick ──────────────────────────────────────
        val now = System.currentTimeMillis()
        val dt  = ((now - lastMs) / 1000f).coerceIn(0f, 0.05f)
        lastMs  = now
        state.tick(dt)

        // ── draw ──────────────────────────────────────────────
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)

        val aPos = GLES20.glGetAttribLocation(program, "aPos")
        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 0, quadBuf)

        GLES20.glUniform1f(uYaw,    state.yaw)
        GLES20.glUniform1f(uPitch,  state.pitch)
        GLES20.glUniform1f(uFov,    state.fovDeg)
        GLES20.glUniform1f(uAspect, vpW.toFloat() / vpH.toFloat())

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glUniform1i(uTex, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(aPos)
    }

    private fun uploadBitmap(bmp: Bitmap) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        val safe = if (bmp.config == Bitmap.Config.ARGB_8888) bmp
        else bmp.copy(Bitmap.Config.ARGB_8888, false)
        val buf = ByteBuffer.allocateDirect(safe.width * safe.height * 4)
            .order(ByteOrder.nativeOrder())
        safe.copyPixelsToBuffer(buf)
        buf.position(0)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
            safe.width, safe.height, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf
        )
    }

    private fun buildProgram(vs: String, fs: String): Int {
        fun shader(type: Int, src: String): Int {
            val s = GLES20.glCreateShader(type)
            GLES20.glShaderSource(s, src)
            GLES20.glCompileShader(s)
            return s
        }
        val p = GLES20.glCreateProgram()
        GLES20.glAttachShader(p, shader(GLES20.GL_VERTEX_SHADER,   vs))
        GLES20.glAttachShader(p, shader(GLES20.GL_FRAGMENT_SHADER, fs))
        GLES20.glLinkProgram(p)
        return p
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
        sm.registerListener(
            this,
            sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) ?: return,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun unregister(ctx: Context) {
        (ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager).unregisterListener(this)
        callback = null
        lastTime = 0L
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_GYROSCOPE) return
        val now = event.timestamp
        if (lastTime == 0L) { lastTime = now; return }
        val dt = (now - lastTime) / 1_000_000_000f   // nanoseconds → seconds
        lastTime = now
        // event.values: [0]=pitch rate, [1]=roll rate, [2]=yaw rate  (rad/s)
        val yawDelta   = -event.values[2] * dt * (1f - alpha)
        val pitchDelta =  event.values[0] * dt * (1f - alpha)
        callback?.invoke(yawDelta, pitchDelta)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

// ─────────────────────────────────────────────────────────────
//  UI sub-composables
// ─────────────────────────────────────────────────────────────

private val PanoMode.accentColor get() = when (this) {
    PanoMode.TOUCH -> Color(0xFFA78BFA)  // violet
    PanoMode.GYRO  -> Color(0xFF6EF7B5)  // mint
    PanoMode.AUTO  -> Color(0xFF6EE7F7)  // cyan
}

@Composable
private fun ModeBadge(mode: PanoMode) {
    val accent by animateColorAsState(mode.accentColor, tween(350), label = "accent")
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.55f))
            .drawBehind {
                drawRoundRect(
                    color        = accent.copy(alpha = 0.40f),
                    cornerRadius = CornerRadius(size.height / 2f),
                    style        = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment         = Alignment.CenterVertically,
        horizontalArrangement     = Arrangement.spacedBy(5.dp)
    ) {
        Icon(mode.icon, null, tint = accent, modifier = Modifier.size(13.dp))
        Text(mode.label.uppercase(), color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
    }
}

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

@Composable
private fun PositionRibbon(yawDeg: Float, mode: PanoMode, modifier: Modifier = Modifier) {
    val frac   = yawDeg / 360f
    val accent by animateColorAsState(mode.accentColor, tween(350), label = "ribAccent")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.10f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(frac.coerceIn(0f, 1f))
                .background(accent.copy(alpha = 0.65f))
        )
        // travelling dot
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (frac * 144).dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(accent)
        )
    }
}

@Composable
private fun ModeSwitcher(current: PanoMode, onSelect: (PanoMode) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF0E0E16).copy(alpha = 0.90f))
            .drawBehind {
                drawRoundRect(
                    color        = Color.White.copy(alpha = 0.08f),
                    cornerRadius = CornerRadius(size.height / 2f),
                    style        = Stroke(width = 0.8.dp.toPx())
                )
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
    val bgAlpha  by animateFloatAsState(if (selected) 1f else 0f,   spring(0.72f, 400f), label = "bg")
    val txtAlpha by animateFloatAsState(if (selected) 1f else 0.45f, tween(200),          label = "txt")
    val scale    by animateFloatAsState(if (selected) 1.12f else 1f, spring(0.55f, 500f), label = "sc")

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(44.dp))
            .background(accent.copy(alpha = bgAlpha * 0.16f))
            .drawBehind {
                if (selected) drawRoundRect(
                    color        = accent.copy(alpha = 0.38f),
                    cornerRadius = CornerRadius(44.dp.toPx()),
                    style        = Stroke(0.8.dp.toPx())
                )
            }
            .then(Modifier.clickable(onClick = onClick))   // ripple-free; add indication if needed
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector        = item.icon,
            contentDescription = item.hint,
            tint               = if (selected) accent else Color.White.copy(alpha = 0.45f),
            modifier           = Modifier.size(17.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
        )
        Text(
            text       = item.label,
            color      = if (selected) Color.White else Color.White.copy(alpha = txtAlpha),
            fontSize   = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
private fun LoadingOverlay() {
    val sweep by rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "sweep"
    )
    Box(
        modifier          = Modifier.fillMaxSize().background(Color(0xFF05050A)),
        contentAlignment  = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Canvas(Modifier.size(44.dp)) {
                drawArc(Color(0xFF6EE7F7).copy(alpha = 0.12f), 0f, 360f, false,
                    style = Stroke(1.8.dp.toPx()))
                drawArc(Color(0xFF6EE7F7), sweep, 210f, false,
                    style = Stroke(1.8.dp.toPx()))
            }
            Text("Loading panorama", color = Color.White.copy(alpha = 0.30f),
                fontSize = 12.sp, letterSpacing = 0.8.sp)
        }
    }
}