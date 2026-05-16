package com.itsfrz.tictactoe.reward.components

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

// ─────────────────────────────────────────────────────────────
//  Public API
// ─────────────────────────────────────────────────────────────

enum class PanoramaViewMode(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    AUTO("Auto", Icons.Rounded.PlayArrow, "Auto-pan mode"),
    CINEMA("Cinema", Icons.Rounded.Theaters, "Cinema mode with slow zoom"),
    MANUAL("Manual", Icons.Rounded.TouchApp, "Manual drag mode"),
    GYRO("Gyro", Icons.Rounded.ScreenRotation, "Gyroscope-controlled mode")
}

data class PanoramaViewerConfig(
    /** Max horizontal translation as a fraction of screen width. */
    val maxOffsetFraction: Float = 0.40f,
    /** Duration (ms) for AUTO mode pan cycle. */
    val autoPanDurationMs: Int = 7_000,
    /** Duration (ms) for CINEMA mode pan cycle. */
    val cinemaPanDurationMs: Int = 16_000,
    /** Zoom target in CINEMA mode (1.0 = no zoom). */
    val cinemaZoomTarget: Float = 1.08f,
    /** How many ms the control bar stays visible after last interaction. */
    val controlsHideDelayMs: Long = 3_000,
    /** Gyroscope low-pass filter alpha (0..1). Higher = smoother but laggier. */
    val gyroSmoothingAlpha: Float = 0.85f,
    /** Gyroscope sensitivity multiplier. */
    val gyroSensitivity: Float = 180f,
)

// ─────────────────────────────────────────────────────────────
//  Main composable
// ─────────────────────────────────────────────────────────────

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PanoramaViewer(
    imageUrl: String,
    modifier: Modifier = Modifier,
    initialMode: PanoramaViewMode = PanoramaViewMode.AUTO,
    config: PanoramaViewerConfig = PanoramaViewerConfig(),
    onModeChanged: ((PanoramaViewMode) -> Unit)? = null,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()

    // ── State ────────────────────────────────────────────────
    var mode by remember { mutableStateOf(initialMode) }
    var manualOffsetX by remember { mutableFloatStateOf(0f) }
    var controlsVisible by remember { mutableStateOf(true) }
    var loadState by remember { mutableStateOf<LoadState>(LoadState.Loading) }
    var gyroOffsetX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val maxOffset = screenWidth * config.maxOffsetFraction

    // ── Gyroscope ────────────────────────────────────────────
    val gyroListener = remember {
        GyroscopeListener(
            alpha = config.gyroSmoothingAlpha,
            sensitivity = config.gyroSensitivity,
            maxOffset = maxOffset
        ) { x -> gyroOffsetX = x }
    }

    DisposableEffect(mode) {
        if (mode == PanoramaViewMode.GYRO) {
            gyroListener.register(context)
        }
        onDispose { gyroListener.unregister(context) }
    }

    // ── Infinite animations ──────────────────────────────────
    val transition = rememberInfiniteTransition(label = "panorama")

    val autoPan by transition.animateFloat(
        initialValue = -screenWidth * 0.22f,
        targetValue = screenWidth * 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (mode == PanoramaViewMode.CINEMA)
                    config.cinemaPanDurationMs else config.autoPanDurationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "autoPan"
    )

    val autoZoom by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (mode == PanoramaViewMode.CINEMA) config.cinemaZoomTarget else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 12_000,
                easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "autoZoom"
    )

    // Animated translation value (springs when switching modes)
    val translationXTarget = when (mode) {
        PanoramaViewMode.MANUAL -> manualOffsetX
        PanoramaViewMode.GYRO -> gyroOffsetX
        else -> autoPan
    }
    val translationXAnim by animateFloatAsState(
        targetValue = translationXTarget,
        animationSpec = if (isDragging) snap() else spring(dampingRatio = 0.8f, stiffness = 120f),
        label = "translateX"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (mode == PanoramaViewMode.CINEMA) autoZoom else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 80f),
        label = "scale"
    )

    // ── Controls visibility ──────────────────────────────────
    fun showControlsTemporarily() {
        controlsVisible = true
        scope.launch {
            delay(config.controlsHideDelayMs)
            controlsVisible = false
        }
    }

    LaunchedEffect(mode) {
        showControlsTemporarily()
        onModeChanged?.invoke(mode)
    }

    // ── Root ─────────────────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .semantics {
                contentDescription =
                    "360° panorama viewer, current mode: ${mode.contentDescription}"
            }
    ) {

        // ── Image ────────────────────────────────────────────
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Panoramic image",
            contentScale = ContentScale.Crop,
            loading = { PanoramaShimmer() },
            error = { PanoramaError(onRetry = { /* retrigger load */ }) },
            onSuccess = { loadState = LoadState.Success },
            onError = { loadState = LoadState.Error("Failed to load image") },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = translationXAnim
                    scaleX = scaleAnim * 1.45f   // over-scale so edges don't show
                    scaleY = scaleAnim * 1.45f
                    renderEffect = null
                }
                .pointerInput(mode) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            showControlsTemporarily()
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { _, drag ->
                            if (mode == PanoramaViewMode.MANUAL) {
                                manualOffsetX =
                                    (manualOffsetX + drag.x).coerceIn(-maxOffset, maxOffset)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { showControlsTemporarily() })
                }
        )

        // ── Vignette overlay ──────────────────────────────────
        Vignette()

        // ── Mode indicator pill (top left) ───────────────────
        AnimatedVisibility(
            visible = controlsVisible && loadState == LoadState.Success,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            ModePill(mode = mode)
        }

        // ── Gyro position indicator ───────────────────────────
        if (mode == PanoramaViewMode.GYRO) {
            GyroPositionBar(
                offsetFraction = gyroOffsetX / maxOffset,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp)
                    .fillMaxWidth(0.5f)
            )
        }

        // ── Manual scrub indicator ────────────────────────────
        AnimatedVisibility(
            visible = mode == PanoramaViewMode.MANUAL && controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
        ) {
            ScrubBar(
                offsetFraction = manualOffsetX / maxOffset,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ── Bottom control bar ───────────────────────────────
        AnimatedVisibility(
            visible = controlsVisible && loadState == LoadState.Success,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            ModeSelector(
                currentMode = mode,
                onModeSelected = { newMode ->
                    if (newMode != mode) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // Reset manual offset when switching away from manual
                        if (mode == PanoramaViewMode.MANUAL) manualOffsetX = 0f
                        mode = newMode
                    }
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Sub-composables
// ─────────────────────────────────────────────────────────────

@Composable
private fun PanoramaShimmer(modifier: Modifier = Modifier) {
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by shimmerTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val gradient = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),
                        Color(0xFF2C2C2C),
                        Color(0xFF1A1A1A)
                    ),
                    start = Offset(shimmerOffset * size.width, 0f),
                    end = Offset((shimmerOffset + 1f) * size.width, size.height)
                )
                onDrawBehind { drawRect(gradient) }
            }
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.6f),
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Loading panorama…",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun PanoramaError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.BrokenImage,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Couldn't load the panorama",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Try again", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun Vignette(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val vignette = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.55f)
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width * 0.85f
                )
                onDrawBehind { drawRect(vignette) }
            }
    )
}

@Composable
private fun ModePill(mode: PanoramaViewMode, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = mode.icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = mode.label.uppercase(),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun ModeSelector(
    currentMode: PanoramaViewMode,
    onModeSelected: (PanoramaViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // Glassmorphism pill
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .background(Color.Black.copy(alpha = 0.68f))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PanoramaViewMode.entries.forEach { item ->
            val selected = currentMode == item

            val bgAlpha by animateFloatAsState(
                targetValue = if (selected) 1f else 0f,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
                label = "btnBg"
            )
            val textAlpha by animateFloatAsState(
                targetValue = if (selected) 1f else 0.55f,
                animationSpec = tween(200),
                label = "btnText"
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100))
                    .background(Color.White.copy(alpha = bgAlpha))
                    .clickable { onModeSelected(item) }
                    .padding(horizontal = 16.dp, vertical = 9.dp)
                    .semantics {
                        contentDescription =
                            "${item.contentDescription}. ${if (selected) "Selected" else "Tap to select"}"
                        role = Role.Button
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (selected) Color.Black else Color.White.copy(alpha = textAlpha),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = item.label,
                    color = if (selected) Color.Black else Color.White.copy(alpha = textAlpha),
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
private fun ScrubBar(
    offsetFraction: Float,   // -1f..1f
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(3.dp)
            .clip(RoundedCornerShape(100))
            .background(Color.White.copy(alpha = 0.15f))
    ) {
        // Thumb
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(fraction = ((offsetFraction + 1f) / 2f).coerceIn(0f, 1f))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.White.copy(alpha = 0.1f), Color.White)
                    )
                )
        )
        // Indicator dot
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (((offsetFraction + 1f) / 2f) * 100).dp - 6.dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun GyroPositionBar(
    offsetFraction: Float,   // -1f..1f
    modifier: Modifier = Modifier
) {
    val fraction = ((offsetFraction + 1f) / 2f).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(2.dp)
            .clip(RoundedCornerShape(100))
            .background(Color.White.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(8.dp)
                .offset(x = (fraction * 100 - 4).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.7f))
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Gyroscope helper (not a composable)
// ─────────────────────────────────────────────────────────────

private class GyroscopeListener(
    private val alpha: Float,
    private val sensitivity: Float,
    private val maxOffset: Float,
    private val onOffsetChanged: (Float) -> Unit
) : SensorEventListener {

    private var smoothedAzimuth = 0f
    private var offset = 0f

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
        val rotMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
        val orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotMatrix, orientationAngles)
        // azimuth in radians → convert to degrees
        val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        smoothedAzimuth = alpha * smoothedAzimuth + (1f - alpha) * azimuth
        offset = (-smoothedAzimuth / 180f * sensitivity).coerceIn(-maxOffset, maxOffset)
        onOffsetChanged(offset)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun register(context: Context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) ?: return
        sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun unregister(context: Context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm.unregisterListener(this)
    }
}

// ─────────────────────────────────────────────────────────────
//  Internal state
// ─────────────────────────────────────────────────────────────

private sealed class LoadState {
    data object Loading : LoadState()
    data object Success : LoadState()
    data class Error(val message: String) : LoadState()
}



/*


can you optimize, switching views, in between not working, not true 360.

Also the quality also not matching google or apple level furnish.

Lack of creativity.


There no game angle touch inside.


Can you write super high fidelity, mesmerizing viewer ? with true furnish and amazing creativity ??

help


 */