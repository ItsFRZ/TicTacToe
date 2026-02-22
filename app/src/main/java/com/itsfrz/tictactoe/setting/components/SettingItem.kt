package com.itsfrz.tictactoe.setting.components

import android.view.HapticFeedbackConstants
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.ThemeBlueLight
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun SettingItem(
    @DrawableRes icon : Int,
    title : String,
    isToggled : Boolean,
    toggleButtonEvent : (toggle : Boolean) -> Unit,
    isAdvance : Boolean = false,
    buttonEvent : () -> Unit = {}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isAdvance) {
                    buttonEvent()
                }
            }
            .background(ThemePicker.primaryColor.value),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(.7F),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(10.dp))
            Icon(modifier = Modifier.size(16.dp),painter = painterResource(id = icon), contentDescription = "Back Arrow Icon", tint = ThemePicker.secondaryColor.value)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, style = headerTitle.copy(fontSize = 16.sp,color = Color.White))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isAdvance){
                Icon(modifier = Modifier.size(20.dp),painter = painterResource(id = R.drawable.ic_right_arrow), contentDescription = "Back Arrow Icon", tint = ThemePicker.secondaryColor.value)
            }else{
                Switch(
                    colors = SwitchDefaults.colors(checkedThumbColor = ThemePicker.secondaryColor.value, checkedTrackColor = ThemePicker.secondaryColor.value, uncheckedThumbColor = ThemeBlueLight, uncheckedTrackColor = ThemeBlueLight),
                    checked = isToggled, onCheckedChange = {
                        toggleButtonEvent(it)
                    }
                )
            }
            Spacer(modifier = Modifier.width(22.dp))
        }
    }

}