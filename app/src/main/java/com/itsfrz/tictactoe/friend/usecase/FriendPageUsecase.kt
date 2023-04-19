package com.itsfrz.tictactoe.friend.usecase

sealed class FriendPageUseCase {
    data class OnUserIdChange(val userInput : String) : FriendPageUseCase()
    object SearchUserEvent : FriendPageUseCase()
    object OnCancelPlayRequest : FriendPageUseCase()
    object RefreshPlaygroundData : FriendPageUseCase()
    data class OnRequestFriendEvent(val index : Int) : FriendPageUseCase()
    data class OnAcceptFriendRequestEvent(val index : Int) : FriendPageUseCase()
    data class OnUpdateGameSessionId(val sessionId : String) : FriendPageUseCase()
    data class OnRequestLoaderVisibilityToggle(val value : Boolean) : FriendPageUseCase()
    data class OnUpdateUserInGameInfo(val value : Boolean) : FriendPageUseCase()
}