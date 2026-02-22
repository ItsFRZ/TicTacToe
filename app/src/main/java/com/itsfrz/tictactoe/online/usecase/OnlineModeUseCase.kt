package com.itsfrz.tictactoe.online.usecase

import com.itsfrz.tictactoe.friend.usecase.FriendPageUseCase
import com.itsfrz.tictactoe.game.domain.usecase.GameUsecase

sealed class OnlineModeUseCase{
    data class OnRandomPlayerSearch(val isEnabled : Boolean) : OnlineModeUseCase()
    data class OnUpdateUserInGameInfo(val value : Boolean) : OnlineModeUseCase()
    data class UpdateFriendUserId(val userId : String): OnlineModeUseCase()
    data class OnUpdateGameSessionId(val sessionId : String) : OnlineModeUseCase()
    object OnRemoveRandomModeConfig : OnlineModeUseCase()
}
