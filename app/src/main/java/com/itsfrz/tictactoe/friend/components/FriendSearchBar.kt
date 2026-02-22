package com.itsfrz.tictactoe.friend.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.*

@Composable
fun FriendSearchBar(
    username : String,
    onUserNameChange : (username : String) -> Unit,
    onAddEvent : () -> Unit
) {
    val view = LocalView.current
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
            placeholder = { Text(
                color = if (isFocussed.value) LightWhite else ThemePicker.secondaryColor.value,
                text = "Paste UserId ...",
            )},
            onValueChange = {inputData -> onUserNameChange(inputData)},
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search Bar Icon", tint = ThemePicker.secondaryColor.value)
            },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                cursorColor = ThemePicker.secondaryColor.value,
                backgroundColor = ThemePicker.primaryColor.value,
                focusedLabelColor = ThemePicker.secondaryColor.value,
                leadingIconColor = ThemePicker.secondaryColor.value,
                focusedBorderColor = ThemePicker.secondaryColor.value,
                disabledTextColor = LightWhite,
                disabledLeadingIconColor = ThemePicker.themeButtonBackgroundColor.value,
                unfocusedBorderColor = ThemePicker.themeButtonBackgroundDisabled.value,

            )
        )
        Spacer(modifier = Modifier
            .width(20.dp)
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = if (username.isEmpty()) ThemePicker.themeButtonBackgroundDisabled.value else ThemePicker.themeButtonBackgroundColor.value, shape = RoundedCornerShape(8.dp)),
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onAddEvent() },
            enabled = username.isNotEmpty(),
            border = BorderStroke(width = 0.4.dp, color = ThemeButtonBorder),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ThemePicker.themeButtonBackgroundColor.value,
                disabledBackgroundColor = ThemePicker.themeButtonBackgroundDisabled.value
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