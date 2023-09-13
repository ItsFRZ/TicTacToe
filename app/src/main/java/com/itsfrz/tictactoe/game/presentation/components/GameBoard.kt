package com.itsfrz.tictactoe.game.presentation.components

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.functionality.ThemePicker

@Composable
fun GameBoard(
    crossList : List<Int>,
    rightList : List<Int>,
    player3List : List<Int>,
    player4List : List<Int>,
    gameMode : GameMode,
    isWinner : Boolean,
    gameCellList : List<Int>,
    columnCount : Int,
    boardHeight : Dp,
    winnerIndexList : List<Int>,
    isPlayerMoved : Boolean,
    onMove : (index : Int) -> Unit,
    onAIMove : () -> Unit,
    userId : String,
    currentUserId : String,
    @DrawableRes playerIcons : List<Int>
) {
    var playerOneIcon by remember {
        mutableStateOf(0)
    }
    var playerTwoIcon by remember {
        mutableStateOf(0)
    }
    var playerThreeIcon by remember {
        mutableStateOf(0)
    }
    var playerFourIcon by remember {
        mutableStateOf(0)
    }
    if (gameMode == GameMode.AI){
        playerOneIcon = playerIcons.get(0)
        playerTwoIcon = R.drawable.ic_ai_emoji
    }else if (gameMode == GameMode.TWO_PLAYER){
        playerOneIcon =  playerIcons.get(0)
        playerTwoIcon =  playerIcons.get(1)
    }else if (gameMode == GameMode.FOUR_PLAYER){
        playerOneIcon =  playerIcons.get(0)
        playerTwoIcon =  playerIcons.get(1)
        playerThreeIcon =  playerIcons.get(2)
        playerFourIcon =  playerIcons.get(3)
    }
    else {
        playerOneIcon = R.drawable.ic_tick_i
        playerTwoIcon = R.drawable.ic_cross_i
    }
    if (isWinner){
        Log.i("CHECK_WINNNER", "GameBoard: Winner Index ${winnerIndexList}")

    }
    val color by animateColorAsState(
        targetValue = ThemePicker.secondaryColor.value,
        animationSpec = tween(durationMillis = 3000, delayMillis = 250, easing = LinearOutSlowInEasing)
    )
    LazyVerticalGrid(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth(9F)
            .border(border = BorderStroke(width = 1.dp, color = ThemePicker.primaryColor.value)),
        columns = GridCells.Fixed(columnCount),
        content = {
            itemsIndexed(items = gameCellList){ index: Int, item: Int ->
                Column(
                    modifier = Modifier
                        .height(boardHeight)
                        .background(color = if (isWinner && index in winnerIndexList) color else ThemePicker.primaryColor.value)
                        .clickable {
                            if (!isWinner) {
                                if (gameMode == GameMode.TWO_PLAYER) {
                                    if (!(index in crossList) && !(index in rightList))
                                        onMove(index)
                                } else if (gameMode == GameMode.AI) {
                                    if (!(index in crossList) && !(index in rightList) && !isPlayerMoved) {
                                        onMove(index)
                                        onAIMove()
                                    }
                                } else if (gameMode == GameMode.FRIEND) {
                                    if (!(index in crossList) && !(index in rightList) && (userId == currentUserId)) {
                                        onMove(index)
                                    }
                                } else if (gameMode == GameMode.FOUR_PLAYER) {
                                    if (!(index in crossList) && !(index in rightList) && !(index in player3List) && !(index in player4List)) {
                                        onMove(index)
                                    }
                                }
                            }
                        }
                        .border(width = 1.dp, color = ThemePicker.secondaryColor.value)
                        .padding(vertical = 15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (index in crossList){
                        Image(modifier = Modifier.fillMaxHeight(1F), painter = painterResource(id = playerTwoIcon), contentDescription = "Cross Mark")
                    }else if (index in rightList){
                        Image(modifier = Modifier.fillMaxHeight(1F),painter = painterResource(id = playerOneIcon), contentDescription = "Right Mark")
                    }else if(index in player3List){
                        Log.i("HIDDEN_BUG", "GameBoard: Player 3 ${player3List}")
                        Image(modifier = Modifier.fillMaxHeight(1F), painter = painterResource(id = playerThreeIcon), contentDescription = "Cross Mark")
                    }else if (index in player4List){
                        Log.i("HIDDEN_BUG", "GameBoard: Player 4 ${player4List}")
                        Image(modifier = Modifier.fillMaxHeight(1F), painter = painterResource(id = playerFourIcon), contentDescription = "Cross Mark")
                    } else{
                        Column(modifier = Modifier
                            .fillMaxHeight(1F)
                            .fillMaxWidth()
                            .background(color = ThemePicker.primaryColor.value)) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Blank", tint = ThemePicker.primaryColor.value)
                        }
                    }
                }
            }
        }
    )
}