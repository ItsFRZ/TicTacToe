package com.itsfrz.tictactoe.goonline.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Setting(
    @SerialName("currentTheme")val currentTheme : String? = null,
    @SerialName("language")val language : String? = null,
)