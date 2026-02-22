package com.itsfrz.tictactoe.setting.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.ThemeBlueLight
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun SettingColorPickerComponent(
    primaryColor : Color,
    secondaryColor : Color,
    themeTitle : String,
    isSelected : Boolean,
    isLanguage : Boolean = false,
    onUpdateTheme : () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        if (!isLanguage){
            Box(modifier = Modifier.size(15.dp), contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.size(18.dp), elevation = 6.dp, backgroundColor = Color.White, shape = RoundedCornerShape(100)) {

                }
                Card(modifier = Modifier.size(12.dp), elevation = 8.dp, backgroundColor = primaryColor, shape = RoundedCornerShape(100)) {

                }
                Card(modifier = Modifier.size(8.dp), elevation = 10.dp, backgroundColor = secondaryColor, shape = RoundedCornerShape(100)) {

                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Row(
            modifier = Modifier.weight(.7F),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(7F),
                text = themeTitle,
                style = headerTitle.copy(fontSize = 16.sp,color = Color.White, textAlign = TextAlign.Start)
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = { onUpdateTheme() },
            colors = RadioButtonDefaults.colors(
                selectedColor = ThemePicker.secondaryColor.value,
                unselectedColor = ThemeBlueLight
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
    }

}