package com.itsfrz.tictactoe.common.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.common.functionality.ThemePicker

@Composable
fun PixelBackground(
    modifier: Modifier = Modifier,
    baseColor: Color = ThemePicker.primaryColor.value
) {

    val infinite = rememberInfiniteTransition(label = "")

    val shimmer by infinite.animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {

        drawRect(
            color = baseColor.copy(alpha = shimmer)
        )

        val pixelSize = 24.dp.toPx()

        val cols = (size.width / pixelSize).toInt()
        val rows = (size.height / pixelSize).toInt()

        for (x in 0..cols) {
            for (y in 0..rows) {

                val random =
                    ((x * 928371 + y * 12377) % 100) / 100f

                val alpha = 0.02f + (random * 0.05f)

                drawRect(
                    color = Color.White.copy(alpha = alpha),
                    topLeft = Offset(
                        x * pixelSize,
                        y * pixelSize
                    ),
                    size = Size(
                        pixelSize - 1f,
                        pixelSize - 1f
                    )
                )
            }
        }

        // subtle vignette
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.22f)
                ),
                center = center,
                radius = size.maxDimension * 0.85f
            )
        )
    }
}