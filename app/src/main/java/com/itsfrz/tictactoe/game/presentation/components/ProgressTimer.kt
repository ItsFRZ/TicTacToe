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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.PlayerCount
import com.itsfrz.tictactoe.common.enums.PlayerTurn
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.*

@Composable
fun ProgressTimer(
    userTimeOutPulsatingWarning : Float,
    timeLimitAnimation : Float,
    @DrawableRes avatar : Int,
    playerTurns : Boolean,
    currentUserId : String,
    gameMode : GameMode,
    userId : String,
    playerTurn: PlayerTurn
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM){
            UserMove(username = if (currentUserId == userId) stringResource(R.string.your_text) else stringResource(R.string.opponent_text) )
        }else if(gameMode == GameMode.AI) {
            UserMove(username = if (playerTurns) stringResource(R.string.your_text) else stringResource(R.string.ai_text))
        }else if(gameMode == GameMode.FOUR_PLAYER){
            val username = when(playerTurn){
                PlayerTurn.ONE -> stringResource(R.string.player_one)
                PlayerTurn.TWO -> stringResource(R.string.player_two)
                PlayerTurn.THREE -> stringResource(R.string.player_three)
                PlayerTurn.FOUR -> stringResource(R.string.player_four)
                else ->  stringResource(R.string.player_one)
            }
            UserMove(username = username)
        } else{
            UserMove(username = if (playerTurns)  stringResource(R.string.player_two) else  stringResource(R.string.player_one) )
        }
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(20.dp))
        Row(modifier = Modifier
            .padding(horizontal = 30.dp)
            .height(40.dp)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(shape = RoundedCornerShape(100))
                    .background(color = ThemePicker.themeButtonBackgroundColor.value)
                    .border(width = 0.4.dp, color = ThemeButtonBorder)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(modifier = Modifier.size(userTimeOutPulsatingWarning.dp), painter = painterResource(id = avatar), contentDescription = "Game Timer")
            }
            Spacer(modifier = Modifier.width(8.dp))
            LinearProgressIndicator(modifier = Modifier
                .height(6.dp)
                .fillMaxWidth(),progress = timeLimitAnimation, color = ThemePicker.secondaryColor.value, backgroundColor = ThemePicker.themeButtonBackgroundColor.value)
        }
    }

}