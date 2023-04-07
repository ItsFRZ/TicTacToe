package com.itsfrz.tictactoe.goonline.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.models.UserProfile
import kotlinx.coroutines.flow.Flow

class IGameStoreRepository(private val dataStore : DataStore<GameDataStore>) : GameStoreRepository {

    val TAG : String = "GAME_STORE"

    override suspend fun updateUserInfo(userId : String){
        try{
            dataStore.updateData {
                it.copy(
                    userId = userId
                )
            }
        }catch (e : Exception){
            Log.e(TAG, "updatePreference: updateUserInfo ${e.message}")
        }
    }

    override suspend fun updateUserProfile(userProfile: UserProfile) {
        try{
            Log.i(TAG, "updateUserProfile: ${userProfile}")
            dataStore.updateData { it.copy(userProfile = userProfile) }
        }catch (e : Exception){
            Log.e(TAG, "updatePreference: GameDataStore ${e.message}")
        }
    }

    override suspend fun updatePlayground(playground: Playground) {
        try{
            dataStore.updateData { it.copy(playGround = playground) }
        }catch (e : Exception){
            Log.e(TAG, "updatePreference: GameDataStore ${e.message}")
        }
    }

    override suspend fun fetchPreference(): Flow<GameDataStore> {
        return dataStore.data
    }
}