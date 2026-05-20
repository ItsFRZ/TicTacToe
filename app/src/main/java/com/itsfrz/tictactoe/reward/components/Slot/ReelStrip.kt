package com.itsfrz.tictactoe.reward.components.Slot


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

//@Composable
//fun ReelStrip(
//    symbol:    SlotNewSymbol,
//    spinning:  Boolean,
//    reelIndex: Int,
//    modifier:  Modifier = Modifier,
//) {
//    // Stagger animation start by reel index
//    val scope = rememberCoroutineScope()
//
//    // Speed: 0 = stopped, 1 = full spin
//    val speed = remember { Animatable(0f) }
//    // Strip scroll offset (0..1 cycles through symbols)
//    val offset = remember { Animatable(0f) }
//
//    var displaySymbol by remember { mutableStateOf(symbol) }
//    var settled       by remember { mutableStateOf(true) }
//
//    LaunchedEffect(spinning) {
//        if (spinning) {
//            settled = false
//            // Ramp up
//            speed.animateTo(1f, tween(180 + reelIndex * 60, easing = FastOutSlowInEasing))
//            // Continuous scroll while speed > 0
//            scope.launch {
//                while (speed.value > 0.01f) {
//                    offset.snapTo((offset.value + speed.value * 0.08f) % 1f)
//                    delay(16)
//                }
//            }
//        } else {
//            // Ramp down and snap to the new symbol
//            speed.animateTo(0f, tween(260 + reelIndex * 80, easing = FastOutSlowInEasing))
//            displaySymbol = symbol
//            settled = true
//        }
//    }
//
//    // Blur amount proportional to speed
//    val blurPx   = speed.value * 18f
//    val emojiScale = 1f + speed.value * 0.06f  // slight scale pulse during spin
//
//    // Bounce scale on settle
//    val settleScale = remember { Animatable(1f) }
//    LaunchedEffect(settled) {
//        if (settled && !spinning) {
//            settleScale.animateTo(1.18f, tween(90, easing = FastOutSlowInEasing))
//            settleScale.animateTo(1f,    tween(140, easing = FastOutSlowInEasing))
//        }
//    }
//
//    Box(
//        modifier = modifier
//            .size(100.dp)
//            .drawBehind { drawReelBackground(this) }
//            .clip(RoundedCornerShape(24.dp)),
//        contentAlignment = Alignment.Center
//    ) {
//        // Spinning ghost symbols (blur simulation via offset opacity layers)
//        if (!settled) {
//            val ghosts = listOf(
//                SlotNewSymbol.entries[((SlotNewSymbol.entries.indexOf(displaySymbol) - 1 + SlotNewSymbol.entries.size) % SlotNewSymbol.entries.size)],
//                SlotNewSymbol.entries[((SlotNewSymbol.entries.indexOf(displaySymbol) + 1) % SlotNewSymbol.entries.size)],
//            )
//            ghosts.forEachIndexed { i, ghost ->
//                val ghostOffset = if (i == 0) (-50f * speed.value).dp else (50f * speed.value).dp
//                Text(
//                    text     = ghost.emoji,
//                    fontSize = 42.sp,
//                    modifier = Modifier
//                        .offset(y = ghostOffset)
//                        .alpha(speed.value * 0.35f)
//                )
//            }
//        }
//
//        // Main symbol
//        Text(
//            text     = displaySymbol.emoji,
//            fontSize = 46.sp,
//            modifier = Modifier
//                .graphicsLayer {
//                    scaleX    = settleScale.value * emojiScale
//                    scaleY    = settleScale.value * emojiScale
//                    alpha     = if (settled) 1f else (1f - speed.value * 0.3f)
//                    // Vertical motion blur approximation
//                    translationY = if (!settled) {
//                        sin(offset.value * PI.toFloat() * 2f) * blurPx
//                    } else 0f
//                }
//        )
//
//        // Inner shadow top/bottom for depth
//        Box(
//            modifier = Modifier
//                .matchParentSize()
//                .drawWithContent {
//                    drawContent()
//                    drawRect(
//                        Brush.verticalGradient(
//                            listOf(
//                                Color.Black.copy(alpha = 0.55f),
//                                Color.Transparent,
//                                Color.Transparent,
//                                Color.Black.copy(alpha = 0.55f)
//                            )
//                        )
//                    )
//                }
//        )
//    }
//}
