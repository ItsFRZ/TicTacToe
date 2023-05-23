package com.itsfrz.tictactoe.setting.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.headerSubTitle


@Composable
fun SettingSubHeader(
    subHeaderTitleText : String,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(10.dp))
        Text(modifier = Modifier, text = subHeaderTitleText, style = headerSubTitle.copy(color = ThemePicker.secondaryColor.value, fontSize = 10.sp))
    }
    
}
