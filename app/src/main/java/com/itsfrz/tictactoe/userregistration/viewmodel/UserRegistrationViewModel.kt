package com.itsfrz.tictactoe.userregistration.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.common.functionality.Generate
import com.itsfrz.tictactoe.common.functionality.LocaleManager
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.models.UserProfile
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingRepository
import com.itsfrz.tictactoe.setting.viewmodel.SettingViewModel
import com.itsfrz.tictactoe.userregistration.usecase.UserRegistrationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.collections.listOf

class UserRegistrationViewModel(
    private val cloudRepository: CloudRepository,
    private val gameStoreRepository: GameStoreRepository,
    private val settingRepository: SettingRepository
) : ViewModel(){
    val supportedLanguages =  listOf(
        "English" to "en",
        "German" to "de",
        "Russian" to "ru",
        "French" to "fr",
        "Spanish" to "es",
        "Portuguese (Brazil)" to "pt-BR",
        "Italian" to "it",
        "Turkish" to "tr",
        "Polish" to "pl",
        "Dutch" to "nl",
        "Chinese (Simplified)" to "zh-CN",
        "Japanese" to "ja",
        "Korean" to "ko",
        "Ukrainian" to "uk",
        "Czech" to "cs",
        "Romanian" to "ro",
        "Hungarian" to "hu",
        "Indonesian" to "id",
        "Arabic" to "ar",
        "Hindi" to "hi"
    )
    private val _usernameValue : MutableState<String> = mutableStateOf("")
    val usernameValue : State<String> = _usernameValue

    private val _isUsernameEmpty : MutableState<Boolean> = mutableStateOf(true)
    val isUsernameEmpty : State<Boolean> = _isUsernameEmpty

    private val _languageExpanded : MutableState<Boolean> = mutableStateOf(false)
    val languageExpanded : State<Boolean> = _languageExpanded

    private val _selectedLanguage : MutableState<Pair<String,String>> = mutableStateOf(supportedLanguages.first())
    val selectedLanguage : State<Pair<String,String>> = _selectedLanguage

    fun onEvent(event : UserRegistrationUseCase){
        when(event){
            is UserRegistrationUseCase.OnUsernameChange -> {
                _usernameValue.value = event.userInput
                _isUsernameEmpty.value = _usernameValue.value.isEmpty()
            }
            is UserRegistrationUseCase.OnLanguageChange -> {
                _selectedLanguage.value = event.lang
                updateLangLocale()
            }
            is UserRegistrationUseCase.OnLangToggle -> {
                _languageExpanded.value = event.state
            }
            is UserRegistrationUseCase.OnSubmitButtonClick -> {
                if (!_isUsernameEmpty.value)
                    setupUser()
            }
        }
    }

    private fun updateLangLocale() {
        LocaleManager.updateLocale( _selectedLanguage.value.second)
        viewModelScope.launch(Dispatchers.IO) {
            val settingData = settingRepository.getGameSetting()?.firstOrNull()
            settingData?.let {
                settingRepository.updateGameSetting(settingData.copy(language = _selectedLanguage.value))
            }
        }
    }

    private fun setupUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val uuid = "${(0..10000).random()}${_usernameValue.value.hashCode()}${System.currentTimeMillis()}"
            val secret = Generate.uniqueId(uuid.lowercase())
            val userProfile = UserProfile(userId = secret, username = _usernameValue.value)
            val userPlayground = Playground().copy(userId = secret, online = true, randomSearch = false)
            launch(Dispatchers.IO) {
                gameStoreRepository.updateUserInfo(secret)
                gameStoreRepository.updateUserProfile(userProfile)
            }
            launch(Dispatchers.IO) {
                cloudRepository.updateUserProfile(userProfile)
                cloudRepository.updatePlayground(userPlayground)
            }
            launch(Dispatchers.IO) {
                gameStoreRepository.updatePlayground(Playground(userId = secret))
            }
            launch(Dispatchers.IO) {
                val settingData = settingRepository.getGameSetting()?.firstOrNull()
                settingData?.let {
                    settingRepository.updateGameSetting(settingData.copy(language = _selectedLanguage.value))
                }
            }
        }
    }

}