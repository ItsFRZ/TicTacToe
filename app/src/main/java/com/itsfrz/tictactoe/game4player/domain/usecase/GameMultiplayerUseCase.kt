package com.itsfrz.tictactoe.game4player.domain.usecase

sealed class GameMultiplayerUseCase  {
    data class OnBackPress(val backPressState : Boolean) : GameMultiplayerUseCase()
}