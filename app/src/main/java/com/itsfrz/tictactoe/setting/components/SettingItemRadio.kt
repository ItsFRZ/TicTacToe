package com.itsfrz.tictactoe.setting.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.ThemeBlueLight
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun SettingItemRadio(
    title : String,
    isEnabled : Boolean,
    radioButtonEvent : () -> Unit,
) {
    val view = LocalView.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ThemePicker.primaryColor.value),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(.7F),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, style = headerTitle.copy(fontSize = 16.sp,color = ThemePicker.secondaryColor.value))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                colors = RadioButtonDefaults.colors(selectedColor = ThemePicker.secondaryColor.value, unselectedColor = ThemeBlueLight),selected = isEnabled, onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                radioButtonEvent()
            })
            Spacer(modifier = Modifier.width(22.dp))
        }
    }
}