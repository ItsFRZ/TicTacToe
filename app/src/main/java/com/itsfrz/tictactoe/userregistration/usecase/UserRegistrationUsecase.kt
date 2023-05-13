package com.itsfrz.tictactoe.userregistration.usecase

sealed class UserRegistrationUseCase {
    data class OnUsernameChange(val userInput : String) : UserRegistrationUseCase()
    object OnSubmitButtonClick : UserRegistrationUseCase()
}