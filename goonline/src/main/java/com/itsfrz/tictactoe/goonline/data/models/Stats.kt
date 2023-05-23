package com.itsfrz.tictactoe.goonline.data.models

import kotlinx.serialization.SerialName

data class Stats(
    @SerialName("userId")val userId : String = "",
    @SerialName("username")val username : String = "",
    @SerialName("rank")val rank : Long = 0L,
    @SerialName("level")val level : Int = 0,
    @SerialName("playTime")val playTime : Int = 0,
    @SerialName("wins")val wins : Int = 0,
    @SerialName("lose")val lose : Int = 0,
    @SerialName("leaderBoard")val topPlayersList : List<Stats> = emptyList()
)
