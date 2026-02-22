package com.itsfrz.tictactoe.setting.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun SettingHeader(
    headerTitleText : String,
    onBackEvent : () -> Unit
) {
    val view = LocalView.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ThemePicker.primaryColor.value),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        Icon(modifier = Modifier.size(25.dp)
            .clickable {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onBackEvent()
        },painter = painterResource(id = R.drawable.ic_arrow_back), contentDescription = "Back Arrow Icon", tint = ThemePicker.secondaryColor.value)
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = headerTitleText, style = headerTitle.copy(color = ThemePicker.secondaryColor.value))
    }
}

