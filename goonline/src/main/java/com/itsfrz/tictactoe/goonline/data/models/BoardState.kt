package com.itsfrz.tictactoe.goonline.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoardState(
    @SerialName("playerOneState")val playerOneState : Player? = null,
    @SerialName("playerTwoState")val playerTwoState : Player? = null,
    @SerialName("currentUserTurnId")val currentUserTurnId : String = "",
    @SerialName("gameWinnerId") val gameWinnerId : String = "",
    @SerialName("resetTimer") val resetTimer : Boolean = false,
    @SerialName("gameDraw") val gameDraw : Boolean = false,
    @SerialName("playAgain") val playAgain : PlayRequest? = null,

){
    @Serializable
    data class Player(
        @SerialName("userId")val userId : String = "",
        @SerialName("indexes")val indexes : List<Int> = emptyList()
    )

    @Serializable
    data class PlayRequest(
        @SerialName("requesterId")val requesterId : String = "",
        @SerialName("retryRequest")val retryRequest : Boolean = false,
        @SerialName("acceptRequest") val acceptRequest : Boolean = false
    )
}