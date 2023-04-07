package com.itsfrz.tictactoe.homepage.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import com.itsfrz.tictactoe.userregistration.usecase.UserRegistrationUseCase

class HomePageViewModel(
    private val cloudRepository: CloudRepository,
    private val gameStoreRepository: GameStoreRepository
) : ViewModel(){

    private val _usernameValue : MutableState<String> = mutableStateOf("")
    val usernameValue : State<String> = _usernameValue

    private val _isUsernameExists : MutableState<Boolean> = mutableStateOf(false)
    val isUsernameExists : State<Boolean> = _isUsernameExists


    fun onEvent(event : UserRegistrationUseCase){
        when(event){
            is UserRegistrationUseCase.OnUsernameChange -> {
                _usernameValue.value = event.userInput
            }
            else -> {}
        }
    }

}