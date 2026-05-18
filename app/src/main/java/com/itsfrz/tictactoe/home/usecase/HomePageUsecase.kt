package com.itsfrz.tictactoe.home.usecase

sealed class HomePageUseCase {
    data class UpdateUserOnlineStatus(val isOnline : Boolean) : HomePageUseCase()
    object OnCopyUserIdEvent : HomePageUseCase()
    data class OnPurchaseDialogEvent(val toggleState : Boolean) : HomePageUseCase()
}