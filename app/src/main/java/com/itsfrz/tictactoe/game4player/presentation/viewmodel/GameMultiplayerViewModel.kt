package com.itsfrz.tictactoe.game4player.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.itsfrz.tictactoe.game4player.domain.usecase.GameMultiplayerUseCase
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository

class GameMultiplayerViewModel(
) : ViewModel() {

    private val _onBackPress : MutableState<Boolean> = mutableStateOf(false)
    val onBackPress = _onBackPress

    fun onEvent(event: GameMultiplayerUseCase) {
        when(event){
            is GameMultiplayerUseCase.OnBackPress -> {
                _onBackPress.value = event.backPressState
            }
        }

    }
}