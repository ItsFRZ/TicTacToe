package com.itsfrz.tictactoe.setting.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingRepository
import com.itsfrz.tictactoe.common.enums.SettingType
import com.itsfrz.tictactoe.goonline.common.GameTheme
import com.itsfrz.tictactoe.setting.usecase.SettingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


class SettingViewModel(

) : ViewModel() {


    companion object{
        private var instance : SettingViewModel? = null
        private lateinit var settingRepository: SettingRepository
        fun registerSettingViewModel(settingRepository: SettingRepository){
            this.settingRepository = settingRepository
        }

        fun getInstance() : SettingViewModel{
            if (instance == null){
                synchronized(this){
                    if (instance == null) {
                        instance = SettingViewModel()
                    }
                }
            }
            return instance!!
        }

    }

    private val _backgroundMusic : MutableState<Boolean> = mutableStateOf(false)
    val backgroundMusic: State<Boolean> = _backgroundMusic

    private val _gameSound : MutableState<Boolean> = mutableStateOf(false)
    val gameSound: State<Boolean> = _gameSound

    private val _systemVibration : MutableState<Boolean> = mutableStateOf(false)
    val systemVibration: State<Boolean> = _systemVibration

    private val _gameNotification : MutableState<Boolean> = mutableStateOf(false)
    val gameNotification: State<Boolean> = _gameNotification

    private val _systemLanguageIndex : MutableState<Int> = mutableStateOf(0)
    val systemLanguageIndex: State<Int> = _systemLanguageIndex

    private val _backgroundColorIndex : MutableState<Int> = mutableStateOf(0)
    val backgroundColorIndex: State<Int> = _backgroundColorIndex

    private val _settingType : MutableState<SettingType> = mutableStateOf(SettingType.COLOR)
    val settingType: State<SettingType> = _settingType

    init {
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getGameSetting()?.collectLatest {
                _backgroundMusic.value = it.music
                _gameSound.value = it.sound
                _systemVibration.value = it.vibration
                _gameNotification.value = it.notification
                _backgroundColorIndex.value = getGameThemeFromEnum(it.theme)
            }
        }
    }

    fun onEvent(event : SettingUseCase){
        when(event){
            is SettingUseCase.OnColorClickEvent -> {
                _backgroundColorIndex.value = event.colorIndex
                updateSettings(SettingType.COLOR,event.colorIndex)
            }
            is SettingUseCase.OnLanguageClickEvent -> {
                _systemLanguageIndex.value = event.langIndex
            }
            is SettingUseCase.OnMusicToggle -> {
                _backgroundMusic.value = event.toggle
                updateSettings(SettingType.MUSIC,event.toggle)
            }
            is SettingUseCase.OnNotificationToggle -> {
                _gameNotification.value = event.toggle
                updateSettings(SettingType.NOTIFICATION,event.toggle)
            }
            is SettingUseCase.OnSoundToggle -> {
                _gameSound.value = event.toggle
                updateSettings(SettingType.SOUND,event.toggle)
            }
            is SettingUseCase.OnVibrationToggle -> {
                _systemVibration.value = event.toggle
                updateSettings(SettingType.VIBRATION,event.toggle)
            }
            is SettingUseCase.OnUpdateSettingType -> {
                _settingType.value = event.settingType
            }
        }
    }

    fun updateSettings(settingType : SettingType, assignValue : Any){
        viewModelScope.launch(Dispatchers.IO) {
            val settingData = settingRepository.getGameSetting()?.firstOrNull()
            settingData?.let {
                when(settingType){
                    SettingType.MUSIC -> {
                        settingRepository.updateGameSetting(it.copy(music = assignValue as Boolean))
                    }
                    SettingType.SOUND -> {
                        settingRepository.updateGameSetting(it.copy(sound = assignValue as Boolean))
                    }
                    SettingType.VIBRATION -> {
                        settingRepository.updateGameSetting(it.copy(vibration = assignValue as Boolean))
                    }
                    SettingType.NOTIFICATION -> {
                        settingRepository.updateGameSetting(it.copy(notification = assignValue as Boolean))
                    }
                    SettingType.COLOR -> {
                        settingRepository.updateGameSetting(it.copy(theme = getGameTheme(_backgroundColorIndex.value)))
                    }
                    SettingType.LANGUAGE -> {}
                }
            }
        }
    }

    private fun getGameTheme(value: Int): GameTheme {
        return when(value){
            0 -> GameTheme.THEME_BLUE
            1 -> GameTheme.DARK_RED
            2 -> GameTheme.POPPY_ORANGE
            3 -> GameTheme.DRACULA_GREEN
            else -> GameTheme.THEME_BLUE
        }
    }

    private fun getGameThemeFromEnum(value: GameTheme): Int {
        return when(value){
            GameTheme.THEME_BLUE -> 0
            GameTheme.DARK_RED -> 1
            GameTheme.POPPY_ORANGE -> 2
            GameTheme.DRACULA_GREEN -> 3
            else -> 0
        }
    }

}