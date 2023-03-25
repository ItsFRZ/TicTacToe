package com.itsfrz.tictactoe.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.ThemeBlueDisabled
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    onButtonClick : () -> Unit,
    isButtonEnabled : Boolean = true,
    buttonText : String = "GO",
    buttonColors : ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = ThemeBlue,
        disabledBackgroundColor = ThemeBlueDisabled
    )
) {
    Button(
        modifier = modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 100.dp),
        colors = buttonColors,
        shape = RoundedCornerShape(8.dp),
        onClick = { onButtonClick() },
        enabled = isButtonEnabled
    ) {
        Text(
            modifier = Modifier,
            text = buttonText,
            textAlign = TextAlign.Center,
            color = PrimaryLight,
            style = headerTitle.copy(
                color = PrimaryLight
            )
        )
    }
}