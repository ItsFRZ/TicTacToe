package com.itsfrz.tictactoe.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun CustomOutlinedButton(
    buttonClick : () -> Unit,
    buttonText : String
) {
    OutlinedButton(
        onClick = {buttonClick()},
        colors = ButtonDefaults.buttonColors(
            backgroundColor = PrimaryLight
        ),
        modifier = Modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth()
            .padding(horizontal = 80.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(width = 1.dp, color = ThemeBlue)
    ){
        Text(
            text = buttonText,
            style = headerTitle.copy(
                color = ThemeBlue
            )
        )
    }
}