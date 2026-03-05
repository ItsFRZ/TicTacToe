package com.itsfrz.tictactoe.setting.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.ThemeBlueLight
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
    val lightColor = ThemeBlueLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryColor)
            .clickable(enabled = isAdvance) { if (isAdvance) buttonEvent() }
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(id = icon),
                contentDescription = "Icon",
                tint = secondaryColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = headerTitle.copy(fontSize = 16.sp, color = Color.White)
            )
        }

        if (isAdvance) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_right_arrow),
                contentDescription = "Arrow",
                tint = secondaryColor
            )
        } else {
            Switch(
                colors = SwitchDefaults.colors(
                    checkedThumbColor = secondaryColor,
                    checkedTrackColor = secondaryColor,
                    uncheckedThumbColor = lightColor,
                    uncheckedTrackColor = lightColor
                ),
                checked = isToggled,
                onCheckedChange = toggleButtonEvent,
                modifier = Modifier.scale(0.8f)
            )
        }
    }
}