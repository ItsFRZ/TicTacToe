package com.itsfrz.tictactoe.goonline.data.repositories

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.itsfrz.tictactoe.goonline.common.Constants
import com.itsfrz.tictactoe.goonline.data.firebase.FirebaseDB
import com.itsfrz.tictactoe.goonline.data.models.BoardState
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.models.UserProfile
import com.itsfrz.tictactoe.goonline.data.service.GameSessionService
import com.itsfrz.tictactoe.goonline.data.service.PlaygroundService
import com.itsfrz.tictactoe.goonline.data.service.UserProfileService
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CloudRepository(
    private val database: FirebaseDB,
    private val dataStoreRepository: GameStoreRepository,
    private val scope : CoroutineScope) : PlaygroundService, UserProfileService, GameSessionService {

    private val TAG : String = "CLOUD_REPO"

    override suspend fun updatePlayground(playground: Playground) {
        try {
            if (playground.userId.isNullOrEmpty())
                throw Exception("UserId should not be null or empty")

            database.getReference()
                .child(Constants.USER_PLAYGROUND)
                .child(playground.userId)
                .setValue(playground)
        }catch (e : FirebaseException){
            Log.e(TAG, "updatePlayground: ${e.message}")
        }
    }

    override suspend fun fetchPlaygroundInfoAndStore(userId: String?){
        try {
            Log.i(TAG, "fetchPlaygroundInfoAndStore: ${userId}")
            if (userId.isNullOrEmpty())
                throw Exception("UserId should not be null or empty")
            Log.i(TAG, "fetchPlaygroundInfoAndStore: ${userId}")
            database.getPlayGroundReference("${Constants.USER_PLAYGROUND}/${userId}")
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userPlayground = snapshot.getValue<Playground>()
                        Log.i(TAG, "fetchPlaygroundInfoAndStore: ${userPlayground}")
                        if(userPlayground!=null){
                            scope.launch(Dispatchers.IO) {
                                dataStoreRepository.updatePlayground(userPlayground)
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "onCancelled: ${error.message}")
                    }
                })
        }catch (e : FirebaseException){
            Log.e(TAG, "fetchPlaygroundInfoAndStore: ${e.message}")
        }
    }

    override suspend fun updateUserProfile(userProfile: UserProfile) {
        try {
            if (userProfile.userId.isNullOrEmpty())
                throw Exception("UserId should not be null or empty")

            database.getReference()
                .child(Constants.USER_PROFILE)
                .child(userProfile.userId)
                .setValue(userProfile)
        }catch (e : FirebaseException){
            Log.e(TAG, "updateUserProfile: ${e.message}")
        }
    }

    override suspend fun fetchAndStoreUserProfileInfo(userId : String?) {
        try {
            if (userId.isNullOrEmpty())
                throw Exception("UserId should not be null or empty")
            database.getProfileReference("${Constants.USER_PROFILE}/${userId}")
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userProfile = snapshot.getValue<UserProfile>()
                            ?: throw Exception("Userprofile Null From Firebase")
                        scope.launch(Dispatchers.IO) {
                            dataStoreRepository.updateUserProfile(userProfile)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "onCancelled: ${error.message}")
                    }
                })
        }catch (e : FirebaseException){
            Log.e(TAG, "fetchUserProfileInfo: ${e.message}")
        }
    }

    suspend fun searchAndStoreFriend(userId : String,friendUserId : String?){
        try {
            Log.i(TAG, "searchAndStoreFriend: Search Started For Friend UserId ${friendUserId}")
            if (friendUserId.isNullOrEmpty())
                throw Exception("Friend UserId should not be null or empty")
            database.getProfileReference(Constants.USER_PROFILE)
                .orderByChild("userId")
                .equalTo(friendUserId)
                .limitToFirst(1)
                .addChildEventListener(object : ChildEventListener{
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        Log.i(TAG, "searchAndStoreFriend: Search Snapshot ${snapshot.value}")
                        val userProfile : UserProfile? = snapshot.getValue(UserProfile::class.java)
                        userProfile?.let {
                            scope.launch(Dispatchers.IO) {
                                dataStoreRepository.updateFriendData(userProfile)
                                updateFriendDataInServer(userId,userProfile)
                            }
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        Log.i(TAG, "searchAndStoreFriend: Search Snapshot ${snapshot.value}")
                        val userProfile : UserProfile? = snapshot.getValue(UserProfile::class.java)
                        userProfile?.let {
                            scope.launch(Dispatchers.IO) {
                                dataStoreRepository.updateFriendData(userProfile)
                                updateFriendDataInServer(userId,userProfile)
                            }
                        }
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "searchAndStoreFriend: Search Result Not Found")
                    }
                })

            updateMyIdInFriendsPlayground(friendUserId)
        }catch (e : FirebaseException){
            Log.e(TAG, "fetchUserProfileInfo: ${e.message}")
        }
    }

    private suspend fun updateMyIdInFriendsPlayground(friendId : String) {
        val myProfile = dataStoreRepository.fetchPreference().firstOrNull()
        myProfile?.let { myProfile ->
            database.getReference(Constants.USER_PLAYGROUND).child(friendId)
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var userPlayground = snapshot.getValue<Playground>()
                        userPlayground?.let {
                            val friendList = arrayListOf<Playground.Friend>()
                            if (it.friendList.isNotEmpty())
                                friendList.addAll(it.friendList)

                            friendList.forEach {
                                if (it.userId == myProfile.userId)
                                    return
                            }
                            myProfile.also {
                                friendList.add(Playground.Friend(userId = it.userId,online = true, username = it.userProfile?.username ?: "",))
                            }
                            userPlayground = it.copy(friendList = friendList)
                            database.getReference(Constants.USER_PLAYGROUND).child(friendId)
                                .setValue(userPlayground)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "onCancelled: ${error.message}")
                    }
                })
        }
    }

    private suspend fun updateFriendDataInServer(userId : String,userProfile: UserProfile) {
        val dataStore = dataStoreRepository.fetchPreference().firstOrNull()
        dataStore?.let {
            var playground : Playground? = null
            if (it.playGround == null){
                playground = Playground()
            } else playground = it.playGround
            val friendList = mutableListOf<Playground.Friend>()
            if (!playground.friendList.isNullOrEmpty()){
                friendList.addAll(playground.friendList!!)
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
            }
            playground = playground.copy(userId = userId,friendList = friendList)
            updatePlayground(playground)
        }
    }

    // request the friend to play + create separate game session
//    suspend fun requestFriendToPlay(userProfile: UserProfile?,friendUserid : String){
//        userProfile?.let { currentUser ->
//            val activeRequest = Playground.ActiveRequest(
//                friendUserId = userProfile.userId,
//                requesterUsername = userProfile.username,
//                online = true,
//                playResponse = false
//            )
//            database.getPlayGroundReference("${Constants.USER_PLAYGROUND}").child("${friendUserid}")
//                .addListenerForSingleValueEvent(object : ValueEventListener{
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        Log.i(TAG, "requestFriendToPlay: ${snapshot}")
//                        var friendPlayground = snapshot.getValue(Playground::class.java)
//                        friendPlayground?.let {
//                            val activeRequestList = it.fr.toMutableList()
//                            activeRequestList.add(0,activeRequest)
//                            friendPlayground = it.copy(
//                                activeRequest = activeRequestList
//                            )
//                            scope.launch {
//                                database.getPlayGroundReference(Constants.USER_PLAYGROUND).child(friendUserid)
//                                    .setValue(friendPlayground)
//                            }
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//                })
//        }
//    }

    suspend fun requestFriendToPlay(userProfile: UserProfile?,friendUserid : String){
        userProfile?.let { currentUser ->
            val userId = userProfile.userId
            database.getPlayGroundReference(Constants.USER_PLAYGROUND).child(friendUserid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.i(TAG, "requestFriendToPlay: ${snapshot}")
                        var friendPlayground = snapshot.getValue(Playground::class.java)
                        friendPlayground?.let {
                            var friendList = it.friendList.toMutableList()
                            var friendItem = friendList.find { it.userId == userId } ?: return
                            friendList.remove(friendItem)
                            friendItem = friendItem.copy(playRequest = true)
                            friendList.add(friendItem)
                            friendPlayground = it.copy(
                                friendList = friendList
                            )
                            scope.launch {
                                database.getPlayGroundReference(Constants.USER_PLAYGROUND).child(friendUserid)
                                    .setValue(friendPlayground)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "onCancelled: ${error.message}")
                    }
                })
        }
    }

    suspend fun cancelFriendPlayRequest(friendUserId: String,userId : String) {
        database.getPlayGroundReference("${Constants.USER_PLAYGROUND}").child("${friendUserId}")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(TAG, "cancelFriendPlayRequest: ${snapshot}")
                    var friendPlayground = snapshot.getValue(Playground::class.java)
                    Log.i(TAG, "cancelFriendPlayRequest: Friend User Id ${friendUserId}")
                    friendPlayground?.let {
                        var friendList = it.friendList.toMutableList()
                        var friendItem = friendList.find { it.userId == userId } ?: return
                        friendList.remove(friendItem)
                        friendItem = friendItem.copy(playRequest = false)
                        friendList.add(friendItem)
                        friendPlayground = it.copy(
                            friendList = friendList,
                            inGame = false
                        )
                        scope.launch {
                            database.getPlayGroundReference(Constants.USER_PLAYGROUND).child(friendUserId)
                                .setValue(friendPlayground)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.message}")
                }
            })
    }

    fun acceptFriendRequest(userId: String, requesterUserId: String) {
        database.getPlayGroundReference("${Constants.USER_PLAYGROUND}").child("${requesterUserId}")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(TAG, "cancelFriendPlayRequest: ${snapshot}")
                    var friendPlayground = snapshot.getValue(Playground::class.java)
                    Log.i(TAG, "cancelFriendPlayRequest: Friend User Id ${requesterUserId}")
                    // set ingame true
                    friendPlayground?.let {
                        friendPlayground = it.copy(
                            inGame = true
                        )
                        scope.launch {
                            database.getPlayGroundReference(Constants.USER_PLAYGROUND).child(requesterUserId)
                                .setValue(friendPlayground)
                        }
                    }

                    // remove current play request from my db
                    scope.launch(Dispatchers.IO) {
                        removeUserRequest(userId,requesterUserId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private suspend fun removeUserRequest(userId: String, requesterUserId: String) {
        database.getPlayGroundReference("${Constants.USER_PLAYGROUND}").child("${userId}")
            .addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var myPlayground = snapshot.getValue(Playground::class.java)
                Log.i(TAG, "cancelFriendPlayRequest: Friend User Id ${requesterUserId}")
                // set ingame true
                myPlayground?.let {


                        var friendList = it.friendList.toMutableList()
                        var friendItem = friendList.find { it.userId == requesterUserId } ?: return
                        friendList.remove(friendItem)
                        friendItem = friendItem.copy(playRequest = false)
                        friendList.add(friendItem)
                        myPlayground = it.copy(
                            friendList = friendList,
                            inGame = true
                        )
                        scope.launch {
                            database.getPlayGroundReference(Constants.USER_PLAYGROUND).child(userId)
                                .setValue(myPlayground)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override suspend fun createGameSession(sessionId: String,friendUserId : String) {
        try { // create common game session
            scope.launch(Dispatchers.IO) {
                val playerOneState = BoardState.Player(userId = sessionId, indexes = emptyList())
                val playerTwoState = BoardState.Player(userId = friendUserId, indexes = emptyList())
                val gameBoard = BoardState(
                    currentUserTurnId = sessionId,
                    playerOneState = playerOneState,
                    playerTwoState = playerTwoState
                )
                database.getReference("${Constants.GAME_SESSION}").child(sessionId).setValue(gameBoard)
            }
        }catch (e : FirebaseException){
            Log.e(TAG, "createGameSession: ${e.message}")
        }
    }

    override suspend fun updateGameBoard(sessionId : String,gameBoardState: BoardState) {
        try {
            scope.launch(Dispatchers.IO) {
                database.getReference(Constants.GAME_SESSION).child(sessionId).setValue(gameBoardState)
            }
        }catch (e : FirebaseException){
            Log.e(TAG, "updateGameBoard: ${e.message}")
        }
    }

    override suspend fun fetchGameBoardInfoAndStore(sessionId: String) {
        database.getReference(Constants.GAME_SESSION)
            .child(sessionId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                   try {
                       val boardState = snapshot.getValue(BoardState::class.java)
                           ?: throw Exception("Game Board is Null")
                       boardState?.let {
                           scope.launch(Dispatchers.IO) {
                               dataStoreRepository.updateBoardState(it)
                           }
                       }
                   }catch (e : java.lang.Exception){
                       Log.e(TAG, "fetchGameBoardInfoAndStore: ${e.message}")
                   }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    override suspend fun removeGameBoard(sessionId: String) {
        try { // remove game session
            scope.launch(Dispatchers.IO) {
                database.getReference("${Constants.GAME_SESSION}").child(sessionId).removeValue()
                val data = dataStoreRepository.fetchPreference().firstOrNull()
                data?.let { it ->
                    it.playGround?.let {
                        updatePlayground(it.copy(inGame = false))
                    }
                }
            }
            scope.launch(Dispatchers.IO) {
                dataStoreRepository.clearGameBoard()
            }
        }catch (e : FirebaseException){
            Log.e(TAG, "createGameSession: ${e.message}")
        }
    }

    override suspend fun playAgainRequest(gameSessionId : String,boardState : BoardState) {
        scope.launch(Dispatchers.IO) {
            database.getReference(Constants.GAME_SESSION).child(gameSessionId).setValue(boardState)
        }
        scope.launch {
            dataStoreRepository.clearGameBoard()
        }
    }

    override suspend fun acceptPlayAgainRequest(gameSessionId: String, boardState: BoardState) {
        scope.launch {
            database.getPlayGroundReference(Constants.GAME_SESSION).child(gameSessionId).setValue(boardState)
        }
        scope.launch {
            dataStoreRepository.clearGameBoard()
        }
    }

    override suspend fun cancelPlayRequest(userId: String, friendUserId: String) {
        scope.launch {
            dataStoreRepository.clearGameBoard()
        }
    }

    override suspend fun updateOnlineStatus(isOnline : Boolean,userId: String?) {
        scope.launch {
            dataStoreRepository.updateOnlineStatus(isOnline)
            val userProfile = dataStoreRepository.getUserProfile()
            userProfile?.let { profile ->
                userId?.let { id ->
                    database.getReference(Constants.USER_PROFILE).child(id).setValue(profile)
                }
            }
        }
    }
}