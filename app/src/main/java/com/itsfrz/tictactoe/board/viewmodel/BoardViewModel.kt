package com.itsfrz.tictactoe.online.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.itsfrz.tictactoe.board.usecase.SelectBoardUseCase
import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameLevel
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.PlayerCount

class BoardViewModel : ViewModel(){

    private val _level : MutableState<GameLevel> = mutableStateOf(GameLevel.NONE)
    val level : State<GameLevel> = _level

    private val _gameMode : MutableState<GameMode> = mutableStateOf(GameMode.AI)
    val gameMode : State<GameMode> = _gameMode

    private val _boardType : MutableState<BoardType> = mutableStateOf(BoardType.THREEX3)
    val boardType : State<BoardType> = _boardType

    private val _playerCount : MutableState<PlayerCount> = mutableStateOf(PlayerCount.ONE)
    val playerCount : State<PlayerCount> = _playerCount

    private val _selectedIndex : MutableState<Int> = mutableStateOf(-1)
    val selectedIndex : State<Int> = _selectedIndex


    fun onEvent(event : SelectBoardUseCase){
        when(event){
            is SelectBoardUseCase.OnBoardInfoEvent -> {
                _boardType.value = event.boardInfo.first
                _selectedIndex.value = event.boardInfo.second
                setLevel(event.boardInfo.second)
            }
            is SelectBoardUseCase.OnGameModeEvent -> {
                _gameMode.value = event.gameMode
            }
            is SelectBoardUseCase.OnBoardTypeChange -> {
                _boardType.value = when(event.index){
                    0 -> BoardType.THREEX3
                    1 -> BoardType.FOURX4
                    2 -> BoardType.FIVEX5
                    else -> BoardType.THREEX3
                }
            }
            is SelectBoardUseCase.OnPlayerCountEvent -> {
                _playerCount.value = event.playerCount
            }
        }
    }

    private fun setLevel(levelIndex: Int) {
        _level.value = when (levelIndex) {
            0 -> GameLevel.EASY
            1 -> GameLevel.MEDIUM
            2 -> GameLevel.HARD
            else -> GameLevel.NONE
        }
    }

}