package com.itsfrz.tictactoe.goonline.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Playground(
    @SerialName("userId")val userId : String? = "",
    @SerialName("totalWins")val totalWins : Long? = 0L,
    @SerialName("totalLose")val totalLose : Long? = 0L,
    @SerialName("isInGame")val isInGame : Boolean? = false,
    @SerialName("isOnline")val isOnline : Boolean? = false,
    @SerialName("boardState")val boardState: BoardState? = null,
    @SerialName("totalCoins")val totalCoins : Long? = 0L,
    @SerialName("activeRequest")val activeRequest : List<ActiveRequest>? = emptyList(),
    @SerialName("friendList")val friendList : List<Friend>? = emptyList()
){
    @Serializable
    data class ActiveRequest(
        @SerialName("friendUserId")val friendUserId: String? = null,
        @SerialName("friendRequestedTime")val friendRequestedTime : String? = null,
        @SerialName("playResponse")val playResponse : Boolean? = false
    )

    @Serializable
    data class Friend(
        @SerialName("userId")val userId : String? = null,
        @SerialName("username")val username : String? = null,
        @SerialName("profileImage")val profileImage : String? = null,
        @SerialName("playRequest")val playRequest : Boolean? = false
    )
}
