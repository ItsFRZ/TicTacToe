package com.itsfrz.tictactoe.homepage.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.itsfrz.tictactoe.userregistration.usecase.UserRegistrationUseCase

class HomePageViewModel : ViewModel(){

    private val _usernameValue : MutableState<String> = mutableStateOf("")
    val usernameValue : State<String> = _usernameValue

    private val _isUsernameExists : MutableState<Boolean> = mutableStateOf(false)
    val isUsernameExists : State<Boolean> = _isUsernameExists


    fun onEvent(event : UserRegistrationUseCase){
        when(event){
            is UserRegistrationUseCase.OnUsernameChange -> {
                Log.i("INPUT", "onEvent: ${event.userInput}")
                _usernameValue.value = event.userInput
                _isUsernameExists.value = _usernameValue.value.equals("Faraz")
            }
        }
    }

}