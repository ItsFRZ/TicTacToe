package com.itsfrz.tictactoe.userregistration.usecase

import com.itsfrz.tictactoe.homepage.usecase.HomePageUseCase

sealed class UserRegistrationUseCase {
    data class OnUsernameChange(val userInput : String) : UserRegistrationUseCase()
    object OnSubmitButtonClick : UserRegistrationUseCase()
}