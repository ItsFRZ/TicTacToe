package com.itsfrz.tictactoe.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.headerTitle

@Composable
fun TitleTextComponent() {
    val accent = ThemePicker.secondaryColor.value
    val text = buildAnnotatedString {
        append(stringResource(R.string.choose_your)+"\n")
        withStyle(
            style = SpanStyle(
                color = accent,
                fontWeight = FontWeight.SemiBold
            )
        ) {
            append(stringResource(R.string.play_mode))
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = headerTitle.copy(
                color = Color.Black.copy(alpha = 0.18f),
                letterSpacing = 0.4.sp
            ),
            modifier = Modifier.offset(y = 1.2.dp),
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )
        Text(
            text = text,
            style = headerTitle.copy(
                color = Color.White,
                letterSpacing = 0.4.sp
            ),
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )
    }
}