package com.itsfrz.tictactoe.common.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.enums.ButtonType
import com.itsfrz.tictactoe.ui.theme.*
import com.itsfrz.tictactoe.R

@Composable
fun UserItemLayout(
    modifier : Modifier,
    username : String,
    isUserOnline : Boolean,
    playRequestEvent : () -> Unit,
    acceptRequestEvent : () -> Unit,
    isRequested : Boolean,
) {
    Column(
        modifier = modifier
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

            Row(modifier = Modifier
                .height(42.dp)
                .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRequested){
                    UserItemButton(buttonText = "Accept", backgroundColor = ThemeGreen) {
                        acceptRequestEvent()
                    }
                }else{
                    UserItemButton(buttonText = "Request", backgroundColor = ThemeBlue) {
                        playRequestEvent()
                    }
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
            .background(color = Color.Transparent, shape = RoundedCornerShape(12.dp))
            .size(width = 80.dp, height = 28.dp),
        onClick = {onEvent()},
        contentPadding = PaddingValues(top = 5.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor
        )
    ) {
        Text(
            modifier = Modifier.fillMaxSize(),
            text = buttonText,
            style = headerSubTitle.copy(
                fontSize = 12.sp,
                color = Color.White,
            )
        )
    }
}
