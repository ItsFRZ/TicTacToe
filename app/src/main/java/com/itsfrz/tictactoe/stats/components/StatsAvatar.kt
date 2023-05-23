package com.itsfrz.tictactoe.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun StatsAvatar(
    username : String
) {

    Box(
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.padding(top = 80.dp).fillMaxWidth().height(80.dp).background(color = ThemePicker.primaryColor.value, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp))) {
            
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color = Color.White, shape = RoundedCornerShape(100)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "${username[0]}", style = headerTitle.copy(color = ThemePicker.secondaryColor.value, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold))
        }
    }

}