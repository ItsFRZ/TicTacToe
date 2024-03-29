package com.itsfrz.tictactoe.game.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.ui.theme.ThemeBlue


@Composable
fun GameDivider() {
    Divider(modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(), color = ThemeBlue, thickness = 0.4.dp)
}