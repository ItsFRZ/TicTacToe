package com.itsfrz.tictactoe.friend.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.friend.usecase.FriendPageUseCase
import com.itsfrz.tictactoe.game.domain.usecase.GameUsecase
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.models.UserProfile
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class FriendPageViewModel(
    private val cloudRepository: CloudRepository,
    private val gameStoreRepository: GameStoreRepository
) : ViewModel(){

    private val TAG = "FRIEND_VM"

    private val _userId : MutableState<String> = mutableStateOf("")
    val userId = _userId

    private val _userProfile : MutableState<UserProfile> = mutableStateOf(UserProfile())
    val userProfile = _userProfile

    private val _usernameSearchId : MutableState<String> = mutableStateOf("")
    val usernameSearchId : State<String> = _usernameSearchId

    private val _friendList : MutableState<List<Playground.Friend>> = mutableStateOf(emptyList())
    val friendList = _friendList

    private val _playRequestList : MutableState<List<Playground.ActiveRequest>> = mutableStateOf(emptyList())
    val playRequestList = _playRequestList

    private val _loaderState : MutableState<Boolean> = mutableStateOf(false)
    val loaderState : State<Boolean> = _loaderState

    private val _playRequestLoader : MutableState<Boolean> = mutableStateOf(false)
    val playRequestLoader : State<Boolean> = _playRequestLoader

    private val _friendRequestId : MutableState<String> = mutableStateOf("")
    val friendRequestId = _friendRequestId


    private val _gameSessionId : MutableState<String> = mutableStateOf("")
    val gameSessionId = _gameSessionId

    private val _inGameState : MutableState<Boolean> = mutableStateOf(false)
    val inGameState : State<Boolean> = _inGameState

    private var job : Job? = null

    fun onEvent(event : FriendPageUseCase){
        when(event){
            is FriendPageUseCase.OnUserIdChange -> {
                _usernameSearchId.value = event.userInput
            }
            is FriendPageUseCase.SearchUserEvent -> {
                searchAndAddUser()
            }
            is FriendPageUseCase.OnRequestFriendEvent -> {
                requestFriendToPlay(event.index)
            }
            is FriendPageUseCase.OnAcceptFriendRequestEvent -> {
                acceptFriendRequest(event.index)
            }
            is FriendPageUseCase.OnCancelPlayRequest -> {
                job?.cancel()
                cancelFriendPlayRequest()
                _playRequestLoader.value = false
            }
            is FriendPageUseCase.OnUpdateGameSessionId -> {
                _gameSessionId.value = event.sessionId
            }
            is FriendPageUseCase.OnRequestLoaderVisibilityToggle -> {
                _playRequestLoader.value = event.value
            }
            is FriendPageUseCase.OnUpdateUserInGameInfo -> {
                _inGameState.value = event.value
            }
            is FriendPageUseCase.RefreshPlaygroundData -> {
            }
            else -> {}
        }
    }

    private fun cancelFriendPlayRequest() {
        if (_friendRequestId.value.isNotEmpty()){
            viewModelScope.launch(Dispatchers.IO) {
                cloudRepository.cancelFriendPlayRequest(_friendRequestId.value,userId.value)
            }
        }
    }

    private fun acceptFriendRequest(index : Int) {
        val requesterUserId = _friendList.value[index].userId
        onEvent(FriendPageUseCase.OnUpdateGameSessionId(requesterUserId))
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.acceptFriendRequest(userId.value,requesterUserId)
        }
    }

    private fun requestFriendToPlay(index : Int) {
        job?.cancel()
        _playRequestLoader.value = true
        _friendRequestId.value = friendList.value[index].userId
        onEvent(FriendPageUseCase.OnUpdateGameSessionId(userProfile.value.userId))
        job = viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.requestFriendToPlay(userProfile.value,_friendRequestId.value)
            cloudRepository.createGameSession(userProfile.value.userId,_friendRequestId.value)
        }
    }

    private fun searchAndAddUser() {
        _loaderState.value = true
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.searchAndStoreFriend(_userId.value,_usernameSearchId.value)
            _loaderState.value = false
        }
    }

    fun setupUserDetail(user: UserProfile?) {
        user?.let {
            _userId.value = it.userId
            _userProfile.value = it
        }
    }

    fun updateFriendList(friendList: List<Playground.Friend>?) {
        friendList?.let {
            Log.i(TAG, "updateFriendList: ${friendList}")
            _friendList.value = friendList.sortedWith(compareBy<Playground.Friend>({!it.online}).thenBy { it.username }).toSet().toList().filter { it.userId != userId.value }
        }
    }

    fun updateActiveRequestList(activeRequest: List<Playground.ActiveRequest>?) {
        activeRequest?.let {
            Log.i(TAG, "updateActiveRequestList: ${activeRequest}")
            _playRequestList.value = activeRequest.sortedWith(compareBy<Playground.ActiveRequest>({!it.online}).thenBy { it.requesterUsername }).toSet().toList()
        }
    }

}