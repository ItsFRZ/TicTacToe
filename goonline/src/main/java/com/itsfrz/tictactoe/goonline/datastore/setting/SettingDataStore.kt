package com.itsfrz.tictactoe.goonline.datastore.setting

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.itsfrz.tictactoe.goonline.common.GameTheme
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class SettingDataStore(
    @SerialName("music")val music : Boolean = true,
    @SerialName("sound")val sound : Boolean = true,
    @SerialName("vibration")val vibration: Boolean = true,
    @SerialName("notification")val notification: Boolean = true,
    @SerialName("theme")val theme : GameTheme = GameTheme.THEME_BLUE,
    @SerialName("coinInfo") val coinInfo : Int = 500,
    @SerialName("gameLevelInfo") val gameLevelInfo : Set<Int> = emptySet<Int>(),
    @SerialName("cashAmount") val cashAmount : Int = 0,
    @SerialName("language")val language: Pair<String,String>? = null

){
    @Keep
    companion object{
        private val settingDatastore : DataStore<SettingDataStore>? = null

        public fun getDataStore(context: Context) : DataStore<SettingDataStore> {
            if (settingDatastore == null)
                return context.settingStore
            return settingDatastore
        }

    }
}

val Context.settingStore by dataStore<SettingDataStore>("game-setting-store.json",
    GameSettingSerializer
)