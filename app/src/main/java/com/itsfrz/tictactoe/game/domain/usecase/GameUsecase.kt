package com.itsfrz.tictactoe.game.domain.usecase

import com.itsfrz.tictactoe.friend.usecase.FriendPageUseCase

sealed class GameUsecase{
    data class OnUserTick(val index : Int) : GameUsecase()
    object OnGameRetry : GameUsecase()
    object OnAIMove : GameUsecase()
    object GameExitEvent : GameUsecase()
    object OnClearGameBoard : GameUsecase()
    data class OnBackPress(val backPressState : Boolean): GameUsecase()
    data class OnDelayLaunch(val delayValue : Boolean): GameUsecase()
    data class UpdateUserId(val userId : String): GameUsecase()
    data class UpdateFriendUserId(val userId : String): GameUsecase()
    data class OnUpdateGameSessionId(val sessionId : String) : GameUsecase()
    data class OnUpdateCurrentUserId(val currentUserId : String) : GameUsecase()
}
