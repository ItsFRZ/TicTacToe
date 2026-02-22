package com.itsfrz.tictactoe.common.viewmodel

import android.content.Context
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.common.enums.GameLanguage
import com.itsfrz.tictactoe.common.enums.GameTheme
import com.itsfrz.tictactoe.common.enums.PlayerCount
import com.itsfrz.tictactoe.common.functionality.GameSound
import com.itsfrz.tictactoe.common.state.EmojiState
import com.itsfrz.tictactoe.common.usecase.CommonUseCase
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingDataStore
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class CommonViewModel private constructor(): ViewModel() {

    private val TAG = "COMMON_VIEW_MODEL"

    private lateinit var gameStoreRepository: GameStoreRepository
    private lateinit var cloudRepository: CloudRepository
    lateinit var settingRepository : SettingRepository
    lateinit var gameSound: GameSound
    private var userId: String = ""
    public var music: Boolean = true
    public var sound: Boolean = true
    public var vibration: Boolean = true
    public var notification: Boolean = true
    public var theme: GameTheme = GameTheme.THEME_BLUE
    public var language: GameLanguage = GameLanguage.ENGLISH


    private var _emojiDataList : ArrayList<EmojiState> = arrayListOf()
    val emojiDataList = _emojiDataList

    private val _selectedEmojiList: MutableState<ArrayList<Int>> = mutableStateOf(arrayListOf())
    val selectedEmojiList : State<List<Int>> = _selectedEmojiList

    private val _playerCount : MutableState<Int> = mutableStateOf(0)
    val playerCount = _playerCount

    companion object {
        private var instance : CommonViewModel? = null
        private lateinit var gameStoreRepository: GameStoreRepository
        private lateinit var cloudRepository: CloudRepository

        private lateinit var dataStoreRepository : GameDataStore
        fun getInstance() : CommonViewModel{
            if (instance == null){
                synchronized(this){
                    if (instance == null){
                       instance = CommonViewModel()
                    }
                }
            }
            return instance!!
        }

        fun destructInstance() : Boolean{
            if (instance != null)
                instance == null
            return instance == null
        }
    }

    fun registerViewModel(gameStoreRepository: GameStoreRepository, cloudRepository: CloudRepository,settingRepository: SettingRepository, gameSound: GameSound, userId : String){
        instance?.let {
            it.gameStoreRepository = gameStoreRepository
            it.cloudRepository = cloudRepository
            it.userId = userId
            it.gameSound = gameSound
            it.settingRepository = settingRepository
        }
    }

    fun updateOnlineStatus(isOnline : Boolean){
        instance?.let {
            viewModelScope.launch(Dispatchers.IO) {
                if (userId.isNotEmpty()){
                    cloudRepository.updateOnlineStatus(isOnline,userId)
                }
            }
        } ?: println("Common ViewModel instance is not initialized")
    }

    fun loadEmojiData(context : Context){
        if (_emojiDataList.isNotEmpty())
            return
        try {
            for (fileNameCounter in 1..30){
                val resId : Int = context.resources.getIdentifier("ic_emoji_i"+fileNameCounter,"drawable",context.packageName)
                emojiDataList.add(EmojiState(resId,fileNameCounter-1,false));
            }
        }catch (e : Exception){
            Log.e(TAG, "loadEmojiData: Error in emoji data parsing")
            e.printStackTrace()
        }
    }

    fun releaseEmojiResources(){
        _emojiDataList.clear()
        _selectedEmojiList.value = arrayListOf()
        _playerCount.value = 0
    }

    fun onEvent(event : CommonUseCase){
        when(event){
            is CommonUseCase.OnSelectedEmojiChange -> {
                _selectedEmojiList.value.add(event.selectedIndex)
            }
            is CommonUseCase.OnRemovedEmojiChange -> {
                _selectedEmojiList.value.remove(event.removedIndex)
            }
            is CommonUseCase.OnPlayerCountUpdate -> {
                _playerCount.value = calculatePlayerCount(event.playerCount)
            }
            is CommonUseCase.ResetSelectEmojiData -> {
                _selectedEmojiList.value = arrayListOf()
            }
        }
        Log.i(TAG, "onEvent: Selected Emoji List ${_selectedEmojiList.value.size}")
        Log.i(TAG, "onEvent: Player Count ${_playerCount.value}")
    }

    private fun calculatePlayerCount(playerCount: PlayerCount): Int {
        return when(playerCount){
            PlayerCount.ONE -> 1
            PlayerCount.TWO -> 2
            PlayerCount.FOUR -> 4
        }
    }

    fun getResourceIdList(): List<Int> {
        val resourceIds = arrayListOf<Int>()
        _selectedEmojiList.value.forEach {
            resourceIds.add(_emojiDataList[it].emojiResourceId)
        }
        return resourceIds
    }

    fun performHapticVibrate(view : View){
        if (vibration) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    suspend fun loadUserPreference() : Unit{
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getGameSetting()?.collectLatest {
                music = it.music
                sound = it.sound
                vibration = it.vibration
                notification = it.notification
                gameSound.updateConditionAttributes(isMusicEnabled = it.music, isSoundEnabled = it.sound)
                gameSound.startBackgroundMusic()
            }
        }
    }
}