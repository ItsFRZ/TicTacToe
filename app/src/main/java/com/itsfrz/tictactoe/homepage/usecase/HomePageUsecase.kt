package com.itsfrz.tictactoe.homepage.usecase

sealed class HomePageUseCase {
    data class UpdateUserOnlineStatus(val isOnline : Boolean) : HomePageUseCase()
    object OnCopyUserIdEvent : HomePageUseCase()
}