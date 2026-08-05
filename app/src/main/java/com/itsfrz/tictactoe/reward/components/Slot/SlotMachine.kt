package com.itsfrz.tictactoe.reward.components.slot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.reward.audio.SlotSoundManager
import com.itsfrz.tictactoe.reward.state.SlotNewSymbol
import com.itsfrz.tictactoe.reward.viewmodel.SlotNewViewModel
import kotlinx.coroutines.*
import kotlin.math.*

private val Gold        = Color(0xFFFFD54F)
private val GoldDark    = Color(0xFFFFB300)
private val GoldSoft    = Color(0xFFFFF1B8)
private val GoldDim     = Color(0xFF7A6420)

private val BgDeep      = Color(0xFF07080D)
private val BgMid       = Color(0xFF0F1118)
private val BgPanel     = Color(0xFF161A24)

private val ReelBg      = Color(0xFF0A0B10)
private val ReelBorder  = Color(0xFF2A3040)

private val Crimson     = Color(0xFFE53935)
private val Teal        = Color(0xFF00BCD4)
private val WinGreen    = Color(0xFF69FF47)
private val DimText     = Color(0xFF6B7891)
private val WhiteSoft   = Color(0xFFE8EAF0)

private val CellH       = 64.dp
private val ReelH       = 192.dp

private data class CoinParticle(
    val x:        Float,
    val sizeSp:   Float,
    val delayMs:  Int,
    val durationMs: Int,
    val rotSpeed: Float,
    val wobbleAmp: Float
)
@Composable
fun SlotScreenRefined(
    vm: SlotNewViewModel,
    am: SlotSoundManager
) {
    val state by vm.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1A1F30), BgDeep),
                    radius = 1400f
                )
            )
    ) {
        ScanlineOverlay()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            TopBar(coins = state.coins, spins = state.totalSpins)
            Spacer(Modifier.height(24.dp))
            SlotMachineBody(
                am        = am,
                vm        = vm,
                scope     = scope,
                reels     = state.reels,
                spinning  = state.spinning,
                resultMsg = state.resultMsg,
                lastWin   = state.lastWin
            )
            Spacer(Modifier.height(28.dp))
            SpinControls(
                spinning  = state.spinning.any { it },
                canSpin   = state.coins >= state.betAmount,
                betAmount = state.betAmount,
                onSpin    = {
                    scope.launch { am.spin(state.lastWin, state.resultMsg) }
                    vm.spin()
                            },
                onBetUp   = { vm.changeBet(state.betAmount + 50) },
                onBetDown = { vm.changeBet(state.betAmount - 50) }
            )
            Spacer(Modifier.height(28.dp))
            PaytableStrip()
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ScanlineOverlay() {
    val transition = rememberInfiniteTransition(label = "scan")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "shift"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val lineH = 3.dp.toPx()
                val gap   = 10.dp.toPx()
                val total = lineH + gap
                val offsetY = (shift * total) % total
                var y = -total + offsetY
                while (y < size.height) {
                    drawRect(
                        color   = Color.White.copy(alpha = 0.015f),
                        topLeft = Offset(0f, y),
                        size    = androidx.compose.ui.geometry.Size(size.width, lineH)
                    )
                    y += total
                }
            }
    )
}
@Composable
private fun SlotMachineBody(
    am: SlotSoundManager,
    vm: SlotNewViewModel,
    scope: CoroutineScope,
    reels: List<SlotNewSymbol>,
    spinning: List<Boolean>,
    resultMsg: String,
    lastWin: Int
) {
    val isWin = lastWin > 0
    val winGlow by animateFloatAsState(
        targetValue = if (isWin) 1f else 0f,
        animationSpec = tween(400),
        label = "winGlow"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CabinetTitle()
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(listOf(BgPanel, BgDeep))
                )
                .border(
                    width = if (isWin) 2.dp else 1.dp,
                    brush = if (isWin)
                        Brush.linearGradient(listOf(WinGreen, Gold, WinGreen))
                    else
                        Brush.linearGradient(listOf(ReelBorder, ReelBorder)),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 14.dp, vertical = 18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ReelViewport(reels = reels, spinning = spinning, vm = vm)
                Spacer(Modifier.height(16.dp))
                ResultBanner(
                    msg    = resultMsg,
                    isWin  = isWin,
                    winAmt = lastWin
                )
            }
            if (lastWin >= 500) {
                CoinRain(scope = scope, am = am)
            }
        }
    }
}
@Composable
private fun ResultBanner(msg: String, isWin: Boolean, winAmt: Int) {
    val pulse by rememberInfiniteTransition(label = "banner")
        .animateFloat(
            initialValue = 0.85f,
            targetValue  = 1f,
            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
            label        = "p"
        )
    AnimatedContent(targetState = msg, label = "result") { text ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isWin)
                        Brush.horizontalGradient(
                            listOf(
                                WinGreen.copy(alpha = 0.12f),
                                Gold.copy(alpha = 0.18f),
                                WinGreen.copy(alpha = 0.12f)
                            )
                        )
                    else
                        Brush.horizontalGradient(
                            listOf(Color.White.copy(alpha = 0.03f), Color.White.copy(alpha = 0.03f))
                        )
                )
                .padding(vertical = 10.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = text,
                color      = if (isWin) WinGreen.copy(alpha = pulse) else DimText,
                fontWeight = FontWeight.Black,
                fontSize   = 17.sp,
                textAlign  = TextAlign.Center
            )
        }
    }
}
@Composable
fun CoinRain(scope: CoroutineScope, am: SlotSoundManager) {
    val density  = LocalDensity.current
    val screenW  = LocalConfiguration.current.screenWidthDp.toFloat()
    val screenH  = LocalConfiguration.current.screenHeightDp.toFloat()

    val particles = remember {
        List(50) {
            CoinParticle(
                x          = kotlin.random.Random.nextFloat(),
                sizeSp     = kotlin.random.Random.nextFloat() * 14f + 18f,
                delayMs    = kotlin.random.Random.nextInt(0, 500),
                durationMs = kotlin.random.Random.nextInt(2800, 4200),
                rotSpeed   = kotlin.random.Random.nextFloat() * 0.6f - 0.2f,
                wobbleAmp  = kotlin.random.Random.nextFloat() * 40f
            )
        }
    }
    LaunchedEffect(Unit) { am.coinRain() }
    Box(Modifier.fillMaxSize()) {
        particles.forEach { p ->
            key(p) {
                val progress = remember { Animatable(0f) }
                LaunchedEffect(p) {
                    delay(p.delayMs.toLong())
                    progress.animateTo(1f, tween(p.durationMs, easing = LinearEasing))
                }
                val t   = progress.value
                val yDp = (-60f + (screenH + 100f) * t).dp
                val xDp = (p.x * screenW +
                        sin(t * PI.toFloat() * 4f) * p.wobbleAmp / density.density).dp

                Text(
                    text     = "🪙",
                    fontSize = p.sizeSp.sp,
                    modifier = Modifier
                        .offset(x = xDp, y = yDp)
                        .graphicsLayer {
                            rotationZ = t * p.durationMs * p.rotSpeed
                            alpha     = (1f - ((t - 0.78f).coerceAtLeast(0f) / 0.22f))
                        }
                )
            }
        }
    }
}

@Composable
private fun ReelViewport(
    reels: List<SlotNewSymbol>,
    spinning: List<Boolean>,
    vm: SlotNewViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ReelH + 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ReelBg)
            .border(1.dp, ReelBorder, RoundedCornerShape(20.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            reels.forEachIndexed { i, symbol ->
                PremiumReel(
                    target       = symbol,
                    spinning     = spinning.getOrElse(i) { false },
                    reelIndex    = i,
                    onStopped    = { vm.onReelStopped(i) },
                    modifier     = Modifier.weight(1f)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Gold, Gold, Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    Brush.verticalGradient(listOf(ReelBg.copy(alpha = 0.96f), Color.Transparent))
                )
        )

        // Bottom fade
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, ReelBg.copy(alpha = 0.96f)))
                )
        )
    }
}

@Composable
fun PremiumReel(
    target: SlotNewSymbol,
    spinning: Boolean,
    reelIndex: Int,
    onStopped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSymbols = remember { SlotNewSymbol.entries.toList() }
    val symbolCount = allSymbols.size
    val density = LocalDensity.current
    val cellPx = remember { with(density) { CellH.toPx() } }

    val offset = remember { Animatable(0f) }

    var displayedSymbol by remember { mutableStateOf(target) }

    val targetIndex = remember(target) { allSymbols.indexOf(target).coerceAtLeast(0) }

    LaunchedEffect(spinning, target) {
        if (!spinning) return@LaunchedEffect

        delay(reelIndex * kotlin.random.Random.nextLong(180L, 280L))

        val stripLen = symbolCount * cellPx
        val normalised = offset.value % stripLen

        val loops = kotlin.random.Random.nextInt(8, 13)   // 8–12 full rotations
        val targetOffsetInStrip = targetIndex * cellPx
        var delta = (targetOffsetInStrip - normalised + loops * stripLen)
        if (delta < loops * stripLen) delta += stripLen
        val finalOffset = normalised + delta
        offset.snapTo(normalised)
        offset.animateTo(
            targetValue   = normalised + delta * 0.70f,
            animationSpec = tween(
                durationMillis = kotlin.random.Random.nextInt(2200, 2800),
                easing         = LinearEasing
            )
        )
        offset.animateTo(
            targetValue   = finalOffset,
            animationSpec = tween(
                durationMillis = kotlin.random.Random.nextInt(1400, 1800),
                easing         = FastOutSlowInEasing
            )
        )
        offset.animateTo(
            targetValue   = finalOffset + cellPx * 0.08f,
            animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
        )
        offset.animateTo(
            targetValue   = finalOffset,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 900f)
        )
        offset.snapTo(finalOffset)
        onStopped()
        displayedSymbol = target
    }
    Box(
        modifier = modifier
            .height(ReelH)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF181C28), Color(0xFF0A0C12))
                )
            )
            .border(1.dp, ReelBorder, RoundedCornerShape(16.dp))
    ) {
        ReelStrip(
            allSymbols    = allSymbols,
            offset        = offset.value,
            cellPx        = cellPx,
            spinning      = spinning,
            stoppedSymbol = displayedSymbol
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(CellH)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Gold.copy(alpha = 0.07f),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Gold.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    RoundedCornerShape(10.dp)
                )
        )
    }
}

@Composable
private fun BoxScope.ReelStrip(
    allSymbols: List<SlotNewSymbol>,
    offset: Float,
    cellPx: Float,
    spinning: Boolean,
    stoppedSymbol: SlotNewSymbol
) {
    val symbolCount = allSymbols.size

    if (!spinning) {
        val centerIdx = allSymbols.indexOf(stoppedSymbol).coerceAtLeast(0)
        val prevIdx   = (centerIdx - 1 + symbolCount) % symbolCount
        val nextIdx   = (centerIdx + 1) % symbolCount
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        ) {
            SymbolCell(symbol = allSymbols[prevIdx], isCenter = false, spinning = false)
            SymbolCell(symbol = allSymbols[centerIdx], isCenter = true,  spinning = false)
            SymbolCell(symbol = allSymbols[nextIdx], isCenter = false, spinning = false)
        }
    } else {
        val baseIndex = (offset / cellPx).toInt()
        val remainder = offset % cellPx

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                // Shift strip upward by the fractional part so it scrolls smoothly
                .offset { IntOffset(x = 0, y = -remainder.roundToInt()) }
        ) {
            for (i in -1..3) {
                val idx = ((baseIndex + i) % symbolCount + symbolCount) % symbolCount
                val isCenter = (i == 1)
                SymbolCell(
                    symbol   = allSymbols[idx],
                    isCenter = isCenter,
                    spinning = true
                )
            }
        }
    }
}
@Composable
private fun SymbolCell(
    symbol: SlotNewSymbol,
    isCenter: Boolean,
    spinning: Boolean
) {
    val scale by animateFloatAsState(
        targetValue   = if (isCenter) 1f else 0.72f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label         = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (isCenter) 1f else 0.30f,
        animationSpec = tween(180),
        label         = "alpha"
    )
    val stretchY by animateFloatAsState(
        targetValue   = if (spinning) 1.12f else 1f,
        animationSpec = tween(120),
        label         = "stretch"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CellH)
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha },
        contentAlignment = Alignment.Center
    ) {
        if (isCenter && !spinning) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .blur(20.dp)
                    .background(Gold.copy(alpha = 0.28f), CircleShape)
            )
        }
        Text(
            text     = symbol.emoji,
            fontSize = 34.sp,
            color    = Color.Black.copy(alpha = 0.3f),
            modifier = Modifier.offset(y = 2.dp)
        )

        Text(
            text     = symbol.emoji,
            fontSize = 33.sp,
            modifier = Modifier.graphicsLayer { scaleY = stretchY }
        )
    }
}

@Composable
private fun CabinetTitle() {
    val inf = rememberInfiniteTransition(label = "title")
    val shift by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label         = "shift"
    )

    val brush = Brush.linearGradient(
        colors = listOf(Gold, GoldSoft, Crimson, Gold, Teal, Gold),
        start  = Offset(shift * 400f, 0f),
        end    = Offset(shift * 400f + 400f, 0f)
    )

    Text(
        text       = stringResource(R.string.slot_master_text),
        style      = TextStyle(brush = brush),
        fontSize   = 22.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 4.sp
    )
}

@Composable
private fun TopBar(coins: Int, spins: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgPanel)
            .border(1.dp, ReelBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        StatChip(label = stringResource(R.string.balance_text), value = "🪙 $coins", valueColor = Gold)
        Box(
            modifier = Modifier
                .height(36.dp)
                .width(1.dp)
                .background(ReelBorder)
        )
        StatChip(label =  stringResource(R.string.spins_text), value = "$spins", valueColor = WhiteSoft, alignEnd = true)
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    valueColor: Color,
    alignEnd: Boolean = false
) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(text = label, color = DimText, fontSize = 9.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(3.dp))
        Text(text = value, color = valueColor, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SpinControls(
    spinning: Boolean,
    canSpin: Boolean,
    betAmount: Int,
    onSpin: () -> Unit,
    onBetUp: () -> Unit,
    onBetDown: () -> Unit
) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue  = 1f,
        targetValue   = 1.04f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label         = "p"
    )
    val active = canSpin && !spinning
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            BetButton("-") { onBetDown() }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("BET", color = DimText, fontSize = 9.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(3.dp))
                Text("🪙 $betAmount", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            BetButton("+") { onBetUp() }
        }
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(64.dp)
                .graphicsLayer {
                    scaleX = if (active) pulse else 1f
                    scaleY = if (active) pulse else 1f
                }
                .clip(RoundedCornerShape(50))
                .background(
                    if (active)
                        Brush.horizontalGradient(listOf(GoldDark, Gold, GoldSoft, Gold, GoldDark))
                    else
                        Brush.horizontalGradient(listOf(GoldDim, GoldDim))
                )
                .clickable(
                    indication     = null,
                    interactionSource = remember { MutableInteractionSource() },
                    enabled        = active
                ) { onSpin() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text          = if (spinning) stringResource(R.string.spinning_text) else stringResource(R.string.spin_text),
                color         = if (active) Color.Black else Color(0xFF3A3010),
                fontWeight    = FontWeight.Black,
                fontSize      = 20.sp,
                letterSpacing = 3.sp
            )
        }
    }
}

@Composable
private fun BetButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(BgPanel)
            .border(1.dp, ReelBorder, CircleShape)
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}
@Composable
private fun PaytableStrip() {
    val symbols = remember { SlotNewSymbol.entries.toList() }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text( stringResource(R.string.paytable_text), color = DimText, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(BgPanel)
                .border(1.dp, ReelBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            symbols.forEachIndexed { i, sym ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (i % 2 == 0) Color.White.copy(0.03f) else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(sym.emoji, fontSize = 26.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.match_text), color = WhiteSoft, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(
                        text       = "×${sym.multiplier}",
                        color      = if (sym.multiplier >= 10) WinGreen else Gold,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}