package com.itsfrz.tictactoe.game4player.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GameMultiplayerViewModelFactory(
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GameMultiplayerViewModel() as T
    }
}