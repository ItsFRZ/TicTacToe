package com.itsfrz.tictactoe.online.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.goonline.data.models.UserProfile
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.online.usecase.OnlineModeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class OnlineModeViewModel(
    private val cloudRepository: CloudRepository,
    private val gameStoreRepository: GameStoreRepository
) : ViewModel(){

    private val _userId : MutableState<String> = mutableStateOf("")
    val userId = _userId

    private val _playerSearchState : MutableState<Boolean> = mutableStateOf(false)
    val playerSearchState : State<Boolean> = _playerSearchState

    private val _inGameState : MutableState<Boolean> = mutableStateOf(false)
    val inGameState : State<Boolean> = _inGameState

    private val _friendRequestId : MutableState<String> = mutableStateOf("")
    val friendRequestId = _friendRequestId


    private val _gameSessionId : MutableState<String> = mutableStateOf("")
    val gameSessionId = _gameSessionId


    private var job : Job? = null


    fun onEvent(event : OnlineModeUseCase){
        when(event){
            is OnlineModeUseCase.OnRandomPlayerSearch -> {
                _playerSearchState.value = event.isEnabled
                searchPlayerAndConnect()
            }
            is OnlineModeUseCase.OnUpdateUserInGameInfo -> {
                _inGameState.value = event.value
                fetchGameSessionData()
            }
            is OnlineModeUseCase.UpdateFriendUserId -> {
                _friendRequestId.value = event.userId
            }
            is OnlineModeUseCase.OnUpdateGameSessionId -> {
                _gameSessionId.value = event.sessionId
            }
            is OnlineModeUseCase.OnRemoveRandomModeConfig -> {
                clearGameSession()
            }
        }
    }

    private fun fetchGameSessionData() {
        cloudRepository.searchAndfetchGameBoardInfoAndStore(userId.value)
    }

    fun setupUserDetail(user: UserProfile?) {
        user?.let {
            _userId.value = it.userId
        }
    }
    private fun searchPlayerAndConnect() {
       if (_playerSearchState.value){
           job?.cancel()
           job = viewModelScope.launch(Dispatchers.IO) {
               cloudRepository.toggleGameAttributes(_userId.value,inGame = false,randomSearch = true)
               cloudRepository.searchRandomUser(_userId.value)
           }
       }
    }

    fun clearGameSession(){
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.toggleGameAttributes(_userId.value,inGame = false,randomSearch = false)
            cloudRepository.removeGameBoard(_userId.value)
            gameStoreRepository.clearGameBoard()
        }
    }

}