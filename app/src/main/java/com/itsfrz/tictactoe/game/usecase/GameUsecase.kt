package com.itsfrz.tictactoe.game.usecase

sealed class GameUsecase{
    data class OnUserTick(val index : Int) : GameUsecase()
    object OnGameRetry : GameUsecase()
    data class OnBackPress(val backPressState : Boolean): GameUsecase()
}
