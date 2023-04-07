package com.itsfrz.tictactoe.goonline.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("userId")val userId : String? = null,
    @SerialName("username")val username : String? = null,
    @SerialName("profileImage")val profileImage : String? = null,
    @SerialName("email")val email : String? = null,
    @SerialName("location")val location : String? = null,
    @SerialName("setting")val setting: Setting? = null
)