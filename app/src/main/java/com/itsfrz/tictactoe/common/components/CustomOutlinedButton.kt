package com.itsfrz.tictactoe.common.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.ui.theme.*

@Composable
fun CustomOutlinedButton(
    buttonClick : () -> Unit,
    buttonText : String,
    enabled : Boolean = true
) {
    val view = LocalView.current
    OutlinedButton(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            buttonClick() },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ThemeButtonBackground
        ),
        enabled = enabled,
        modifier = Modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth()
            .padding(horizontal = 80.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(width = 1.dp, color = ThemeButtonBorder)
    ){
        Text(
            text = buttonText,
            style = headerTitle.copy(
                color = Color.White
            )
        )
    }
}