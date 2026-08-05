package com.itsfrz.tictactoe.userregistration.usecase

sealed class UserRegistrationUseCase {
    data class OnUsernameChange(val userInput : String) : UserRegistrationUseCase()
    data class OnLanguageChange(val lang : Pair<String,String>) : UserRegistrationUseCase()
    data class OnLangToggle(val state : Boolean) : UserRegistrationUseCase()
    object OnSubmitButtonClick : UserRegistrationUseCase()
}