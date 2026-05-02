package com.itsfrz.tictactoe.common.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun CustomOutlinedButton(
    buttonClick: () -> Unit,
    buttonText: String,
    enabled: Boolean = true
) {
    var pressed by remember { mutableStateOf(false) }
    val baseColor = ThemePicker.themeButtonBackgroundColor.value
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 3.dp else 8.dp,
        label = "elevation"
    )
    val infinite = rememberInfiniteTransition(label = "idle")
    val highlightAlpha by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "highlight"
    )

    Box(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .padding(horizontal = 70.dp)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(14.dp),
                ambientColor = baseColor.copy(alpha = 0.3f),
                spotColor = baseColor.copy(alpha = 0.45f)
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shape = RoundedCornerShape(14.dp)
                clip = true
                alpha = if (enabled) 1f else 0.6f
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 1f),
                        baseColor.copy(alpha = 0.85f),
                        baseColor.copy(alpha = 0.75f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(14.dp)
            )
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { buttonClick() }
                )
            }
            .padding(vertical = 14.dp)
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.16f)
                        )
                    )
                )
        )
        Text(
            text = buttonText,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = if (pressed) 1.dp else 0.dp),
            style = headerTitle.copy(
                color = Color.White
            )
        )
    }
}