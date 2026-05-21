package com.itsfrz.tictactoe.game.domain.usecase

// BUG FIX: Removed unused import `FriendPageUseCase` that polluted the namespace.
import com.itsfrz.tictactoe.common.enums.PlayerCount

sealed class GameUsecase {
    data class OnUserTick(val index: Int) : GameUsecase()
    data class UserMove(val state: Boolean) : GameUsecase()
    object OnGameRetry : GameUsecase()
    object OnAIMove : GameUsecase()
    object OnAIMoveImmediate : GameUsecase()
    object GameExitEvent : GameUsecase()
    object OnClearGameBoard : GameUsecase()
    object OnCancelPlayRequest : GameUsecase()
    object OnAcceptPlayAgainRequest : GameUsecase()
    data class OnBackPress(val backPressState: Boolean) : GameUsecase()
    data class OnDelayLaunch(val delayValue: Boolean) : GameUsecase()
    data class UpdateUserId(val userId: String) : GameUsecase()
    data class UpdateFriendUserId(val userId: String) : GameUsecase()
    data class OnUpdateGameSessionId(val sessionId: String) : GameUsecase()
    data class OnUpdateCurrentUserId(val currentUserId: String) : GameUsecase()
    data class OnUpdateInGameInfo(val value: Boolean) : GameUsecase()
    data class OnPlayerCountUpdate(val playerCount: PlayerCount) : GameUsecase()
}