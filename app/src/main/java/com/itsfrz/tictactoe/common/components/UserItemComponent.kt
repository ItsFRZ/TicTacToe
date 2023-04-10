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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.ui.theme.*

@Composable
fun UserItemLayout(
    username : String,
    isUserOnline : Boolean,
    onItemEvent : () -> Unit,
    onAcceptEvent : () -> Unit,
    isFriendList : Boolean = true,
) {

    Row(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 4.dp)
            .clickable {
                if (isFriendList) {
                    onItemEvent()
                }
            }
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier
            .fillMaxWidth(.8F),
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
                modifier = Modifier.fillMaxWidth(.5F),
                text = if (username.length > 12) "${username.substring(0,12)}..." else username,
                style = headerSubTitle.copy(color = PrimaryDark, fontSize = 14.sp, textAlign = TextAlign.Start)
            )

            Box(modifier = Modifier
                .size(5.dp)
                .background(
                    color = if (isUserOnline) ThemeGreen else ThemeRed,
                    shape = RoundedCornerShape(100)
                )
            )


        }

        Row(modifier = Modifier
            .fillMaxWidth(1F),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isFriendList){
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Right Arrow", tint = ThemeBlue)
            }else{
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = ThemeBlue, shape = RoundedCornerShape(8.dp)),
                    onClick = { onAcceptEvent() },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ThemeBlue
                    )
                ) {
                    Text(
                        text = "Accept",
                        style = headerSubTitle.copy(
                            fontSize = 12.sp,
                            color = Color.White,
                        )
                    )
                }
            }
        }
    }



}