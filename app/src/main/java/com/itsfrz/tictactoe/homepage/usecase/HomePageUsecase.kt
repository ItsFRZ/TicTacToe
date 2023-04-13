package com.itsfrz.tictactoe.homepage.usecase

sealed class HomePageUseCase {
    object OnCopyUserIdEvent : HomePageUseCase()
}