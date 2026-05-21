package com.itsfrz.tictactoe.board.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.*

@Composable
fun BoardTypeComponent(
    modifier: Modifier = Modifier,
    boardLevelText: String,
    @DrawableRes boardTypeVisual: Int,
    selectedIndex: Int,
    onDifficultyEvent: (index: Int) -> Unit,
    boardSizeText: String,
    isAIMode: Boolean = false,
    gameBoardContentText: String
) {
    Card(
        modifier = modifier,
        elevation = 15.dp,
        backgroundColor = ThemePicker.themeBoardBackground.value
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            BoardHeaderComponent(
                boardSizeText = boardSizeText,
                boardHeaderText = boardLevelText
            )
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(25.dp))
            Image(
                modifier = Modifier.size(140.dp),
                painter = painterResource(id = boardTypeVisual),
                contentDescription = "Board Type Image",
                contentScale = ContentScale.FillBounds
            )
            Spacer(modifier = Modifier.fillMaxWidth().height(18.dp))

            if (isAIMode) {
                DifficultyCapsule(
                    selectedIndex = selectedIndex,
                    onDifficultyEvent = onDifficultyEvent
                )
            } else {
                Text(
                    text = gameBoardContentText,
                    style = headerSubTitle.copy(
                        fontSize = 12.sp,
                        color = ThemePicker.secondaryColor.value
                    )
                )
            }

            Spacer(modifier = Modifier.fillMaxWidth().height(5.dp))
        }
    }
}

@Composable
private fun BoardHeaderComponent(
    boardSizeText: String,
    boardHeaderText: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = boardHeaderText,
            style = headerSubTitle.copy(color = Color.White, fontSize = 22.sp)
        )
        Row(
            modifier = Modifier
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
                backgroundColor = ThemePicker.secondaryColor.value
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
    selectedIndex: Int,
    onDifficultyEvent: (index: Int) -> Unit
) {
    val onClickEasy = remember { { onDifficultyEvent(0) } }
    val onClickMedium = remember { { onDifficultyEvent(1) } }
    val onClickHard = remember { { onDifficultyEvent(2) } }

    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(color = ThemePicker.themeButtonBackgroundColor.value)
            .border(width = 1.dp, color = ThemeGreen, shape = RoundedCornerShape(20.dp))
    ) {
        CapsuleShape(
            currentIndex = 0,
            selectedIndex = selectedIndex,
            borderColor = ThemePicker.themeButtonBackgroundColor.value,
            onClickEvent = onClickEasy
        )
        CapsuleShape(
            currentIndex = 1,
            selectedIndex = selectedIndex,
            borderColor = ThemePicker.themeButtonBackgroundColor.value,
            onClickEvent = onClickMedium
        )
        CapsuleShape(
            currentIndex = 2,
            selectedIndex = selectedIndex,
            borderColor = ThemePicker.themeButtonBackgroundColor.value,
            onClickEvent = onClickHard
        )
    }
}

@Composable
private fun CapsuleShape(
    currentIndex: Int,
    selectedIndex: Int,
    borderColor: Color,
    onClickEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(
                when (currentIndex) {
                    0 -> 0.3F
                    1 -> 0.4F
                    else -> 0.6F
                }
            )
            .fillMaxHeight()
            .clip(
                when (currentIndex) {
                    0 -> Shapes.leftRoundedCorners(12.dp)
                    2 -> Shapes.rightRoundedCorners(12.dp)
                    else -> Shapes.zeroRoundedCorners()
                }
            )
            .border(width = 1.dp, color = borderColor)
            .background(
                color = if (selectedIndex == currentIndex) ThemeGreen
                else ThemePicker.themeButtonBackgroundColor.value
            )
            .clickable(onClick = onClickEvent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth(1F)
                .fillMaxHeight(),
            text = when (currentIndex) {
                0 -> "Easy"
                1 -> "Medium"
                else -> "Hard"
            },
            style = headerSubTitle.copy(color = Color.White)
        )
    }
}