package com.itsfrz.tictactoe.online.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BoardViewModelFactory(
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BoardViewModel() as T
    }
}