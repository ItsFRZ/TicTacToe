package com.itsfrz.tictactoe.game.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue

@Composable
fun GameBoard(
    crossList : List<Int>,
    rightList : List<Int>,
    gameMode : GameMode,
    isPlayerMoved : Boolean,
    onMove : (index : Int) -> Unit,
) {
    val gameItemList = (1..9).toList()
    LazyVerticalGrid(
        modifier = Modifier.padding(horizontal = 12.dp).border(border = BorderStroke(width = 1.dp, color = PrimaryLight)),
        columns = GridCells.Fixed(3),
        content = {
            itemsIndexed(items = gameItemList){ index: Int, item: Int ->
                Column(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(9F)
                        .background(color = PrimaryLight)
                        .clickable {
                            if (gameMode == GameMode.TWO_PLAYER){
                                if (!(index in crossList) && !(index in rightList))
                                    onMove(index)
                            }else if(gameMode == GameMode.AI){
                                  if (!isPlayerMoved)
                                      onMove(index)
                            }
                        }
                        .border(width = 1.dp, color = ThemeBlue)
                        .padding(vertical = 15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (index in crossList){
                        Image(modifier = Modifier.fillMaxHeight(1F), painter = painterResource(id = R.drawable.ic_cross), contentDescription = "Cross Mark")
                    }else if (index in rightList){
                        Image(modifier = Modifier.fillMaxHeight(1F),painter = painterResource(id = R.drawable.ic_right), contentDescription = "Right Mark")
                    }else{
                        Column(modifier = Modifier
                            .fillMaxHeight(1F)
                            .fillMaxWidth()
                            .background(color = PrimaryLight)) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Blank", tint = PrimaryLight)
                        }
                    }
                }
            }
        }
    )
    
}