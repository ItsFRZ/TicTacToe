package com.itsfrz.tictactoe.homepage.usecase

sealed class HomePageUseCase {
    data class OnUsernameChange(val userInput : String) : HomePageUseCase()
}