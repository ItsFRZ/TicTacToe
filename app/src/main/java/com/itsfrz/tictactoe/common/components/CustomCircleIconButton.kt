package com.itsfrz.tictactoe.common.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.common.functionality.ThemePicker

@Composable
fun CustomCircleIconButton(
    modifier: Modifier = Modifier,
    iconButtonClick: () -> Unit,
    @DrawableRes buttonIcon: Int
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 3.dp else 10.dp,
        label = "elevation"
    )
    val baseColor = ThemePicker.themeButtonBackgroundColor.value
    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                ambientColor = baseColor.copy(alpha = 0.35f),
                spotColor = baseColor.copy(alpha = 0.55f)
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shape = CircleShape
                clip = true
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 1f),
                        baseColor.copy(alpha = 0.85f),
                        baseColor.copy(alpha = 0.75f)
                    )
                ),
                shape = CircleShape
            )
            // subtle outer edge
            .border(
                width = 0.8.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { iconButtonClick() }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        radius = 140f
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(14.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = 8.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Image(
            painter = painterResource(id = buttonIcon),
            contentDescription = "Candy Button",
            modifier = Modifier
                .size(26.dp)
                .align(Alignment.Center),
            contentScale = ContentScale.Fit
        )
    }
}