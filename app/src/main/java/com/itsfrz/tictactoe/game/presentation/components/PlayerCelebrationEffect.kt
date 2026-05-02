package com.itsfrz.tictactoe.game.presentation.components

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║        PLAYER CELEBRATION EFFECT  —  Retro × Candy-Crush Style           ║
 * ║                                                                           ║
 * ║  Per-player feel-good burst system with 8-bit synthesised sound          ║
 * ║  (No asset files needed — sound generated entirely in code)              ║
 * ║                                                                           ║
 * ║  Player 1 ▶ FIRE  theme  · warm ascending arpeggio  · fire gems         ║
 * ║  Player 2 ▶ ICE   theme  · cool descending arpeggio · crystal gems      ║
 * ║  Win      ▶ RAINBOW storm  ·  victory fanfare                            ║
 * ║  Draw     ▶ Silver shimmer  ·  minor descend                             ║
 * ║                                                                           ║
 * ║  ── Quick-start ──────────────────────────────────────────────────────── ║
 * ║                                                                           ║
 * ║    val celebration = rememberCelebrationState()                           ║
 * ║                                                                           ║
 * ║    // fire when a move happens (pass Offset of the cell's centre):        ║
 * ║    celebration.triggerMove(player = 1, anchor = cellOffset)              ║
 * ║    celebration.triggerWin (player = 1, lineCenters = listOf(...))        ║
 * ║    celebration.triggerDraw()                                              ║
 * ║                                                                           ║
 * ║    Box {                                                                  ║
 * ║        AnimatedGameBackground(Modifier.matchParentSize())                 ║
 * ║        YourGameGrid(...)                                                  ║
 * ║        PlayerCelebrationOverlay(celebration, Modifier.matchParentSize())  ║
 * ║    }                                                                      ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

import android.content.res.Resources
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

// ════════════════════════════════════════════════════════════════════════════
//  ①  PLAYER THEMES
// ════════════════════════════════════════════════════════════════════════════

data class PlayerTheme(
    val primary:    Color,
    val secondary:  Color,
    val accent:     Color,
    val gemPalette: List<Color>,
    val moveNotes:  IntArray,        // Hz — played as ascending/descending arpeggio
    val wordPool:   List<String>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerTheme

        if (primary != other.primary) return false
        if (secondary != other.secondary) return false
        if (accent != other.accent) return false
        if (gemPalette != other.gemPalette) return false
        if (!moveNotes.contentEquals(other.moveNotes)) return false
        if (wordPool != other.wordPool) return false

        return true
    }

    override fun hashCode(): Int {
        var result = primary.hashCode()
        result = 31 * result + secondary.hashCode()
        result = 31 * result + accent.hashCode()
        result = 31 * result + gemPalette.hashCode()
        result = 31 * result + moveNotes.contentHashCode()
        result = 31 * result + wordPool.hashCode()
        return result
    }
}

/** Player 1 — FIRE. Warm oranges, gold, crimson. Rising cheer. */
val PLAYER_1_THEME = PlayerTheme(
    primary    = Color(0xFFFF6B1A),
    secondary  = Color(0xFFFFCC00),
    accent     = Color(0xFFFF1A4A),
    gemPalette = listOf(
        Color(0xFFFF6B1A), Color(0xFFFFCC00), Color(0xFFFF1A4A),
        Color(0xFFFF8C42), Color(0xFFFFEB3B), Color(0xFFFF4500),
    ),
    moveNotes  = intArrayOf(523, 659, 784),          // C5 → E5 → G5  (warm, triumphant)
    wordPool   = listOf("NICE!", "HOT!", "RADICAL!", "GNARLY!", "BLAZING!", "SWEET!"),
)

/** Player 2 — ICE. Electric cyan, cobalt, white crystal. Cool precision. */
val PLAYER_2_THEME = PlayerTheme(
    primary    = Color(0xFF00E5FF),
    secondary  = Color(0xFF2979FF),
    accent     = Color(0xFFE0F7FA),
    gemPalette = listOf(
        Color(0xFF00E5FF), Color(0xFF2979FF), Color(0xFFE0F7FA),
        Color(0xFF40C4FF), Color(0xFF82B1FF), Color(0xFF00B0FF),
    ),
    moveNotes  = intArrayOf(784, 659, 523),          // G5 → E5 → C5  (cool, resolute)
    wordPool   = listOf("COOL!", "ICY!", "CRISP!", "SLICK!", "CHILL!", "SHARP!"),
)

private val WIN_PALETTE = listOf(
    Color(0xFFFF1A4A), Color(0xFFFF6B1A), Color(0xFFFFCC00),
    Color(0xFF00E5FF), Color(0xFF2979FF), Color(0xFF9C27B0),
    Color(0xFF00E676), Color(0xFFFFEB3B), Color(0xFFFF80AB),
)
private val WIN_NOTES   = intArrayOf(523, 659, 784, 880, 1047)  // C-E-G-A-C6  victory fanfare
private val DRAW_NOTES  = intArrayOf(880, 784, 659, 494)         // A-G-E-B  gentle minor
private val WIN_WORDS   = listOf("VICTORY!", "CHAMPION!", "LEGENDARY!", "EPIC!", "FLAWLESS!")
private val DRAW_WORDS  = listOf("DRAW!", "CLOSE ONE!", "WELL PLAYED!", "NICE TRY!")

private val DEG = (PI / 180.0).toFloat()

// ════════════════════════════════════════════════════════════════════════════
//  ②  PARTICLE DATA
// ════════════════════════════════════════════════════════════════════════════

enum class PType { GEM, SPARK, CONFETTI, STAR }

class CParticle(
    var x: Float,  var y: Float,
    var vx: Float, var vy: Float,
    val type:      PType,
    val color:     Color,
    val size:      Float,
    var rotation:  Float  = Random.nextFloat() * 360f,
    var life:      Float  = 1f,           // 1 → 0 over particle lifetime
    val decay:     Float  = 0.007f + Random.nextFloat() * 0.010f,
    var bounces:   Int    = 0,
    val maxBounces: Int   = Random.nextInt(0, 3),
)

class CRing(
    val cx: Float, val cy: Float,
    var radius: Float = 0f,
    val maxR:   Float,
    val color:  Color,
    var life:   Float = 1f,
    val speed:  Float = 580f + Random.nextFloat() * 220f,
    val thick:  Float = 3f + Random.nextFloat() * 3f,
)

class CTextPop(
    val text:  String,
    val cx:    Float,
    var y:     Float,
    val color: Color,
    val size:  Float = 54f,
    var scale: Float = 0.05f,
    var vy:    Float = -260f,
    var life:  Float = 1f,
)

// ════════════════════════════════════════════════════════════════════════════
//  ③  CELEBRATION STATE  (public API surface)
// ════════════════════════════════════════════════════════════════════════════

class CelebrationState {
    internal val particles  = mutableStateListOf<CParticle>()
    internal val rings      = mutableStateListOf<CRing>()
    internal val textPops   = mutableStateListOf<CTextPop>()
    internal var flashColor by mutableStateOf(Color.Transparent)
    internal var flashAlpha by mutableFloatStateOf(0f)
    internal var screenW    = 0f
    internal var screenH    = 0f

    // ── Public triggers ────────────────────────────────────────────────────

    /** Call when a player places a piece. anchor = pixel centre of the tapped cell. */
    fun triggerMove(player: Int) {
        val density = Resources.getSystem().displayMetrics.density
        val getRandomHeight = listOf(500f,760f,620f,550f,680F).random()
        val anchor = Offset(
            x = screenW / 2f,
            y = screenH - (getRandomHeight * density)
        )

        val theme = player.theme()
        spawnGemBurst (anchor, theme.gemPalette, count = 14)
        spawnSparks   (anchor, theme.primary,    count = 20)
        spawnRing     (anchor, theme.primary, maxR = 190f)
        spawnRing     (anchor, theme.secondary, maxR = 130f, delay = 0.06f)
        spawnTextPop  (theme.wordPool.random(), anchor, theme.secondary)
        flashScreen   (theme.primary, alpha = 0.22f)
        RetroSoundEngine.play(theme.moveNotes, SoundShape.SQUARE, noteDuration = 0.090f)
    }

    /** Call when a player wins. lineCenters = pixel centres of the 3+ winning cells. */
    fun triggerWin(player: Int, lineCenters: List<Offset> = emptyList()) {
        val theme  = player.theme()
        val midX   = if (lineCenters.isEmpty()) screenW / 2f
        else lineCenters.map { it.x }.average().toFloat()
        val midY   = if (lineCenters.isEmpty()) screenH / 2f
        else lineCenters.map { it.y }.average().toFloat()
        val centre = Offset(midX, midY)

        spawnConfettiRain(WIN_PALETTE, count = 70)
        lineCenters.forEach { spawnGemBurst(it, WIN_PALETTE, count = 10) }
        spawnGemBurst(centre, WIN_PALETTE, count = 22)
        repeat(4) { i ->
            spawnRing(centre, WIN_PALETTE[i % WIN_PALETTE.size],
                maxR = 220f + i * 90f, delay = i * 0.08f)
        }
        spawnTextPop(WIN_WORDS.random(), centre, theme.primary, size = 74f)
        flashScreen(theme.primary, alpha = 0.38f)
        RetroSoundEngine.play(WIN_NOTES, SoundShape.SQUARE, noteDuration = 0.11f)
    }

    /** Call on a draw. */
    fun triggerDraw() {
        val silver = listOf(Color(0xFFB0BEC5), Color(0xFF78909C), Color(0xFFECEFF1), Color(0xFF90A4AE))
        val centre = Offset(screenW / 2f, screenH / 1f)
        spawnGemBurst(centre, silver, count = 18)
        spawnSparks  (centre, Color(0xFFB0BEC5), count = 14)
        spawnRing    (centre, Color(0xFFB0BEC5), maxR = 260f)
        spawnTextPop (DRAW_WORDS.random(), centre, Color(0xFFCFD8DC), size = 60f)
        flashScreen  (Color(0xFF78909C), alpha = 0.22f)
        RetroSoundEngine.play(DRAW_NOTES, SoundShape.SINE, noteDuration = 0.13f)
    }

    // ── Internal spawn helpers ─────────────────────────────────────────────
    private fun spawnGemBurst(at: Offset, palette: List<Color>, count: Int) {
        repeat(count) { i ->
            val angle = (i * (360f / count) + Random.nextFloat() * 28f) * DEG
            val speed = 320f + Random.nextFloat() * 520f
            particles += CParticle(
                x = at.x, y = at.y,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 180f,
                type  = PType.GEM,
                color = palette.random(),
                size  = 16f + Random.nextFloat() * 20f,
                decay = 0.005f + Random.nextFloat() * 0.007f,
            )
        }
    }

    private fun spawnSparks(at: Offset, color: Color, count: Int) {
        repeat(count) {
            val angle = Random.nextFloat() * 360f * DEG
            val speed = 130f + Random.nextFloat() * 380f
            particles += CParticle(
                x = at.x, y = at.y,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                type  = PType.SPARK,
                color = color,
                size  = 3f + Random.nextFloat() * 7f,
                decay = 0.014f + Random.nextFloat() * 0.020f,
            )
        }
    }

    private fun spawnConfettiRain(palette: List<Color>, count: Int) {
        repeat(count) {
            particles += CParticle(
                x = Random.nextFloat() * screenW,
                y = -30f - Random.nextFloat() * 280f,
                vx = (Random.nextFloat() - 0.5f) * 110f,
                vy = 180f + Random.nextFloat() * 340f,
                type      = PType.CONFETTI,
                color     = palette.random(),
                size      = 9f + Random.nextFloat() * 13f,
                maxBounces = 0,
                decay     = 0.003f + Random.nextFloat() * 0.005f,
            )
        }
    }

    private fun spawnRing(at: Offset, color: Color, maxR: Float, delay: Float = 0f) {
        // Simple: treat delay as pre-advancing radius (no timer needed)
        rings += CRing(at.x, at.y, radius = delay * 580f, maxR = maxR, color = color)
    }

    private fun spawnTextPop(text: String, at: Offset, color: Color, size: Float = 54f) {
        textPops += CTextPop(text, cx = at.x, y = at.y, color = color, size = size)
    }

    private fun flashScreen(color: Color, alpha: Float) {
        flashColor = color
        flashAlpha = alpha
    }

    private fun Int.theme() = if (this == 1) PLAYER_1_THEME else PLAYER_2_THEME
}

@Composable
fun rememberCelebrationState(): CelebrationState = remember { CelebrationState() }

// ════════════════════════════════════════════════════════════════════════════
//  ④  8-BIT RETRO SOUND ENGINE  — zero assets, pure synthesis
// ════════════════════════════════════════════════════════════════════════════

/**
 * Generates PCM samples for retro 8-bit waveforms and plays them via
 * AudioTrack on a dedicated IO coroutine. No permissions required.
 *
 * Waveforms:
 *   SQUARE   → classic Game Boy / NES chiptune
 *   SINE     → softer, SNES-style
 *   TRIANGLE → Atari-style mellow lead
 */
private enum class SoundShape { SQUARE, SINE, TRIANGLE }


private object RetroSoundEngine {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun play(
        freqsHz: IntArray,
        shape: SoundShape,
        noteDuration: Float = 0.11f,   // slightly longer = less sharp
        volume: Float = 0.28f,         // reduced default volume
    ) {
        scope.launch {
            val sampleRate = 44100

            freqsHz.forEach { freq ->
                val sampleCount = (sampleRate * noteDuration).toInt()
                val buf = ShortArray(sampleCount)

                var prevSample = 0f

                for (i in 0 until sampleCount) {
                    val t = i.toFloat() / sampleRate
                    val phase = (t * freq) % 1f

                    // Softer ADSR-style envelope (less punchy)
                    val attackTime = 0.01f
                    val releaseStart = noteDuration * 0.65f

                    val env = when {
                        t < attackTime ->
                            (t / attackTime).coerceIn(0f, 1f)

                        t > releaseStart ->
                            exp(-((t - releaseStart) * 8f)) // gentle fade out

                        else -> 1f
                    }

                    // Soft waveform shaping (reduces harsh harmonics)
                    val rawWave = when (shape) {
                        SoundShape.SQUARE -> {
                            val s = sin(2f * PI.toFloat() * freq * t)
                            // soft square (sigmoid-ish shaping)
                            tanh(s * 2.2f)
                        }

                        SoundShape.SINE -> {
                            sin(2f * PI.toFloat() * freq * t)
                        }

                        SoundShape.TRIANGLE -> {
                            val tri = abs(phase - 0.5f) * 4f - 1f
                            // soften edges
                            sin(tri * (PI.toFloat() / 2f))
                        }
                    }

                    // gentle smoothing (simple low-pass feel)
                    val smoothed = (rawWave * 0.6f) + (prevSample * 0.4f)
                    prevSample = smoothed

                    // final output with soft limiting
                    val sample = smoothed * env * volume

                    val softClipped = tanh(sample) // prevents ear-piercing peaks

                    buf[i] = (softClipped * Short.MAX_VALUE)
                        .toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        .toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buf.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                try {
                    track.write(buf, 0, buf.size)
                    track.play()

                    delay((noteDuration * 1000f).toLong())
                } finally {
                    track.stop()
                    track.release()
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  ⑤  OVERLAY COMPOSABLE
// ════════════════════════════════════════════════════════════════════════════

@Composable
fun PlayerCelebrationOverlay(
    state:    CelebrationState,
    modifier: Modifier = Modifier,
) {
    // ── Physics / lifecycle loop ──────────────────────────────────────────
    LaunchedEffect(Unit) {
        val gravity    = 580f
        val dragH      = 0.42f
        val dragV      = 0.07f
        val bounceDamp = 0.46f

        var prevNs = 0L
        while (isActive) {
            withFrameNanos { ns ->
                val dt = if (prevNs == 0L) 0.016f
                else ((ns - prevNs) / 1_000_000_000f).coerceIn(0f, 0.05f)
                prevNs = ns

                val w = state.screenW
                val h = state.screenH

                // ── particles ──────────────────────────────────────────────
                val dead = ArrayList<CParticle>(8)
                state.particles.forEach { p ->
                    // Integrate forces
                    p.vy += gravity * dt
                    p.vx *= (1f - dragH * dt)
                    p.vy *= (1f - dragV * dt)
                    p.x  += p.vx * dt
                    p.y  += p.vy * dt
                    p.rotation += p.vx * 0.28f * dt
                    p.life -= p.decay

                    // Floor bounce (gems / confetti only — sparks vanish)
                    if (p.type != PType.SPARK && p.y >= h - 16f && p.vy > 0f) {
                        p.y      = h - 16f
                        p.vy     = -p.vy * bounceDamp
                        p.vx    *= 0.80f
                        p.bounces++
                    }
                    // Cull
                    if (p.life <= 0f ||
                        (p.bounces > p.maxBounces && p.y > h) ||
                        p.x < -120f || p.x > w + 120f
                    ) dead += p
                }
                state.particles.removeAll(dead.toSet())

                // ── rings ──────────────────────────────────────────────────
                val deadR = ArrayList<CRing>(4)
                state.rings.forEach { r ->
                    r.radius += r.speed * dt
                    r.life    = 1f - (r.radius / r.maxR).coerceIn(0f, 1f)
                    if (r.life <= 0f) deadR += r
                }
                state.rings.removeAll(deadR.toSet())

                // ── text pops ──────────────────────────────────────────────
                val deadT = ArrayList<CTextPop>(4)
                state.textPops.forEach { tp ->
                    // Spring scale: snaps to 1, mild overshoot
                    val springK = 10f
                    tp.scale += (1f - tp.scale) * (springK * dt).coerceIn(0f, 1f)
                    tp.vy    *= (1f - 3.5f * dt)
                    tp.y     += tp.vy * dt
                    tp.life  -= 0.006f
                    if (tp.life <= 0f) deadT += tp
                }
                state.textPops.removeAll(deadT.toSet())

                // ── screen flash decay ─────────────────────────────────────
                if (state.flashAlpha > 0f) {
                    state.flashAlpha = (state.flashAlpha - 2.2f * dt).coerceAtLeast(0f)
                }
            }
        }
    }

    // ── Canvas ────────────────────────────────────────────────────────────
    Canvas(
        modifier = modifier.onSizeChanged { sz ->
            state.screenW = sz.width.toFloat()
            state.screenH = sz.height.toFloat()
        }
    ) {
        // Screen flash
        if (state.flashAlpha > 0.01f) {
            drawRect(state.flashColor.copy(alpha = state.flashAlpha))
        }

        // Rings — drawn first (behind particles)
        state.rings.forEach { drawRing(it) }

        // Particles
        state.particles.forEach { p ->
            when (p.type) {
                PType.GEM  -> drawGem(p)
                PType.SPARK -> drawSpark(p)
                PType.STAR -> drawStar(p)
                PType.CONFETTI -> { /* handled on native canvas below */ }
            }
        }

        // Confetti & text need native canvas (rotation / font control)
        drawIntoCanvas { c ->
            state.particles
                .filter { it.type == PType.CONFETTI }
                .forEach { drawConfetti(c.nativeCanvas, it) }
            state.textPops
                .forEach { drawTextPop(c.nativeCanvas, it) }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  ⑥  INDIVIDUAL DRAW FUNCTIONS
// ════════════════════════════════════════════════════════════════════════════

/**
 * Pixel-art octagonal gem — Candy Crush–style.
 * Positions are quantised to 4px grid for that retro chunky feel.
 */
private fun DrawScope.drawGem(p: CParticle) {
    val a  = p.life.coerceIn(0f, 1f)
    val s  = p.size * p.life.coerceIn(0.35f, 1f)   // shrinks as it dies
    val cut = s * 0.30f
    // Pixel-grid snap
    val qx = (p.x / 4f).toInt() * 4f
    val qy = (p.y / 4f).toInt() * 4f

    val body = Path().apply {
        moveTo(qx - s + cut, qy - s)
        lineTo(qx + s - cut, qy - s)
        lineTo(qx + s,       qy - s + cut)
        lineTo(qx + s,       qy + s - cut)
        lineTo(qx + s - cut, qy + s)
        lineTo(qx - s + cut, qy + s)
        lineTo(qx - s,       qy + s - cut)
        lineTo(qx - s,       qy - s + cut)
        close()
    }
    drawPath(body, p.color.copy(alpha = a * 0.95f))

    // Top-left shine triangle
    val shine = Path().apply {
        moveTo(qx - s + cut, qy - s)
        lineTo(qx + s * 0.1f, qy - s)
        lineTo(qx - s,       qy + s * 0.1f)
        lineTo(qx - s,       qy - s + cut)
        close()
    }
    drawPath(shine, Color.White.copy(alpha = a * 0.48f))

    // Bottom-right shadow
    val shadow = Path().apply {
        moveTo(qx + s - cut, qy + s)
        lineTo(qx - s * 0.1f, qy + s)
        lineTo(qx + s,       qy - s * 0.1f)
        lineTo(qx + s,       qy + s - cut)
        close()
    }
    drawPath(shadow, Color.Black.copy(alpha = a * 0.28f))
}

/**
 * Tiny square pixel spark — raw pixel, ultra-retro.
 */
private fun DrawScope.drawSpark(p: CParticle) {
    val a = p.life.coerceIn(0f, 1f)
    val s = p.size * a
    val qx = (p.x / 2f).toInt() * 2f
    val qy = (p.y / 2f).toInt() * 2f
    drawRect(
        color   = p.color.copy(alpha = a),
        topLeft = Offset(qx - s * 0.5f, qy - s * 0.5f),
        size    = Size(s, s),
    )
}

/**
 * 8-pointed pixel star.
 */
private fun DrawScope.drawStar(p: CParticle) {
    val a     = p.life.coerceIn(0f, 1f)
    val outer = p.size * a
    val inner = outer * 0.40f
    val path  = Path()
    for (i in 0 until 8) {
        val angle = (i * 45f - p.rotation) * DEG
        val r     = if (i % 2 == 0) outer else inner
        val px    = p.x + cos(angle) * r
        val py    = p.y + sin(angle) * r
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, p.color.copy(alpha = a * 0.95f))
    // Bright centre dot
    drawCircle(Color.White.copy(alpha = a * 0.7f), outer * 0.15f, Offset(p.x, p.y))
}

/**
 * Rotating confetti rectangle — needs native canvas for rotation.
 */
private fun drawConfetti(canvas: android.graphics.Canvas, p: CParticle) {
    val a     = (p.life.coerceIn(0f, 1f) * 255).toInt()
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(
            a,
            (p.color.red   * 255).toInt(),
            (p.color.green * 255).toInt(),
            (p.color.blue  * 255).toInt(),
        )
    }
    canvas.save()
    canvas.translate(p.x, p.y)
    canvas.rotate(p.rotation)
    // Alternating thin/wide for variety
    val hw = p.size * 0.50f
    val hh = p.size * 0.22f
    canvas.drawRect(-hw, -hh, hw, hh, paint)
    canvas.restore()
}

/**
 * Expanding ripple ring — double-ring for richness.
 */
private fun DrawScope.drawRing(r: CRing) {
    val a  = r.life.coerceIn(0f, 1f)
    val sw = r.thick * a
    val center = Offset(r.cx, r.cy)
    // Outer ring
    drawCircle(
        color  = r.color.copy(alpha = a * 0.75f),
        radius = r.radius,
        center = center,
        style  = Stroke(sw),
    )
    // Inner softer echo
    if (r.radius > sw * 3f) {
        drawCircle(
            color  = r.color.copy(alpha = a * 0.28f),
            radius = (r.radius - sw * 2.5f).coerceAtLeast(0f),
            center = center,
            style  = Stroke(sw * 0.5f),
        )
    }
    // Glow bloom (filled, very transparent)
    drawCircle(
        brush  = Brush.radialGradient(
            colors = listOf(r.color.copy(alpha = a * 0.10f), Color.Transparent),
            center = center,
            radius = (r.radius + sw * 4f).coerceAtLeast(1f),
        ),
        radius = (r.radius + sw * 4f).coerceAtLeast(1f),
        center = center,
    )
}

/**
 * Retro word pop — bold monospace with dark outline for readability
 * over any background. Springs in, floats up, fades out.
 */
private fun drawTextPop(canvas: android.graphics.Canvas, tp: CTextPop) {
    val alpha     = (tp.life.coerceIn(0f, 1f) * 255).toInt()
    val scaledSz  = tp.size * tp.scale.coerceIn(0.01f, 1.3f)

    val outlinePaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize    = scaledSz
        textAlign   = android.graphics.Paint.Align.CENTER
        typeface    = android.graphics.Typeface.create(
            android.graphics.Typeface.MONOSPACE,
            android.graphics.Typeface.BOLD,
        )
        color       = android.graphics.Color.argb(alpha, 8, 4, 18)
        strokeWidth = scaledSz * 0.14f
        style       = android.graphics.Paint.Style.STROKE
        strokeJoin  = android.graphics.Paint.Join.ROUND
        strokeCap   = android.graphics.Paint.Cap.ROUND
    }
    val fillPaint = android.graphics.Paint(outlinePaint).apply {
        color = android.graphics.Color.argb(
            alpha,
            (tp.color.red   * 255).toInt(),
            (tp.color.green * 255).toInt(),
            (tp.color.blue  * 255).toInt(),
        )
        style = android.graphics.Paint.Style.FILL
    }
    // Shadow layer
    val shadowPaint = android.graphics.Paint(outlinePaint).apply {
        color       = android.graphics.Color.argb((alpha * 0.4f).toInt(), 0, 0, 0)
        strokeWidth = scaledSz * 0.06f
        style       = android.graphics.Paint.Style.FILL
    }
    canvas.drawText(tp.text, tp.cx + scaledSz * 0.06f, tp.y + scaledSz * 0.06f, shadowPaint)
    canvas.drawText(tp.text, tp.cx, tp.y, outlinePaint)
    canvas.drawText(tp.text, tp.cx, tp.y, fillPaint)
}