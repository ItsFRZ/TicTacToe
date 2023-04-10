package com.itsfrz.tictactoe.goonline.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Playground(
    @SerialName("userId")val userId : String? = "",
    @SerialName("totalWins")val totalWins : Long? = 0L,
    @SerialName("totalLose")val totalLose : Long? = 0L,
    @SerialName("inGame")val inGame : Boolean = false,
    @SerialName("online")val online : Boolean = false,
    @SerialName("totalCoins")val totalCoins : Long = 0L,
    @SerialName("activeRequest")val activeRequest : List<ActiveRequest> = emptyList(),
    @SerialName("friendList")val friendList : List<Friend> = emptyList()
){
    @Serializable
    data class ActiveRequest(
        @SerialName("friendUserId")val friendUserId: String = "",
        @SerialName("requesterUsername")val requesterUsername : String = "",
        @SerialName("online")val online : Boolean = false,
        @SerialName("playResponse")val playResponse : Boolean = false
    )

    @Serializable
    data class Friend(
        @SerialName("userId")val userId : String = "",
        @SerialName("online")val online : Boolean = false,
        @SerialName("username")val username : String = "",
        @SerialName("profileImage")val profileImage : String = "",
        @SerialName("playRequest")val playRequest : Boolean = false
    )
}
