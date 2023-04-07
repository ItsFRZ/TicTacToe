package com.itsfrz.tictactoe.game.domain.usecase

sealed class GameUsecase{
    data class OnUserTick(val index : Int) : GameUsecase()
    object OnGameRetry : GameUsecase()
    object OnAIMove : GameUsecase()
    data class OnBackPress(val backPressState : Boolean): GameUsecase()
    data class OnDelayLaunch(val delayValue : Boolean): GameUsecase()
}
