package com.itsfrz.tictactoe.game.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun UserMove(
    username : String,
    isCross : Boolean
) {
    
    Column(
        modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (username.length > 12) "${username.substring(0,11)}'s Move" else "${username}'s Move", style = headerTitle.copy(color = ThemeBlue))
        Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))
        Column(modifier = Modifier
            .size(35.dp)
            .border(border = BorderStroke(1.dp, ThemeBlue), shape = RoundedCornerShape(100)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isCross)
                Image(modifier = Modifier.size(20.dp), painter = painterResource(id = R.drawable.ic_cross), contentDescription = "Cross Move")
            else Image(modifier = Modifier.size(20.dp),painter = painterResource(id = R.drawable.ic_right), contentDescription = "Cross Move")
        }
    }
    
    
}