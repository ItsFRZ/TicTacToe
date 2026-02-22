package com.itsfrz.tictactoe.stats.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.goonline.data.models.Stats
import com.itsfrz.tictactoe.ui.theme.headerSubTitle
import com.itsfrz.tictactoe.ui.theme.headerTitle
import com.itsfrz.tictactoe.R

@Composable
fun PlayerStatsCompact(
    playerStats: Stats
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Spacer(modifier = Modifier.width(2.dp))
        Row(modifier = Modifier.weight(.2F)) {
           Text(text = "${playerStats.rank}", style = headerTitle.copy(color = Color.Yellow, fontWeight = FontWeight.ExtraBold))
        }
        Row(modifier = Modifier.weight(.4F)) {
           Box(modifier = Modifier
               .size(35.dp)
               .background(
                   color = ThemePicker.secondaryColor.value,
                   shape = RoundedCornerShape(100)
               ),
               contentAlignment = Alignment.Center
           ) {
               Text(text = "${playerStats.username[0]}", style = headerSubTitle.copy(color = Color.White))
           }
        }
        Column(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = if (playerStats.username.isNotEmpty() && playerStats.username.length < 15) playerStats.username else if(playerStats.username.isNotEmpty()) "${playerStats.username.substring(0,13)}..." else playerStats.username, style = headerSubTitle.copy(color = Color.White, fontSize = 12.sp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Level ${playerStats.level}", style = headerSubTitle.copy(color = ThemePicker.secondaryColor.value, fontSize = 8.sp))
        }
        Column(
            modifier = Modifier.weight(.4F),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "${playerStats.playTime} min", style = headerSubTitle.copy(color = Color.Yellow, fontSize = 10.sp))
            Row(modifier = Modifier.wrapContentSize()) {
                Image(modifier = Modifier.size(12.dp), painter = painterResource(id = R.drawable.ic_wins), contentDescription = "Icon Win")
                Spacer(modifier = Modifier.width(1.dp))
                Text(text = "${playerStats.wins} Wins", style = headerSubTitle.copy(color = ThemePicker.secondaryColor.value, fontSize = 8.sp))
            }
            Spacer(modifier = Modifier.height(1.dp))
            Row(modifier = Modifier.wrapContentSize()) {
                Image(modifier = Modifier.size(12.dp), painter = painterResource(id = R.drawable.ic_loss), contentDescription = "Icon Lose")
                Spacer(modifier = Modifier.width(1.dp))
                Text(text = "${playerStats.lose} Lose", style = headerSubTitle.copy(color = ThemePicker.secondaryColor.value, fontSize = 8.sp))
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}