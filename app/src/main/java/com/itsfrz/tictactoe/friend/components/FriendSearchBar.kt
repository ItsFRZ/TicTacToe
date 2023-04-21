package com.itsfrz.tictactoe.friend.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.ThemeBlueDisabled
import com.itsfrz.tictactoe.ui.theme.headerSubTitle

@Composable
fun FriendSearchBar(
    username : String,
    onUserNameChange : (username : String) -> Unit,
    onAddEvent : () -> Unit
) {

    val isFocussed = remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 10.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(0.75F)
                .height(50.dp)
                .onFocusChanged {
                    isFocussed.value = it.isFocused || it.hasFocus
                },
            value = username,
            placeholder = { Text(text = "Paste UserId ...")},
            onValueChange = {inputData -> onUserNameChange(inputData)},
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search Bar Icon", tint = ThemeBlue)
            },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.textFieldColors(
                textColor = ThemeBlue,
                cursorColor = ThemeBlue,
                backgroundColor = PrimaryLight,
                focusedLabelColor = ThemeBlue,
                focusedIndicatorColor = ThemeBlue,
                leadingIconColor = ThemeBlue
            )
        )
        Spacer(modifier = Modifier
            .width(20.dp)
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = if (username.isEmpty()) ThemeBlueDisabled else ThemeBlue, shape = RoundedCornerShape(8.dp)),
            onClick = { onAddEvent() },
            enabled = username.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ThemeBlue,
                disabledBackgroundColor = ThemeBlueDisabled
            )
        ) {
            Text(
                text = "Add",
                style = headerSubTitle.copy(
                    color = Color.White,
                )
            )
        }
    }


}