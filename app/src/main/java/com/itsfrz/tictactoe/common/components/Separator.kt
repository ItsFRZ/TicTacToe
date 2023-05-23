package com.itsfrz.tictactoe.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Separator(
    color : Color
) {
    Spacer(modifier = Modifier
        .padding(horizontal = 15.dp)
        .fillMaxWidth()
        .height(0.6.dp)
        .background(color = color)
    )
}