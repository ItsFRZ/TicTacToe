package com.itsfrz.tictactoe.common.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.PrimaryDark
import com.itsfrz.tictactoe.ui.theme.errorMessage

@Composable
fun TextFieldWithValidation(
    fieldValue : String,
    onUsernameChange : (inputData : String) -> Unit,
    isValidationTriggered : Boolean,
    validationMessage : String = "Username field should not be empty!",
    @DrawableRes leadingIcon : Int = R.drawable.ic_username,
    label : String = "Username"
) {
    val isFocussed = remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier.onFocusChanged {
                isFocussed.value = it.isFocused || it.hasFocus
            },
            label = { Text(text = label, color = if (isFocussed.value && !isValidationTriggered) ThemePicker.secondaryColor.value else if (isValidationTriggered) Color.Red else PrimaryDark) },
            value = fieldValue,
            onValueChange = {inputData -> onUsernameChange(inputData)},
            leadingIcon = {
                Image(colorFilter = ColorFilter.tint(color = if (isFocussed.value && !isValidationTriggered) ThemePicker.secondaryColor.value else if (isValidationTriggered) Color.Red else PrimaryDark), painter = painterResource(id = leadingIcon), contentDescription = "Username")
            },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.textFieldColors(
                textColor = if(isValidationTriggered) Color.Red else ThemePicker.secondaryColor.value,
                cursorColor = ThemePicker.secondaryColor.value,
                backgroundColor = ThemePicker.primaryColor.value,
                focusedLabelColor = ThemePicker.secondaryColor.value,
                focusedIndicatorColor = ThemePicker.secondaryColor.value,
                leadingIconColor = ThemePicker.secondaryColor.value,
                errorLabelColor = if (isValidationTriggered) Color.Red else ThemePicker.secondaryColor.value,
                errorLeadingIconColor = if (isValidationTriggered) Color.Red else ThemePicker.secondaryColor.value,
                errorIndicatorColor = if (isValidationTriggered) Color.Red else ThemePicker.secondaryColor.value
            ),
            isError = isValidationTriggered,
            singleLine = true
        )
        Spacer(modifier = Modifier
            .height(8.dp)
            .fillMaxWidth())

        if (isValidationTriggered){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 62.dp)
            ) {
                Text(
                    style = errorMessage,
                    text = validationMessage,
                    textAlign = TextAlign.Start
                )
            }
        }
    }

}

