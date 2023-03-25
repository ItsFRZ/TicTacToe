package com.itsfrz.tictactoe.common.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue


@Composable
fun CustomCircleIconButton(
    buttonModifier: Modifier = Modifier,
    iconButtonClick : () -> Unit,
    @DrawableRes buttonIcon : Int
) {
    OutlinedButton(
        modifier = buttonModifier
            .size(50.dp)
            .border(
                border = BorderStroke(width = 0.2.dp, color = ThemeBlue),
                shape = RoundedCornerShape(100)
            ),
        shape = RoundedCornerShape(100),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = PrimaryLight
        ),
        onClick = { iconButtonClick() }) {
        Image(contentScale = ContentScale.Fit, painter = painterResource(id =buttonIcon), contentDescription = "Icon Button")
    }
}