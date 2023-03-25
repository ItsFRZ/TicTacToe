package com.itsfrz.tictactoe.userregistration.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class UserRegistrationViewModelFactory(
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserRegistrationViewModel() as T
    }
}