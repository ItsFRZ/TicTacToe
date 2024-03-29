package com.itsfrz.tictactoe.homepage.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import com.itsfrz.tictactoe.homepage.usecase.HomePageUseCase
import com.itsfrz.tictactoe.userregistration.usecase.UserRegistrationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HomePageViewModel(
    private val cloudRepository: CloudRepository,
    private val gameStoreRepository: GameStoreRepository
) : ViewModel(){

    private val _userId : MutableState<String> = mutableStateOf("")
    val userId : State<String> = _userId

    private val _isUsernameExists : MutableState<Boolean> = mutableStateOf(false)
    val isUsernameExists : State<Boolean> = _isUsernameExists

    init {
        viewModelScope.launch(Dispatchers.IO) {
            gameStoreRepository.fetchPreference().collect{
                _userId.value = it.userId
            }

        }
    }

    fun onEvent(event : HomePageUseCase){
        when(event){
            HomePageUseCase.OnCopyUserIdEvent -> {
                fetchUserId()
            }
            else -> {}
        }
    }

    private fun fetchUserId() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_userId.value.isEmpty()){
                _userId.value = async { gameStoreRepository.fetchUserInfo() }.await()
            }
        }
    }
}