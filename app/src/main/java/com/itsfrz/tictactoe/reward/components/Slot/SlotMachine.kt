package com.itsfrz.tictactoe.reward.components.Slot

/**
 * ═══════════════════════════════════════════════════════════════
 *  SLOT MACHINE  ·  Casino Edition
 *
 *  Files in this compilation unit:
 *   1. CoinRain        — physics-based coin particles with rotation + gravity
 *   2. ReelStrip       — scrolling symbol strip that blurs during spin
 *   3. SlotMachineBody — chrome cabinet, payline, win flash
 *   4. SlotScreen      — root composable wiring everything together
 *
 *  Design direction: High-roller Vegas — deep black lacquer, gold chrome,
 *  neon payline, tactile mechanical feel. Every animation timed to feel
 *  physical and weighty.
 * ═══════════════════════════════════════════════════════════════
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.itsfrz.tictactoe.common.usecase.CommonUseCase
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.reward.audio.SlotSoundManager
import com.itsfrz.tictactoe.reward.state.SlotNewSymbol
import com.itsfrz.tictactoe.reward.viewmodel.SlotNewViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

// ─────────────────────────────────────────────────────────────
//  Design tokens
// ─────────────────────────────────────────────────────────────

private val Gold        = Color(0xFFFFD700)
private val GoldDark    = Color(0xFFB8860B)
private val GoldLight   = Color(0xFFFFF0A0)
private val Chrome      = Color(0xFFD0D0D0)
private val ChromeDark  = Color(0xFF606060)
private val NeonRed     = Color(0xFFFF2244)
private val NeonGreen   = Color(0xFF00FF88)
private val CabinetBg   = Color(0xFF0A0A0A)
private val ReelBg      = Color(0xFF111116)
private val ReelBorder  = Color(0xFF2A2A35)

private val GoldGradient = Brush.linearGradient(
    listOf(GoldDark, Gold, GoldLight, Gold, GoldDark)
)

// ═══════════════════════════════════════════════════════════════
//  1.  COIN RAIN — physics particles
// ═══════════════════════════════════════════════════════════════

private data class CoinParticle(
    val x:        Float,       // 0..1 fraction of screen width
    val size:     Float,       // dp
    val delay:    Int,         // ms before launch
    val duration: Int,         // ms to fall
    val rotSpeed: Float,       // degrees per ms
    val wobble:   Float,       // horizontal wobble amplitude (px)
)

@Composable
fun CoinRain(
    scope: CoroutineScope,
    am: SlotSoundManager
) {
    val density     = LocalDensity.current
    val screenW     = LocalConfiguration.current.screenWidthDp.toFloat()
    val screenH     = LocalConfiguration.current.screenHeightDp.toFloat()

    val particles = remember {
        List(40) {
            CoinParticle(
                x        = Random.nextFloat(),
                size     = Random.nextFloat() * 16f + 20f,   // 20–36 dp
                delay    = Random.nextInt(0, 1200),
                duration = Random.nextInt(1400, 2600),
                rotSpeed = Random.nextFloat() * 0.4f - 0.2f,
                wobble   = Random.nextFloat() * 30f - 15f,
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        particles.forEach { p ->
            key(p) {
                val progress = remember { Animatable(0f) }
                LaunchedEffect(p) {
                    delay(p.delay.toLong())
                    scope.launch {
                        am.coinRain()
                    }
                    progress.animateTo(
                        1f,
                        tween(p.duration, easing = LinearEasing)
                    )

                }
                val t = progress.value
                val yDp = (-80f + (screenH + 120f) * t).dp          // above screen → below
                val xDp = (p.x * screenW + sin(t * PI.toFloat() * 3f) * p.wobble / density.density).dp
                val rot = t * p.duration * p.rotSpeed
                val scale = 1f - t * 0.3f   // slight shrink as it falls

                Text(
                    text     = "🪙",
                    fontSize = p.size.sp,
                    modifier = Modifier
                        .offset(x = xDp, y = yDp)
                        .graphicsLayer {
                            rotationZ  = rot
                            scaleX     = scale
                            scaleY     = scale
                            alpha      = (1f - (t - 0.8f).coerceAtLeast(0f) / 0.2f)
                        }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  2.  REEL STRIP — scrolling symbol with motion blur
// ═══════════════════════════════════════════════════════════════

@Composable
fun ReelStrip(
    symbol:    SlotNewSymbol,
    spinning:  Boolean,
    reelIndex: Int,
    modifier:  Modifier = Modifier,
) {
    // Stagger animation start by reel index
    val scope = rememberCoroutineScope()

    // Speed: 0 = stopped, 1 = full spin
    val speed = remember { Animatable(0f) }
    // Strip scroll offset (0..1 cycles through symbols)
    val offset = remember { Animatable(0f) }

    var displaySymbol by remember { mutableStateOf(symbol) }
    var settled       by remember { mutableStateOf(true) }

    LaunchedEffect(spinning) {
        if (spinning) {
            settled = false
            // Ramp up
            speed.animateTo(1f, tween(180 + reelIndex * 60, easing = FastOutSlowInEasing))
            // Continuous scroll while speed > 0
            scope.launch {
                while (speed.value > 0.01f) {
                    offset.snapTo((offset.value + speed.value * 0.08f) % 1f)
                    delay(16)
                }
            }
        } else {
            // Ramp down and snap to the new symbol
            speed.animateTo(0f, tween(260 + reelIndex * 80, easing = FastOutSlowInEasing))
            displaySymbol = symbol
            settled = true
        }
    }

    // Blur amount proportional to speed
    val blurPx   = speed.value * 18f
    val emojiScale = 1f + speed.value * 0.06f  // slight scale pulse during spin

    // Bounce scale on settle
    val settleScale = remember { Animatable(1f) }
    LaunchedEffect(settled) {
        if (settled && !spinning) {
            settleScale.animateTo(1.18f, tween(90, easing = FastOutSlowInEasing))
            settleScale.animateTo(1f,    tween(140, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = modifier
            .size(100.dp)
            .drawBehind { drawReelBackground(this) }
            .clip(RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Spinning ghost symbols (blur simulation via offset opacity layers)
        if (!settled) {
            val ghosts = listOf(
                SlotNewSymbol.entries[((SlotNewSymbol.entries.indexOf(displaySymbol) - 1 + SlotNewSymbol.entries.size) % SlotNewSymbol.entries.size)],
                SlotNewSymbol.entries[((SlotNewSymbol.entries.indexOf(displaySymbol) + 1) % SlotNewSymbol.entries.size)],
            )
            ghosts.forEachIndexed { i, ghost ->
                val ghostOffset = if (i == 0) (-50f * speed.value).dp else (50f * speed.value).dp
                Text(
                    text     = ghost.emoji,
                    fontSize = 42.sp,
                    modifier = Modifier
                        .offset(y = ghostOffset)
                        .alpha(speed.value * 0.35f)
                )
            }
        }

        // Main symbol
        Text(
            text     = displaySymbol.emoji,
            fontSize = 46.sp,
            modifier = Modifier
                .graphicsLayer {
                    scaleX    = settleScale.value * emojiScale
                    scaleY    = settleScale.value * emojiScale
                    alpha     = if (settled) 1f else (1f - speed.value * 0.3f)
                    // Vertical motion blur approximation
                    translationY = if (!settled) {
                        sin(offset.value * PI.toFloat() * 2f) * blurPx
                    } else 0f
                }
        )

        // Inner shadow top/bottom for depth
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f)
                            )
                        )
                    )
                }
        )
    }
}

private fun drawReelBackground(scope: DrawScope) {
    with(scope) {
        // Base dark
        drawRoundRect(
            color        = ReelBg,
            cornerRadius = CornerRadius(24.dp.toPx())
        )
        // Subtle inner glow
        drawRoundRect(
            brush = Brush.radialGradient(
                listOf(Color(0xFF1E1E2E), Color.Transparent),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = size.width * 0.7f
            ),
            cornerRadius = CornerRadius(24.dp.toPx())
        )
        // Chrome border
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Chrome.copy(alpha = 0.5f), ChromeDark.copy(alpha = 0.2f), Chrome.copy(alpha = 0.4f))
            ),
            cornerRadius = CornerRadius(24.dp.toPx()),
            style        = Stroke(width = 1.5.dp.toPx())
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  3.  SLOT MACHINE BODY  — chrome cabinet composable
// ═══════════════════════════════════════════════════════════════

@Composable
fun SlotMachineBody(
    reels:      List<SlotNewSymbol>,
    spinning:   List<Boolean>,
    lastWin:    Int,
    resultMsg:  String,
    modifier:   Modifier = Modifier,
    content:    @Composable () -> Unit,   // spin button + controls below
) {
    // Payline flash on win
    val paylineAlpha = remember { Animatable(0f) }
    val isWin = lastWin > 0

    LaunchedEffect(lastWin) {
        if (lastWin > 0) {
            repeat(5) {
                paylineAlpha.animateTo(1f, tween(120))
                paylineAlpha.animateTo(0f, tween(120))
            }
        }
    }

    Column(
        modifier          = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Machine frame ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .drawBehind { drawMachineFrame(this) }
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Marquee title ─────────────────────────────
                MarqueeTitle()

                // ── Reel window ───────────────────────────────
                Box {
                    // Window mask with chrome rim
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp)
                            .drawBehind { drawReelWindow(this) }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            reels.forEachIndexed { i, symbol ->
                                ReelStrip(
                                    symbol    = symbol,
                                    spinning  = spinning[i],
                                    reelIndex = i,
                                )
                            }
                        }
                    }

                    // Payline overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(2.dp)
                            .alpha(0.35f + paylineAlpha.value * 0.65f)
                            .background(
                                if (isWin)
                                    Brush.horizontalGradient(listOf(Color.Transparent, NeonGreen, NeonGreen, Color.Transparent))
                                else
                                    Brush.horizontalGradient(listOf(Color.Transparent, NeonRed.copy(alpha = 0.6f), NeonRed.copy(alpha = 0.6f), Color.Transparent))
                            )
                    )
                }

                // ── Win message ───────────────────────────────
                AnimatedContent(
                    targetState = resultMsg,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically { -it })
                            .togetherWith(fadeOut(tween(200)) + slideOutVertically { it })
                    },
                    label = "resultMsg"
                ) { msg ->
                    if (msg.isNotEmpty()) {
                        Text(
                            text      = msg,
                            color     = if (lastWin > 0) NeonGreen else Chrome.copy(alpha = 0.55f),
                            fontSize  = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.2.sp,
                            modifier  = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation     = if (lastWin > 0) 20.dp else 0.dp,
                                    spotColor     = NeonGreen,
                                    ambientColor  = NeonGreen,
                                    shape         = RoundedCornerShape(8.dp)
                                )
                        )
                    } else {
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        content()
    }
}

private fun drawMachineFrame(scope: DrawScope) {
    with(scope) {
        val r = CornerRadius(32.dp.toPx())
        // Outer cabinet
        drawRoundRect(
            brush        = Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D))),
            cornerRadius = r
        )
        // Chrome rim
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Chrome.copy(alpha = 0.7f), ChromeDark.copy(alpha = 0.25f), Chrome.copy(alpha = 0.5f)),
                start = Offset.Zero,
                end   = Offset(size.width, size.height)
            ),
            cornerRadius = r,
            style        = Stroke(2.dp.toPx())
        )
        // Top gold accent bar
        drawRoundRect(
            brush        = Brush.horizontalGradient(listOf(Color.Transparent, Gold.copy(alpha = 0.8f), Color.Transparent)),
            topLeft      = Offset(size.width * 0.15f, 0f),
            size         = Size(size.width * 0.70f, 3.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
    }
}

private fun drawReelWindow(scope: DrawScope) {
    with(scope) {
        val r = CornerRadius(20.dp.toPx())
        drawRoundRect(Color(0xFF080810), cornerRadius = r)
        // Inner chrome rim
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(ChromeDark.copy(alpha = 0.6f), Color.Transparent, Chrome.copy(alpha = 0.3f))
            ),
            cornerRadius = r,
            style        = Stroke(1.5.dp.toPx())
        )
        // Inner shadow top
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                endY = size.height * 0.35f
            ),
            cornerRadius = r
        )
    }
}

@Composable
private fun MarqueeTitle() {
    val infiniteAnim = rememberInfiniteTransition(label = "marquee")
    val glowAlpha by infiniteAnim.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .drawBehind {
                drawRoundRect(
                    brush        = GoldGradient,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style        = Stroke(1.5.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, Gold.copy(alpha = 0.08f), Color.Transparent)
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = "✦  SLOT  MASTER  ✦",
            color     = Gold.copy(alpha = glowAlpha),
            fontSize  = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            modifier  = Modifier.graphicsLayer {
                shadowElevation = 24f
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  4.  SPIN BUTTON + BET CONTROLS
// ═══════════════════════════════════════════════════════════════

@Composable
fun SpinButton(
    spinning:  Boolean,
    canSpin:   Boolean,
    betAmount: Int,
    onSpin:    () -> Unit,
    onBetUp:   () -> Unit,
    onBetDown: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    // Pulsing glow on idle
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )
    val glowAlpha by pulseAnim.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    // Press scale
    val pressScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bet selector
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBetDown()
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(Gold.copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, Gold.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Rounded.Remove, null, tint = Gold, modifier = Modifier.size(18.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("BET", color = Chrome.copy(alpha = 0.45f), fontSize = 9.sp, letterSpacing = 2.sp)
                Text(
                    text       = "🪙 $betAmount",
                    color      = Gold,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBetUp()
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(Gold.copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, Gold.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Rounded.Add, null, tint = Gold, modifier = Modifier.size(18.dp))
            }
        }

        // Spin button
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 64.dp)
                .graphicsLayer {
                    val s = if (!spinning && canSpin) pulseScale * pressScale.value else pressScale.value
                    scaleX = s; scaleY = s
                }
                .shadow(
                    elevation    = if (canSpin && !spinning) 24.dp else 4.dp,
                    shape        = CircleShape,
                    spotColor    = Gold.copy(alpha = if (canSpin) glowAlpha else 0f),
                    ambientColor = Gold.copy(alpha = 0.3f)
                )
                .background(
                    if (canSpin)
                        Brush.linearGradient(listOf(Color(0xFFB8860B), Color(0xFFFFD700), Color(0xFFFFF0A0), Color(0xFFFFD700), Color(0xFFB8860B)))
                    else
                        Brush.linearGradient(listOf(Color(0xFF333333), Color(0xFF444444))),
                    CircleShape
                )
                .clickable(enabled = canSpin && !spinning) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        pressScale.animateTo(0.93f, tween(80, easing = FastOutSlowInEasing))
                        pressScale.animateTo(1f,    tween(100, easing = FastOutSlowInEasing))
                    }

                    onSpin()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = if (spinning) "SPINNING…" else "SPIN",
                color      = if (canSpin) Color(0xFF3A2800) else Chrome.copy(alpha = 0.3f),
                fontSize   = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  5.  PAYTABLE STRIP  — quick symbol reference
// ═══════════════════════════════════════════════════════════════

@Composable
fun PaytableStrip() {
    val topSymbols = SlotNewSymbol.entries.takeLast(4)
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        topSymbols.forEach { sym ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(sym.emoji, fontSize = 20.sp)
                Text("×${sym.multiplier}", color = Gold.copy(alpha = 0.7f),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  6.  ROOT SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
fun SlotScreenRefined(
    vm: SlotNewViewModel,
    am : SlotSoundManager
) {


    val state by vm.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D0D12), Color(0xFF050508), Color(0xFF0D0D0A))
                )
            )
            .drawBehind { drawAmbientLights(this) }
    ) {
        // ── Coin rain ─────────────────────────────────────────
        AnimatedVisibility(
            visible = state.showCoins,
            enter   = fadeIn(tween(200)),
            exit    = fadeOut(tween(400))
        ) { CoinRain(scope,am) }

        // ── Main layout ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // Coin balance bar
            CoinBalanceBar(coins = state.coins, totalSpins = state.totalSpins)

            Spacer(Modifier.height(20.dp))

            // Machine body
            SlotMachineBody(
                reels     = state.reels,
                spinning  = state.spinning,
                lastWin   = state.lastWin,
                resultMsg = state.resultMsg,
            ) {
                // Spin button + bet
                SpinButton(
                    spinning  = state.spinning.any { it },
                    canSpin   = state.coins >= state.betAmount,
                    betAmount = state.betAmount,
                    onSpin    = {
                        scope.launch {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.spin()
                            am.spin(state.lastWin,state.resultMsg)
                        }
                    },
                    onBetUp   = { vm.changeBet(state.betAmount + 5) },
                    onBetDown = { vm.changeBet(state.betAmount - 5) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // Paytable strip
            Text(
                "PAYTABLE",
                color         = Chrome.copy(alpha = 0.3f),
                fontSize      = 9.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            PaytableStrip()

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Coin balance bar (top HUD)
// ─────────────────────────────────────────────────────────────

@Composable
private fun CoinBalanceBar(coins: Int, totalSpins: Int) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
            .border(
                width  = 1.dp,
                brush  = Brush.horizontalGradient(listOf(ChromeDark.copy(alpha = 0.3f), Chrome.copy(alpha = 0.15f), ChromeDark.copy(alpha = 0.3f))),
                shape  = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text("BALANCE", color = Chrome.copy(alpha = 0.35f), fontSize = 8.sp, letterSpacing = 2.sp)
            AnimatedContent(
                targetState = coins,
                transitionSpec = {
                    (fadeIn() + slideInVertically { -it }).togetherWith(fadeOut() + slideOutVertically { it })
                },
                label = "coins"
            ) { c ->
                Text("🪙 $c", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("SPINS", color = Chrome.copy(alpha = 0.35f), fontSize = 8.sp, letterSpacing = 2.sp)
            Text("$totalSpins", color = Chrome.copy(alpha = 0.7f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Ambient casino light blobs (canvas decoration)
// ─────────────────────────────────────────────────────────────

private fun drawAmbientLights(scope: DrawScope) {
    with(scope) {
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(Gold.copy(alpha = 0.06f), Color.Transparent),
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.15f, size.height * 0.15f)
            ),
            radius = size.width * 0.7f,
            center = Offset(size.width * 0.15f, size.height * 0.15f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(Color(0xFF1A0030).copy(alpha = 0.5f), Color.Transparent),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.85f, size.height * 0.5f)
            ),
            radius = size.width * 0.6f,
            center = Offset(size.width * 0.85f, size.height * 0.5f)
        )
    }
}