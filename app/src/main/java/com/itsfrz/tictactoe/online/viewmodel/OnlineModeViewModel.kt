package com.itsfrz.tictactoe.online.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameLevel
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.userregistration.usecase.UserRegistrationUseCase

class OnlineModeViewModel : ViewModel(){

    private val _usernameValue : MutableState<String> = mutableStateOf("")
    val usernameValue : State<String> = _usernameValue

    private val _isUsernameExists : MutableState<Boolean> = mutableStateOf(false)
    val isUsernameExists : State<Boolean> = _isUsernameExists

    private val _level : MutableState<GameLevel> = mutableStateOf(GameLevel.NONE)
    val level : State<GameLevel> = _level

    private val _gameMode : MutableState<GameMode> = mutableStateOf(GameMode.FRIEND)
    val gameMode : State<GameMode> = _gameMode

    private val _boardType : MutableState<BoardType> = mutableStateOf(BoardType.THREEX3)
    val boardType : State<BoardType> = _boardType

    fun onEvent(event : UserRegistrationUseCase){
        when(event){
            is UserRegistrationUseCase.OnUsernameChange -> {
                Log.i("INPUT", "onEvent: ${event.userInput}")
                _usernameValue.value = event.userInput
                _isUsernameExists.value = _usernameValue.value.equals("Faraz")
            }
            else -> {}
        }
    }

}