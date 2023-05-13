package com.itsfrz.tictactoe.emoji.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.itsfrz.tictactoe.common.state.EmojiState
import com.itsfrz.tictactoe.emoji.usecase.EmojiPickerUseCase

class EmojiPickerViewModel : ViewModel() {

    private val TAG = "TAG"
    private val _emojiList: MutableState<List<EmojiState>> = mutableStateOf(listOf())
    val emojiList : State<List<EmojiState>> = _emojiList
    // Online Config
    private val _friendUserId: MutableState<String> = mutableStateOf("")
    val friendUserId = _friendUserId

    private val _userId: MutableState<String> = mutableStateOf("")
    val userId = _userId

    private val _currentUserId: MutableState<String> = mutableStateOf("")
    val currentUserId = _currentUserId

    private val _gameSessionId : MutableState<String> = mutableStateOf("")
    val gameSessionId = _gameSessionId



    fun onEvent(event : EmojiPickerUseCase){
       when(event){
           is EmojiPickerUseCase.OnSelectedEmojiChange -> {
               var data = _emojiList.value.get(event.selectedIndex)
               val tempList = arrayListOf<EmojiState>()
               tempList.addAll(_emojiList.value)
               tempList.removeAt(event.selectedIndex)
               data = data.copy(isSelected = true)
               tempList.add(event.selectedIndex,data)
               _emojiList.value = tempList

           }
           is EmojiPickerUseCase.OnRemovedEmojiChange -> {
               var data = _emojiList.value.get(event.removedIndex)
               val tempList = arrayListOf<EmojiState>()
               tempList.addAll(_emojiList.value)
               tempList.removeAt(event.removedIndex)
               data = data.copy(isSelected = false)
               tempList.add(event.removedIndex,data)
               _emojiList.value = tempList

           }
           is EmojiPickerUseCase.FillEmojiData -> {
               _emojiList.value = event.emojis
           }
           is EmojiPickerUseCase.OnUpdateCurrentUserId -> {
               _currentUserId.value = event.currentUserId
           }
           is EmojiPickerUseCase.OnUpdateGameSessionId -> {
               _gameSessionId.value = event.sessionId
           }
           is EmojiPickerUseCase.UpdateFriendUserId -> {
               _friendUserId.value = event.userId
           }
           is EmojiPickerUseCase.UpdateUserId -> {
               _userId.value = event.userId
           }
       }
    }



}