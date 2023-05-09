package com.itsfrz.tictactoe.board.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.RadioButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.ui.theme.ThemeBlue

@Composable
fun SelectedBoardIndicator(
    selectedBoardIndex: Int,
    eventPropagate: (index : Int) -> Unit
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(), horizontalArrangement = Arrangement.Center) {
        for (i in 0..2){
            if (i == selectedBoardIndex) eventPropagate(i)
            CustomRadioIndicator(isSelected = i == selectedBoardIndex, indicatorSize = 10.dp)
        }
    }
}

@Composable
private fun CustomRadioIndicator(
    isSelected : Boolean,
    indicatorSize : Dp
){
    Row(modifier = Modifier
        .padding(horizontal = 8.dp)
        .size(indicatorSize)
        .clip(RoundedCornerShape(100))
        .background(color = if (isSelected) ThemeBlue else Color.Transparent)
        .border(width = 1.dp, color = ThemeBlue, shape = RoundedCornerShape(100))

    ){}
}