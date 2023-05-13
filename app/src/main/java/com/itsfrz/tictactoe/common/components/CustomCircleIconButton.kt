package com.itsfrz.tictactoe.common.components

import android.view.HapticFeedbackConstants
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.ui.theme.PrimaryMain
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.ThemeButtonBackground
import com.itsfrz.tictactoe.ui.theme.ThemeButtonBorder


@Composable
fun CustomCircleIconButton(
    buttonModifier: Modifier = Modifier,
    iconButtonClick : () -> Unit,
    @DrawableRes buttonIcon : Int
) {
    val view = LocalView.current
    OutlinedButton(
        modifier = buttonModifier
            .size(50.dp)
            .border(
                border = BorderStroke(width = 0.4.dp, color = ThemeButtonBorder),
                shape = RoundedCornerShape(100)
            ),
        shape = RoundedCornerShape(100),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ThemeButtonBackground
        ),
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            iconButtonClick()
        }) {
        Image(modifier = Modifier.size(22.dp), contentScale = ContentScale.Fit, painter = painterResource(id =buttonIcon), contentDescription = "Icon Button")
    }
}