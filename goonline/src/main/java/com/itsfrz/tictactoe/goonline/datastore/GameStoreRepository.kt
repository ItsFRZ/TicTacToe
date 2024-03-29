package com.itsfrz.tictactoe.goonline.datastore

import com.itsfrz.tictactoe.goonline.data.models.BoardState
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface GameStoreRepository {
    suspend fun updateUserInfo(userId : String)
    suspend fun fetchUserInfo() : String
    suspend fun updateUserProfile(userProfile : UserProfile)
    suspend fun updatePlayground(playground: Playground)
    suspend fun fetchPreference(): Flow<GameDataStore>
    suspend fun updateFriendData(userProfile: UserProfile)
    suspend fun clearPlayground(userId: String)
    suspend fun updateBoardState(boardState: BoardState)
    suspend fun clearGameBoard()
}