package com.itsfrz.tictactoe.goonline.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import com.itsfrz.tictactoe.goonline.data.models.BoardState
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

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

    override suspend fun fetchUserInfo() : String {
        return dataStore.data.firstOrNull()?.userId ?: ""
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

    override suspend fun updateFriendData(userProfile: UserProfile) {
        Log.i(TAG, "updateFriendData: Inside Function Call")
        val datastore = dataStore.data.firstOrNull()
        datastore?.let {
            val friendList = mutableListOf<Playground.Friend>()
            if (!it.playGround?.friendList.isNullOrEmpty()){
                friendList.addAll(it.playGround?.friendList!!)
            }
            friendList.let{
                val index = it.indexOfFirst { it.userId == userProfile.userId  }
                if (index != -1) {
                    friendList.removeAt(index)
                }
                val friend = Playground.Friend(
                    userId = userProfile.userId,
                    online = userProfile.online,
                    username = userProfile.username,
                    profileImage = userProfile.profileImage,
                    playRequest = false
                )
                friendList.add(friend)
                updateFriendList(friendList)
            }
        }
    }

    private suspend fun updateFriendList(friends: List<Playground.Friend>){
        try {
            val dataStoreValue = dataStore.data.firstOrNull()
            dataStoreValue?.let {
                var playground : Playground = it.playGround ?: Playground()
                playground = playground.copy(friendList = friends)
                Log.i(TAG, "updateFriendList: ${playground}")
                dataStore.updateData {
                    it.copy(playGround = playground)
                }
            }

        }catch (e : Exception){
            Log.e(TAG, "updateFriendList: GameDataStore ${e.message}")
        }
    }

    override suspend fun clearPlayground(userId: String) {
        dataStore.updateData {
            it.copy(playGround = Playground(userId = userId))
        }
    }

    override suspend fun clearGameBoard() {
        try {
            val dataStoreValue = dataStore.data.firstOrNull()
            dataStoreValue?.let {
                var playground : Playground = it.playGround ?: Playground()
                playground = playground.copy(inGame = false)
                Log.i(TAG, "clearGameBoard: ${playground}")
                dataStore.updateData {
                    it.copy(playGround = playground, boardState = BoardState())
                }
            }
        }catch (e : Exception){
            Log.e(TAG, "clearGameBoard: GameDataStore ${e.message}")
        }
    }


    override suspend fun updateBoardState(boardState: BoardState) {
        try {
            val dataStoreValue = dataStore.data.firstOrNull()
            dataStoreValue?.let {
                Log.i(TAG, "updateBoardState: ${boardState}")
                dataStore.updateData {
                    it.copy(boardState = boardState)
                }
            }
        }catch (e : Exception){
            Log.e(TAG, "updateBoardState: GameDataStore ${e.message}")
        }
    }

}