package com.itsfrz.tictactoe.userregistration.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.models.UserProfile
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import com.itsfrz.tictactoe.userregistration.usecase.UserRegistrationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRegistrationViewModel(
    private val cloudRepository: CloudRepository,
    private val gameStoreRepository: GameStoreRepository
) : ViewModel(){

    private val _usernameValue : MutableState<String> = mutableStateOf("")
    val usernameValue : State<String> = _usernameValue

    private val _isUsernameEmpty : MutableState<Boolean> = mutableStateOf(true)
    val isUsernameEmpty : State<Boolean> = _isUsernameEmpty


    fun onEvent(event : UserRegistrationUseCase){
        when(event){
            is UserRegistrationUseCase.OnUsernameChange -> {
                _usernameValue.value = event.userInput
                _isUsernameEmpty.value = _usernameValue.value.isEmpty()
            }

            is UserRegistrationUseCase.OnSubmitButtonClick -> {
                if (!_isUsernameEmpty.value)
                    setupUser()
            }
        }
    }

    private fun setupUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val secret = "${(0..10000).random()}${_usernameValue.value.hashCode()}${System.currentTimeMillis()}"
            val userProfile = UserProfile(userId = secret.lowercase(), username = _usernameValue.value)
            launch(Dispatchers.IO) {
                cloudRepository.updateUserProfile(userProfile)
            }
            launch(Dispatchers.IO) {
                gameStoreRepository.updateUserInfo(secret.lowercase())
                gameStoreRepository.updateUserProfile(userProfile)
            }
            launch(Dispatchers.IO) {
                gameStoreRepository.updatePlayground(Playground(userId = secret.lowercase()))
            }
        }
    }

}