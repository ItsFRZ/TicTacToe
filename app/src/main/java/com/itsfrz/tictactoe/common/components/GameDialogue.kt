package com.itsfrz.tictactoe.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.headerSubTitle
import com.itsfrz.tictactoe.ui.theme.headerTitle



@Composable
fun GameWinDialogue(
    winnerUsername : String,
    onRetryButtonClick : () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(7f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(2.dp),
        elevation = 10.dp
    ) {
        Column(
            modifier = Modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier
                .wrapContentWidth()
                .height(5.dp))
            Text(text = "WIN", style = headerTitle.copy(color = ThemeBlue))
            Spacer(modifier = Modifier
                .wrapContentWidth()
                .height(12.dp))
            Image(
                contentScale = ContentScale.Fit,
                painter = painterResource(id = R.drawable.ic_trophy), contentDescription = "Trophy Image")
            Spacer(modifier = Modifier
                .wrapContentWidth()
                .height(2.dp))
            Text(text = winnerUsername, style = headerSubTitle.copy(color = ThemeBlue))
            Spacer(modifier = Modifier
                .wrapContentWidth()
                .height(5.dp))
            CustomButton(
                onButtonClick = { onRetryButtonClick() },
                buttonText = "Retry"
            )
        }
    }
}
