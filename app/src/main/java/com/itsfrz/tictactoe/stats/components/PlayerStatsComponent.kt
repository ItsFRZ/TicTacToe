package com.itsfrz.tictactoe.stats.components

import androidx.compose.foundation.layout.*

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.headerSubTitle
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun PlayerStatsComponent(
    username : String,
    rank : Int,
    level : Int,
    playTime : Int,
    wins : Int,
    lose : Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.weight(1F),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = if (username.isNotEmpty() && username.length < 15) username else if(username.isNotEmpty()) "${username.substring(0,13)}..." else username, style = headerTitle.copy(color = Color.White, fontSize = 20.sp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "# $rank", style = headerTitle.copy(color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Level $level", style = headerTitle.copy(color = Color.White, fontSize = 18.sp))
        }
        
        Column(
            modifier = Modifier.weight(1F),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Play Time", style = headerSubTitle.copy(color = ThemePicker.themeStatsTextHeading.value, fontSize = 10.sp))
            Text(text = "$playTime min", style = headerTitle.copy(color = Color.White, fontSize = 22.sp))
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1F)) {
                    Text(text = "Wins", style = headerSubTitle.copy(color = ThemePicker.themeStatsTextHeading.value, fontSize = 10.sp))
                    Text(text = "$wins", style = headerTitle.copy(color = Color.White, fontSize = 22.sp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(
                    modifier = Modifier.weight(1F),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = "Lose", style = headerSubTitle.copy(color = ThemePicker.themeStatsTextHeading.value, fontSize = 10.sp))
                    Text(text = "$lose", style = headerTitle.copy(color = Color.White, fontSize = 22.sp))
                }
            }
        }
    }
}