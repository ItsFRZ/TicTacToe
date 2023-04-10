package com.itsfrz.tictactoe.goonline.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("userId")val userId : String = "",
    @SerialName("username")val username : String = "",
    @SerialName("profileImage")val profileImage : String = "",
    @SerialName("online") val online : Boolean = false,
    @SerialName("email")val email : String = "",
    @SerialName("location")val location : String = "",
    @SerialName("setting")val setting: Setting? = null
)