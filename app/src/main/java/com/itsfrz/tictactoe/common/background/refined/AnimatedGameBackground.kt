package com.itsfrz.tictactoe.common.background.refined

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ════════════════════════════════════════════════════════════════════════════
//  ② PALETTE  ─  SNES deep-ocean cycling (4 phases × 4 shades)
// ════════════════════════════════════════════════════════════════════════════

private val WATER_PALETTES = arrayOf(
    /* phase 0 – deep night */  arrayOf(Color(0xFF050F1E), Color(0xFF081A38), Color(0xFF0C2D5A), Color(0xFF104278)),
    /* phase 1 – mid pulse  */  arrayOf(Color(0xFF060E20), Color(0xFF0A1E42), Color(0xFF0E3468), Color(0xFF124A88)),
    /* phase 2 – light crest*/  arrayOf(Color(0xFF071228), Color(0xFF0C224C), Color(0xFF103C78), Color(0xFF155296)),
    /* phase 3 – mid pulse  */  arrayOf(Color(0xFF060E20), Color(0xFF0A1E42), Color(0xFF0E3468), Color(0xFF124A88)),
)

// Horizon fog to blend water → sky
private val WATER_FOG = Color(0xFF0A1428)

// ════════════════════════════════════════════════════════════════════════════
//  ③ MOUNTAIN LAYERS
// ════════════════════════════════════════════════════════════════════════════

private data class MtnLayer(
    val darkCol:   Color,
    val lightCol:  Color,
    val yFrac:     Float,   // where the peak-line sits as fraction of screen height
    val amplitude: Float,   // height of mountains as fraction of screen height
    val freq:      Float,   // ridge frequency
    val seed:      Float,   // phase offset
    val scrollIdx: Int      // index into Cfg.MTN_SCROLL
)

private val MOUNTAIN_LAYERS = listOf(
    // Far — ghostly purple ridge
    MtnLayer(Color(0xFF14082A), Color(0xFF1E1040), 0.48f, 0.17f, 1.1f, 2.3f, 0),
    // Mid — dark indigo
    MtnLayer(Color(0xFF0C0620), Color(0xFF120A30), 0.57f, 0.14f, 1.7f, 7.8f, 1),
    // Near — near-black silhouette
    MtnLayer(Color(0xFF060412), Color(0xFF09061A), 0.64f, 0.11f, 2.6f, 14.1f, 2),
)

// ════════════════════════════════════════════════════════════════════════════
//  ④ EMOJI PHYSICS PARTICLE
// ════════════════════════════════════════════════════════════════════════════

private val EMOJI_POOL = listOf(
    "🌟","✨","💀","🎯","🎲","💫","🔥", "🌈","🎪","🔴","🌙","💎", "🎠","🎡","🍄",
    "🦋","🎈","🌸", "👾","🪩","🧋","🧩", "🎨","🍄","🦄","🐳","🚀","📀","🫠","🪔"
)

private class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    val emoji: String,
    val textSizePx: Float,
    var rotation: Float       = Random.nextFloat() * 360f,
    var bounceCount: Int      = 0,
    val maxBounces: Int       = Random.nextInt(2, 6)
)

private fun randomParticle(w: Float, maxY: Float) = Particle(
    x           = Random.nextFloat() * w,
    y           = Random.nextFloat() * maxY * 0.85f + maxY * 0.03f,
    vx          = (Random.nextFloat() - 0.5f) * 110f,
    vy          = (Random.nextFloat() - 0.5f) * 65f,
    emoji       = EMOJI_POOL.random(),
    textSizePx  = 36f + Random.nextFloat() * 34f,
)

// ════════════════════════════════════════════════════════════════════════════
//  ⑤ MAIN COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@Composable
fun AnimatedGameBackground(modifier: Modifier = Modifier) {

    /* ── particles ──────────────────────────────────────────────────────── */
    val particles = remember { mutableStateListOf<Particle>() }
    var screenW   by remember { mutableFloatStateOf(0f) }
    var screenH   by remember { mutableFloatStateOf(0f) }

    /* ── physics loop (runs once screen size is known) ──────────────────── */
    LaunchedEffect(screenW, screenH) {
        if (screenW == 0f) return@LaunchedEffect

        val waterY   = screenH * BackgroundConfig.WATER_Y_FRAC
        val floatBand = waterY * 0.78f         // emojis float in this band

        if (particles.isEmpty()) {
            repeat(BackgroundConfig.MIN_PARTICLES) { particles += randomParticle(screenW, floatBand) }
        }

        var prevNs = 0L
        while (isActive) {
            withFrameNanos { ns ->
                val dt = if (prevNs == 0L) 0.016f
                else ((ns - prevNs) / 1_000_000_000f).coerceIn(0f, 0.05f)
                prevNs = ns

                val dead = ArrayList<Particle>(4)
                particles.forEach { p ->
                    // Integrate
                    p.vy       += BackgroundConfig.GRAVITY * dt
                    p.vx       *= (1f - BackgroundConfig.DRAG_H * dt)
                    p.vy       *= (1f - BackgroundConfig.DRAG_V * dt)
                    p.x        += p.vx * dt
                    p.y        += p.vy * dt
                    p.rotation += p.vx * 0.22f * dt

                    // Bounce off water surface
                    if (p.y >= waterY && p.vy > 0f) {
                        p.y           = waterY - 1f
                        p.vy          = -p.vy * BackgroundConfig.BOUNCE_DAMP
                        p.vx         *= 0.88f
                        p.bounceCount ++
                    }
                    // Wrap/bounce walls
                    if (p.x < -90f)            { p.x  =  -90f;          p.vx =  abs(p.vx) * 0.72f }
                    if (p.x > screenW + 90f)   { p.x  = screenW + 90f;  p.vx = -abs(p.vx) * 0.72f }

                    if (p.bounceCount > p.maxBounces || p.y > screenH + 120f) dead += p
                }
                particles.removeAll(dead.toSet())
                val deficit = (BackgroundConfig.MIN_PARTICLES - particles.size).coerceAtLeast(0)
                repeat(deficit) { particles += randomParticle(screenW, floatBand) }
            }
        }
    }

    /* ── animation clock ─────────────────────────────────────────────────── */
    val infinite = rememberInfiniteTransition(label = "bg")
    val timeSec  by infinite.animateFloat(
        0f, 3600f,
        infiniteRepeatable(tween(3_600_000, easing = LinearEasing)),
        label = "t"
    )

    /* ── tap → emoji burst ───────────────────────────────────────────────── */
    val tapMod = Modifier.pointerInput(Unit) {
        detectTapGestures { tap ->
            val burst = minOf(8, BackgroundConfig.MAX_PARTICLES - particles.size).coerceAtLeast(2)
            repeat(burst) { i ->
                val angle = (i * (360f / burst.toFloat()) + Random.nextFloat() * 35f) *
                        (PI / 180.0).toFloat()
                val speed = 420f + Random.nextFloat() * 580f
                particles += Particle(
                    x          = tap.x,
                    y          = tap.y,
                    vx         = cos(angle) * speed,
                    vy         = sin(angle) * speed - 320f,
                    emoji      = EMOJI_POOL.random(),
                    textSizePx = 40f + Random.nextFloat() * 26f,
                )
            }
        }
    }

    /* ── canvas ──────────────────────────────────────────────────────────── */
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .onSizeChanged { sz ->
                screenW = sz.width.toFloat()
                screenH = sz.height.toFloat()
            }
            .then(tapMod)
    ) {
        val t = timeSec
        val w = size.width
        val h = size.height

        drawSky        (t, w, h)
        drawStars      (t, w, h)
        drawMoonGlow   (    w, h)
        drawMountains  (t, w, h)
//        drawPixelWater (t, w, h)
        drawIntoCanvas { c -> paintEmojis(c.nativeCanvas, particles) }
//        drawScanlines  (w, h)
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  DRAW  ①  SKY — deep retro night gradient
// ════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawSky(t: Float, w: Float, h: Float) {
    // Slowly breathes between two deep night tones
    val pulse = (sin(t * 0.04f) * 0.5f + 0.5f) * 0.08f
    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF03060F),
                0.25f to Color(0xFF060D1E).copy(alpha = 1f - pulse),
                0.55f to Color(0xFF0A1530),
                0.80f to Color(0xFF0C1828),
                1.00f to Color(0xFF070D1C),
            ),
            startY = 0f, endY = h
        )
    )
}

// ════════════════════════════════════════════════════════════════════════════
//  DRAW  ②  STARS — parallax twinkling + big sparkle crosses
// ════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawStars(t: Float, w: Float, h: Float) {
    val cap = h * 0.62f

    // Two parallax planes
    for (plane in 0..1) {
        val count  = if (plane == 0) 90 else 40
        val scroll = (t * if (plane == 0) 0.3f else 0.7f) % w

        repeat(count) { i ->
            val sx = ((i * 97L + plane * 53L + 11L) % w.toLong()).toFloat()
            val sy = ((i * 193L + plane * 17L + 7L) % cap.toLong()).toFloat()
            val parallaxX = (sx - scroll + w) % w

            val twinkle = (sin(t * 0.9f + i * 0.77f + plane) * 0.5f + 0.5f).toFloat()
            val alpha   = 0.18f + twinkle * 0.82f
            val r       = when {
                i % 18 == 0 -> 2.8f
                i % 6  == 0 -> 1.8f
                else         -> 1.1f
            }

            if (i % 18 == 0) {
                // Sparkle cross
                drawCircle(Color.White.copy(alpha = alpha * 0.95f), r * 1.6f, Offset(parallaxX, sy))
                val arm = r * 4.5f
                drawLine(Color.White.copy(alpha = alpha * 0.35f), Offset(parallaxX - arm, sy),    Offset(parallaxX + arm, sy),    1.5f)
                drawLine(Color.White.copy(alpha = alpha * 0.35f), Offset(parallaxX, sy - arm),    Offset(parallaxX, sy + arm),    1.5f)
                drawLine(Color.White.copy(alpha = alpha * 0.18f), Offset(parallaxX - arm * 0.6f, sy - arm * 0.6f), Offset(parallaxX + arm * 0.6f, sy + arm * 0.6f), 1f)
                drawLine(Color.White.copy(alpha = alpha * 0.18f), Offset(parallaxX + arm * 0.6f, sy - arm * 0.6f), Offset(parallaxX - arm * 0.6f, sy + arm * 0.6f), 1f)
            } else {
                drawCircle(Color.White.copy(alpha = alpha), r, Offset(parallaxX, sy))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  DRAW  ③  MOON GLOW — crescent with halo bloom
// ════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawMoonGlow(w: Float, h: Float) {
    val mx = w * 0.77f
    val my = h * 0.10f
    val mr = h * 0.042f

    // Outer halo bloom (two radial layers)
    listOf(mr * 6f to 0x18E8D4A0.toInt(), mr * 3.5f to 0x30E8D4A0.toInt()).forEach { (r, argb) ->
        drawCircle(
            brush  = Brush.radialGradient(
                colors = listOf(Color(argb.toLong()), Color(0x00E8D4A0)),
                center = Offset(mx, my), radius = r
            ),
            radius = r, center = Offset(mx, my)
        )
    }
    // Moon disk
    drawCircle(Color(0xFFE4CFA0), mr, Offset(mx, my))
    // Crescent shadow (offset disk)
    drawCircle(Color(0xFF08060F).copy(alpha = 0.62f), mr * 0.86f, Offset(mx + mr * 0.38f, my - mr * 0.08f))
}

// ════════════════════════════════════════════════════════════════════════════
//  DRAW  ④  MOUNTAINS — parallax pixel-stepped silhouettes
// ════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawMountains(t: Float, w: Float, h: Float) {
    MOUNTAIN_LAYERS.forEachIndexed { idx, layer ->
        val path    = Path()
        val baseY   = h * layer.yFrac
        val scroll  = (t * BackgroundConfig.MTN_SCROLL[layer.scrollIdx] * w) % (w * 2f)
        val step    = BackgroundConfig.PIXEL_STEP

        path.moveTo(-scroll - step, h)

        var x = -scroll - step
        while (x <= w + step) {
            val nx = x / w
            // Sum of 3 harmonics → natural-looking ridgeline
            val raw = baseY - h * layer.amplitude * (
                    0.50f * sin(nx * PI.toFloat() * layer.freq * 2.0f + layer.seed)
                            + 0.30f * sin(nx * PI.toFloat() * layer.freq * 5.3f + layer.seed * 1.7f)
                            + 0.20f * sin(nx * PI.toFloat() * layer.freq * 11.0f + layer.seed * 3.1f)
                    )
            // Pixel-art quantise
            val sy = (raw / step).toInt() * step
            path.lineTo(x, sy.toFloat())
            x += step
        }

        path.lineTo(w + step, h)
        path.close()

        drawPath(
            path  = path,
            brush = Brush.verticalGradient(
                colors = listOf(layer.lightCol, layer.darkCol),
                startY = h * (layer.yFrac - layer.amplitude * 1.1f),
                endY   = h,
            )
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  DRAW  ⑤  PIXEL WATER — palette cycling + dithered edge + shimmer
// ════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawPixelWater(t: Float, w: Float, h: Float) {
    val waterTop = h * BackgroundConfig.WATER_Y_FRAC
    val stripe   = BackgroundConfig.STRIPE_H
    val dw       = BackgroundConfig.DITHER_W
    val palIdx   = ((t * BackgroundConfig.PAL_SPEED) % WATER_PALETTES.size.toFloat()).toInt()
        .coerceIn(0, WATER_PALETTES.lastIndex)
    val pal      = WATER_PALETTES[palIdx]

    // ── Dithered wave edge ────────────────────────────────────────────────
    var dx = 0f
    while (dx < w) {
        val waveY = waterTop +
                sin((dx * 0.022f) + t * 1.6f) * 10f +
                sin((dx * 0.011f) + t * 0.7f) *  6f
        val ditherAlpha =
            if (((dx / dw).toInt() + ((waveY - waterTop) / stripe).toInt()) % 2 == 0) 0.95f else 0.22f
        drawRect(
            color   = pal[0].copy(alpha = ditherAlpha),
            topLeft = Offset(dx, waveY - stripe),
            size    = Size(dw, stripe * 1.5f),
        )
        dx += dw
    }

    // ── Horizontal colour bands ───────────────────────────────────────────
    var y    = waterTop
    var band = 0
    while (y < h) {
        val bandColor = pal[band % pal.size]

        // Per-band wave undulation
        val waveShift = sin((band * 0.4f) + t * 1.1f) * 3.5f +
                sin((band * 0.2f) + t * 0.5f) * 2.0f

        drawRect(
            color   = bandColor,
            topLeft = Offset(0f, y + waveShift),
            size    = Size(w, stripe + 1f),
        )

        // White shimmer every 4th band
        if (band % 4 == 1) {
            val sa = (sin(t * 2.2f + band * 0.7f) * 0.5f + 0.5f).toFloat() * 0.22f
            drawRect(
                color   = Color.White.copy(alpha = sa),
                topLeft = Offset(0f, y + waveShift),
                size    = Size(w, stripe * 0.4f),
            )
        }

        // Subtle foam highlight on top 3 bands
        if (band < 3) {
            val fa = (0.12f - band * 0.04f).coerceAtLeast(0f)
            drawRect(
                color   = Color(0xFF9FD8FF).copy(alpha = fa),
                topLeft = Offset(0f, y + waveShift),
                size    = Size(w, stripe),
            )
        }

        y    += stripe
        band ++
    }

    // ── Horizon fog blend ─────────────────────────────────────────────────
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(WATER_FOG.copy(alpha = 0.55f), Color.Transparent),
            startY = waterTop,
            endY   = waterTop + h * 0.07f,
        )
    )
}

// ════════════════════════════════════════════════════════════════════════════
//  DRAW  ⑥  EMOJI PARTICLES (native Android canvas for emoji rendering)
// ════════════════════════════════════════════════════════════════════════════

private fun paintEmojis(canvas: android.graphics.Canvas, particles: List<Particle>) {
    val paint = android.graphics.Paint().apply { isAntiAlias = true }
    particles.forEach { p ->
        canvas.save()
        canvas.translate(p.x, p.y)
        canvas.rotate(p.rotation)
        paint.textSize = p.textSizePx
        paint.alpha    = 255
        // Centre the emoji on its anchor point
        canvas.drawText(p.emoji, -p.textSizePx * 0.5f, p.textSizePx * 0.45f, paint)
        canvas.restore()
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  DRAW  ⑦  CRT SCAN-LINES — optional retro overlay
// ════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawScanlines(w: Float, h: Float) {
    var y = 0f
    while (y < h) {
        drawRect(
            color   = Color.Black.copy(alpha = BackgroundConfig.SCANLINE_ALPHA),
            topLeft = Offset(0f, y),
            size    = Size(w, 1.5f),
        )
        y += BackgroundConfig.SCANLINE_GAP
    }
}

