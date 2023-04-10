package com.itsfrz.tictactoe.goonline.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoardState(
    @SerialName("playerOneState")val playerOneState : Player? = null,
    @SerialName("playerTwoState")val playerTwoState : Player? = null,
    @SerialName("currentUserTurnId")val currentUserTurnId : String = "",
    @SerialName("gameWinnerId") val gameWinnerId : String = ""
){
    @Serializable
    data class Player(
        @SerialName("userId")val userId : String = "",
        @SerialName("indexes")val indexes : List<Int> = emptyList()
    )
}