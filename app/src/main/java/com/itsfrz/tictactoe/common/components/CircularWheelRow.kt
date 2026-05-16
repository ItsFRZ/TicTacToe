package com.itsfrz.tictactoe.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CircularWheelRow(
    items: List<String>,
    modifier: Modifier = Modifier
) {

    val listState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 140.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {



            itemsIndexed(items) { index, item ->

                val layoutInfo = listState.layoutInfo
                val visibleItem = layoutInfo.visibleItemsInfo
                    .firstOrNull { it.index == index }

                val screenCenter =
                    layoutInfo.viewportEndOffset / 2

                val itemCenter =
                    (visibleItem?.offset ?: 0) +
                            (visibleItem?.size ?: 0) / 2

                val distance =
                    (itemCenter - screenCenter).toFloat()

                val normalized =
                    (distance / screenCenter)
                        .coerceIn(-1f, 1f)

                // Circular arc effect
                val yOffset =
                    kotlin.math.abs(normalized) * 80f

                // Scale effect
                val scale =
                    1f - (kotlin.math.abs(normalized) * 0.25f)

                // Alpha effect
                val alpha =
                    1f - (kotlin.math.abs(normalized) * 0.5f)


                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = yOffset
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha

                            rotationZ = normalized * 12f
                        }
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Color(
                                red = 255,
                                green = 140,
                                blue = 0
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = item,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}