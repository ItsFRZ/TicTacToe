package com.itsfrz.tictactoe.game.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.ui.theme.*

@Composable
fun ProgressTimer(
    userTimeOutPulsatingWarning : Float,
    timeLimitAnimation : Float,
    @DrawableRes avatar : Int = 0,
    playerTurns : Boolean,
    currentUserId : String,
    gameMode : GameMode,
    userId : String,
    isCross : Boolean
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM){
            UserMove(username = if (currentUserId == userId) "Your" else "Opponent")
        }else if(gameMode == GameMode.AI) {
            UserMove(username = if (playerTurns) "Your" else "AI")
        } else{
            UserMove(username = if (playerTurns) "Player 2" else "Player 1")
        }
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(20.dp))
        Row(modifier = Modifier
            .padding(horizontal = 30.dp)
            .height(30.dp)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(25.dp).clip(shape = RoundedCornerShape(100)).background(color = ThemeButtonBackground).border(width = 0.4.dp, color = ThemeButtonBorder).padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(modifier = Modifier.size(userTimeOutPulsatingWarning.dp), painter = painterResource(id = if (isCross) R.drawable.ic_cross else R.drawable.ic_right), contentDescription = "Game Timer")
            }
            Spacer(modifier = Modifier.width(8.dp))
            LinearProgressIndicator(modifier = Modifier
                .height(6.dp)
                .fillMaxWidth(),progress = timeLimitAnimation, color = ThemeBlue, backgroundColor = ThemeButtonBackground)
        }
    }

}