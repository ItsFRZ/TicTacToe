package com.itsfrz.tictactoe.home.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.home.usecase.HomePageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomePageViewModel(
    private val cloudRepository: CloudRepository,
    private val gameStoreRepository: GameStoreRepository
) : ViewModel(){

    private val _userId : MutableState<String> = mutableStateOf("")
    val userId : State<String> = _userId

    private val _isUsernameExists : MutableState<Boolean> = mutableStateOf(false)
    val isUsernameExists : State<Boolean> = _isUsernameExists

    private val _shareFriendDetails : MutableState<String> = mutableStateOf("")
    val shareFriendDetails : State<String> = _shareFriendDetails

    init {
        viewModelScope.launch(Dispatchers.IO) {
            gameStoreRepository.fetchPreference().collect{
                _userId.value = it.userId
            }

        }
    }

    fun onEvent(event : HomePageUseCase){
        when(event){
            is HomePageUseCase.OnCopyUserIdEvent -> {
                viewModelScope.launch(Dispatchers.IO) {
                     fetchUserId()
                }

            }
            else -> {}
        }
    }

    suspend fun fetchUserId() {
        if (_userId.value.isEmpty()){
            _userId.value = gameStoreRepository.getUserInfo()
        }
    }
}