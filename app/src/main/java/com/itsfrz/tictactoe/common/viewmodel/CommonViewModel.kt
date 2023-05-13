package com.itsfrz.tictactoe.common.viewmodel

import android.content.Context
import android.content.res.AssetManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.enums.PlayerCount
import com.itsfrz.tictactoe.common.state.EmojiState
import com.itsfrz.tictactoe.common.usecase.CommonUseCase
import com.itsfrz.tictactoe.emoji.usecase.EmojiPickerUseCase
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream


class CommonViewModel private constructor(): ViewModel() {

    private val TAG = "COMMON_VIEW_MODEL"

    private lateinit var gameStoreRepository: GameStoreRepository
    private lateinit var cloudRepository: CloudRepository
    private var userId: String = ""

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
    }

    fun registerViewModel(gameStoreRepository: GameStoreRepository,cloudRepository: CloudRepository,userId : String){
        instance?.let {
            it.gameStoreRepository = gameStoreRepository
            it.cloudRepository = cloudRepository
            it.userId = userId
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
}