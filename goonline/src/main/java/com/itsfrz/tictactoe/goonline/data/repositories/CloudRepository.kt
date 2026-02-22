package com.itsfrz.tictactoe.goonline.data.repositories


import com.itsfrz.tictactoe.goonline.data.models.BoardState
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.models.UserProfile
import com.itsfrz.tictactoe.goonline.data.service.GameSessionService
import com.itsfrz.tictactoe.goonline.data.service.PlaygroundService
import com.itsfrz.tictactoe.goonline.data.service.UserProfileService
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CloudRepository(
    private val dataStoreRepository: GameStoreRepository,
    private val scope : CoroutineScope) : PlaygroundService, UserProfileService, GameSessionService {

    private val TAG : String = "CLOUD_REPO"

    override suspend fun updatePlayground(playground: Playground) {

    }

    override suspend fun fetchPlaygroundInfoAndStore(userId: String?){

    }

    override suspend fun updateUserProfile(userProfile: UserProfile) {

    }

    override suspend fun fetchAndStoreUserProfileInfo(userId : String?) {

    }

    suspend fun searchAndStoreFriend(userId : String,friendUserId : String?){
    }

    private suspend fun updateMyIdInFriendsPlayground(friendId : String) {
    }

    private suspend fun updateFriendDataInServer(userId : String,userProfile: UserProfile) {
    }
    suspend fun requestFriendToPlay(userProfile: UserProfile?,friendUserid : String){
    }

    suspend fun cancelFriendPlayRequest(friendUserId: String,userId : String) {
    }

    fun acceptFriendRequest(userId: String, requesterUserId: String) {
    }

    private suspend fun removeUserRequest(userId: String, requesterUserId: String) {
    }

    override suspend fun createGameSession(sessionId: String,friendUserId : String) {
    }

    override suspend fun updateGameBoard(sessionId : String,gameBoardState: BoardState) {
    }

    override suspend fun fetchGameBoardInfoAndStore(sessionId: String) {
    }

    override suspend fun removeGameBoard(sessionId: String) {
    }

    suspend fun toggleGameAttributes(userId: String,inGame : Boolean,randomSearch : Boolean){
    }


    override suspend fun playAgainRequest(gameSessionId : String,boardState : BoardState) {
    }

    override suspend fun acceptPlayAgainRequest(gameSessionId: String, boardState: BoardState) {
    }

    override suspend fun cancelPlayRequest(userId: String, friendUserId: String) {
    }

    override suspend fun updateOnlineStatus(isOnline : Boolean,userId: String?) {
    }

    private suspend fun updateMineOnlineStatusInFriendList(currentUserId : String,friendUserId: String,isOnline : Boolean) {
    }

    suspend fun searchRandomUser(userId: String){
    }

    private fun waitForRandomUser(sessionId: String) {
    }

    private suspend fun playWithRandomUser(playground: Playground, userId: String) {
    }

    fun searchAndfetchGameBoardInfoAndStore(userId: String) {
    }

}