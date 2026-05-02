package com.itsfrz.tictactoe.setting.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun SettingItem(
    @DrawableRes icon: Int,
    title: String,
    isToggled: Boolean,
    toggleButtonEvent: (Boolean) -> Unit,
    isAdvance: Boolean = false,
    buttonEvent: () -> Unit = {}
) {
    val primaryColor = ThemePicker.primaryColor.value
    val secondaryColor = ThemePicker.secondaryColor.value
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else 6.dp,
        label = "elevation"
    )
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(16.dp),
                ambientColor = primaryColor.copy(alpha = 0.25f),
                spotColor = primaryColor.copy(alpha = 0.35f)
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shape = RoundedCornerShape(16.dp)
                clip = true
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.95f),
                        primaryColor.copy(alpha = 0.85f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(isAdvance) {
                if (!isAdvance) return@pointerInput
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { buttonEvent() }
                )
            }
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    secondaryColor.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "Icon",
                        tint = secondaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = headerTitle.copy(
                        fontSize = 16.sp,
                        color = Color.White,
                        letterSpacing = 0.3.sp
                    )
                )
            }
            if (isAdvance) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_right_arrow),
                    contentDescription = "Arrow",
                    tint = secondaryColor.copy(alpha = 0.9f),
                    modifier = Modifier.size(18.dp)
                )
            } else {
                MinimalToggle(
                    checked = isToggled,
                    onCheckedChange = toggleButtonEvent,
                    accent = secondaryColor
                )
            }
        }
    }
}

@Composable
fun MinimalToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color
) {
    val offset by animateDpAsState(
        targetValue = if (checked) 18.dp else 2.dp,
        label = "toggleOffset"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) accent.copy(alpha = 0.6f)
        else Color.White.copy(alpha = 0.15f),
        label = "trackColor"
    )

    Box(
        modifier = Modifier
            .width(36.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = offset)
                .size(16.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color.White.copy(alpha = 0.7f)
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}