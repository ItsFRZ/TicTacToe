package com.itsfrz.tictactoe.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.ui.theme.*

@Composable
fun UserItemLayout(
    username : String,
    isUserOnline : Boolean,
    playRequestEvent : () -> Unit,
    acceptRequestEvent : () -> Unit,
    isRequested : Boolean,
) {

    Row(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier
            .fillMaxWidth(.7F),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(modifier = Modifier
                .size(40.dp)
                .background(color = ThemeBlue, shape = RoundedCornerShape(100)),
                contentAlignment = Alignment.Center) {
                Text(
                    text = "${username[0]}",
                    style = headerTitle.copy(
                        fontSize = 18.sp,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                modifier = Modifier.fillMaxWidth(.54F),
                text = if (username.length > 12) "${username.substring(0,12)}..." else username,
                style = headerSubTitle.copy(color = PrimaryDark, fontSize = 14.sp, textAlign = TextAlign.Start)
            )
            Box(modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isUserOnline) ThemeGreen else ThemeRed,
                    shape = RoundedCornerShape(100)
                )
            )
        }

        Column(modifier = Modifier
            .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isRequested){
                UserItemButton(buttonText = "Accept", backgroundColor = ThemeBlue) {
                    acceptRequestEvent()
                }
            }else{
                UserItemButton(buttonText = "Request", backgroundColor = ThemeRed) {
                    playRequestEvent()
                }
            }
        }
    }



}

@Composable
private fun UserItemButton(
    buttonText : String,
    backgroundColor : Color,
    onEvent : () -> Unit
) {
    Button(
        modifier = Modifier
            .width(85.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp)),
        onClick = { onEvent() },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor
        )
    ) {
        Text(
            text = buttonText,
            style = headerSubTitle.copy(
                fontSize = 12.sp,
                color = Color.White,
            )
        )
    }
}
