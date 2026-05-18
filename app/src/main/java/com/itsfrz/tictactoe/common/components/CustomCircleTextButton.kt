package com.itsfrz.tictactoe.common.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Easing ──────────────────────────────────────────────────────────────────
// Smooth sine curve – organic, never mechanical
private val SineEasing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)

// ── Palettes ─────────────────────────────────────────────────────────────────
// Each palette is a mountain/arctic gem: glacier, aurora, amethyst, rose quartz…
private enum class CrystalPalette(
    val core: Color,   // Bright lit center (white-hot light source)
    val mid: Color,    // Main body hue
    val edge: Color,   // Dark shadowed limb
    val glow: Color,   // Bloom / emission halo
    val inner: Color,  // Internal light scatter
) {
    GlacierBlue(
        core = Color(0xFFE8F6FF), mid = Color(0xFF4FC3F7),
        edge = Color(0xFF01579B), glow = Color(0xFF29B6F6),
        inner = Color(0xFFB3E5FC),
    ),
    AuroraPurple(
        core = Color(0xFFF5E8FF), mid = Color(0xFFCE93D8),
        edge = Color(0xFF4A148C), glow = Color(0xFFAB47BC),
        inner = Color(0xFFE1BEE7),
    ),
    ArcticTeal(
        core = Color(0xFFE0FFFB), mid = Color(0xFF4DB6AC),
        edge = Color(0xFF004D40), glow = Color(0xFF26A69A),
        inner = Color(0xFFB2DFDB),
    ),
    RoseQuartz(
        core = Color(0xFFFFECF1), mid = Color(0xFFF48FB1),
        edge = Color(0xFF880E4F), glow = Color(0xFFEC407A),
        inner = Color(0xFFFCE4EC),
    ),
    CitrineIce(
        core = Color(0xFFFFFDE8), mid = Color(0xFFFFD54F),
        edge = Color(0xFFBF360C), glow = Color(0xFFFFCA28),
        inner = Color(0xFFFFF9C4),
    ),
    AmethystSnow(
        core = Color(0xFFF8F0FF), mid = Color(0xFFB39DDB),
        edge = Color(0xFF311B92), glow = Color(0xFF9575CD),
        inner = Color(0xFFEDE7F6),
    ),
    MintCrystal(
        core = Color(0xFFE8FFF5), mid = Color(0xFF69F0AE),
        edge = Color(0xFF1B5E20), glow = Color(0xFF00E676),
        inner = Color(0xFFB9FBE3),
    ),
}

// ── Component ────────────────────────────────────────────────────────────────
@Composable
fun CrystalOrbButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    // Palette chosen once, never recalculated
    val palette = remember { CrystalPalette.values().random() }

    // One shared transition object drives ALL continuous animation
    val transition = rememberInfiniteTransition(label = "orb")

    // Slow pulse: breathing glow
    val glowPulse by transition.animateFloat(
        initialValue = 0.42f, targetValue = 1.00f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = SineEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )

    // Very slow drift: moves the specular highlight like light shifting on ice
    val shimmer by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = SineEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer",
    )

    // Press state
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "scale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick() },
                )
            },
    ) {
        // ── Single Canvas replaces all nested Box + Brush + shadow modifiers ──
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.5f
            val r = minOf(cx, cy)
            val c = Offset(cx, cy)

            // Light-source drifts slowly (upper-left → slightly more upper-left)
            // This makes the specular feel liquid / alive without being distracting
            val lx = cx - r * (0.30f + shimmer * 0.07f)
            val ly = cy - r * (0.28f + shimmer * 0.05f)

            // ① Outer bloom — soft halo, breathes with glowPulse ────────────
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.glow.copy(alpha = 0.42f * glowPulse),
                        palette.glow.copy(alpha = 0.12f * glowPulse),
                        Color.Transparent,
                    ),
                    center = c, radius = r * 1.72f,
                ),
                radius = r * 1.72f, center = c,
            )

            // ② Orb body — radial gradient offset toward light source ────────
            // Offset center creates convincing 3-D sphere shading
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.core,
                        palette.mid,
                        palette.mid.copy(alpha = 0.88f),
                        palette.edge,
                    ),
                    center = Offset(lx, ly), radius = r * 1.55f,
                ),
                radius = r, center = c,
            )

            // ③ Depth shadow — darkens the bottom-right limb (opposing the light) ──
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.28f),
                    ),
                    center = Offset(cx + r * 0.42f, cy + r * 0.42f),
                    radius = r * 1.1f,
                ),
                radius = r, center = c,
            )

            // ④ Inner crystal core — subsurface scatter, breathes ────────────
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.inner.copy(alpha = 0.62f * glowPulse),
                        Color.Transparent,
                    ),
                    center = c, radius = r * 0.50f,
                ),
                radius = r * 0.50f, center = c,
            )

            // ⑤ Crystalline facet ring — suggests internal structure ──────────
            drawCircle(
                color = Color.White.copy(alpha = 0.09f + 0.07f * glowPulse),
                radius = r * 0.70f, center = c,
                style = Stroke(width = 0.9f),
            )

            // ⑥ Primary specular — large, soft, drifts with shimmer ──────────
            // This is the "wet glass / ice surface" highlight
            val specC = Offset(
                cx - r * (0.29f + shimmer * 0.06f),
                cy - r * (0.31f + shimmer * 0.05f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.92f),
                        Color.White.copy(alpha = 0.34f),
                        Color.Transparent,
                    ),
                    center = specC, radius = r * 0.36f,
                ),
                radius = r * 0.36f, center = specC,
            )

            // ⑦ Sharp sparkle — gem-like point just above the main highlight ──
            val sparkC = Offset(cx - r * 0.06f, cy - r * 0.53f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.96f), Color.Transparent),
                    center = sparkC, radius = r * 0.09f,
                ),
                radius = r * 0.09f, center = sparkC,
            )

            // ⑧ Caustic rim glow — light escaping through the crystal's base ──
            val rimC = Offset(cx + r * 0.15f, cy + r * 0.45f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.glow.copy(alpha = 0.32f * glowPulse),
                        Color.Transparent,
                    ),
                    center = rimC, radius = r * 0.26f,
                ),
                radius = r * 0.26f, center = rimC,
            )
        }

        // Text floats above the Canvas – shadow makes it readable on any palette
        Text(
            text = text,
            style = TextStyle(
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.55f),
                    blurRadius = 8f,
                    offset = Offset(0f, 1f),
                ),
            ),
            textAlign = TextAlign.Center,
        )
    }
}