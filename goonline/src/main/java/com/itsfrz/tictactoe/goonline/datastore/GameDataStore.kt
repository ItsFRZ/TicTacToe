package com.itsfrz.tictactoe.goonline.datastore

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.models.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class GameDataStore(
    @SerialName("userProfile")val userProfile : UserProfile? = null,
    @SerialName("playGround")val playGround: Playground? = null,
    @SerialName("userId")val userId : String = userProfile?.userId ?: "",
){
    @Keep
    companion object{
        private val gameDataStore : DataStore<GameDataStore>? = null

        public fun getDataStore(context: Context) : DataStore<GameDataStore>{
            if (gameDataStore == null)
                return context.gameStore
            return gameDataStore
        }

    }
}

val Context.gameStore by dataStore<GameDataStore>("game-store.json",GameStoreSerializer)