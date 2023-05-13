package com.itsfrz.tictactoe.board.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.ui.theme.*

@Composable
fun BoardTypeComponent(
    modifier: Modifier = Modifier,
    boardLevelText : String,
    @DrawableRes boardTypeVisual : Int,
    selectedIndex: Int,
    onDifficultyEvent: (index: Int) -> Unit,
    boardSizeText: String,
    isAIMode : Boolean = false,
    gameBoardContentText : String
) {
    Card(
        modifier = modifier,
        elevation = 15.dp,
        backgroundColor = ThemeButtonBackground
    ) {
        Column(modifier = Modifier
            .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            BoardHeaderComponent(boardSizeText = boardSizeText, boardHeaderText = boardLevelText)
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(25.dp))
            Image(modifier = Modifier.size(140.dp), painter = painterResource(id = boardTypeVisual), contentDescription = "Board Type Image", contentScale = ContentScale.FillBounds)
            Spacer(modifier = Modifier.fillMaxWidth().height(18.dp))
            if (isAIMode) {
                DifficultyCapsule(selectedIndex = selectedIndex, onDifficultyEvent = { idx -> onDifficultyEvent(idx) })
            }else{
                Text(text = gameBoardContentText, style = headerSubTitle.copy(fontSize = 12.sp, color = ThemeBlue))
            }
            Spacer(modifier = Modifier.fillMaxWidth().height(5.dp))
        }
    }
}

@Composable
private fun BoardHeaderComponent(
    boardSizeText : String,
    boardHeaderText : String
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(), contentAlignment = Alignment.Center) {
        Text(modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, text = boardHeaderText, style = headerSubTitle.copy(color = Color.White, fontSize = 22.sp))
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Card(
                modifier = Modifier
                    .size(width = 50.dp, height = 30.dp)
                    .clip(shape = Shapes.cutRoundedCorners(12.dp)),
                elevation = 18.dp,
                backgroundColor = ThemeBlue
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    text = boardSizeText,
                    style = headerSubTitle.copy(fontSize = 10.sp, color = Color.White)
                )
            }
        }
    }
}

@Composable
private fun DifficultyCapsule(
    selectedIndex : Int,
    onDifficultyEvent : (index : Int) -> Unit
) {

    Row(modifier = Modifier
        .padding(horizontal = 20.dp, vertical = 10.dp)
        .height(30.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(color = ThemeButtonBackgroundDisabled)
        .border(width = 1.dp, color = ThemeGreen, shape = RoundedCornerShape(20.dp))
    ) {
        for (i in 0 until 3){
            var borderColor = ThemeButtonBackground
            if (i == 1)
                borderColor = ThemeGreen
            Column(modifier = Modifier
                .weight(1F)
                .clip(
                    if (i == 0) Shapes.leftRoundedCorners(12.dp) else if (i == 2) Shapes.rightRoundedCorners(
                        12.dp
                    ) else Shapes.zeroRoundedCorners()
                )
                .border(width = 1.dp, color = borderColor)
                .background(color = if (selectedIndex == i) ThemeGreen else ThemeButtonBackground)
                .clickable { onDifficultyEvent(i) },
               horizontalAlignment = Alignment.CenterHorizontally,
               verticalArrangement = Arrangement.Center
            ) {
                Text(modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(1F)
                    .fillMaxHeight(),text = if (i == 0) "Easy" else if (i == 1) "Medium" else "Hard", style = headerSubTitle.copy(color = Color.White))
            }
        }
    }
}