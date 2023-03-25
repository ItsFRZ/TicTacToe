package com.itsfrz.tictactoe.online.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OnlineModeViewModelFactory(
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OnlineModeViewModel() as T
    }
}