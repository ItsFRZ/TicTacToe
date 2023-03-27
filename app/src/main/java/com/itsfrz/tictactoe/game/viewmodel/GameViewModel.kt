package com.itsfrz.tictactoe.game.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.game.usecase.GameUsecase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private var job : Job? = null

    private lateinit var gameMode : GameMode

    private val _userTimer : MutableState<Float> = mutableStateOf(1F)
    val userTimer : State<Float> = _userTimer

    private val _isUserTurnsComplete : MutableState<Boolean> = mutableStateOf(false)
    val isUserTurnsComplete : State<Boolean> = _isUserTurnsComplete

    private var gameMap : ArrayList<ArrayList<Int>> = arrayListOf(
        arrayListOf(0,0,0),
        arrayListOf(0,0,0),
        arrayListOf(0,0,0),
    )

    private val _playerOneIndex : MutableState<ArrayList<Int>> = mutableStateOf(arrayListOf())
    val playerOneIndex = _playerOneIndex

    private val _playerTwoIndex : MutableState<ArrayList<Int>> = mutableStateOf(arrayListOf())
    val playerTwoIndex = _playerTwoIndex

    private val _isWinner : MutableState<Boolean> = mutableStateOf(false)
    val isWinner : State<Boolean> = _isWinner

    init {
        timeLimitStart()
    }


    fun onEvent(event : GameUsecase){
        when(event){
            is GameUsecase.OnUserTick -> {
                if (gameMode == GameMode.TWO_PLAYER){
                    if (_isUserTurnsComplete.value){
                        _playerOneIndex.value.add(event.index)
                    }else{
                        _playerTwoIndex.value.add(event.index)
                    }
                }
                setGameMap(event.index)
                playGame()
            }
            is GameUsecase.OnGameRetry -> {
                resetGameBoard()
            }
        }
    }


    private fun playGame(){
        when(gameMode){
            GameMode.TWO_PLAYER -> {
                setUserTurn()
                resetTimeLimit()
                checkWinner()
            }
            GameMode.AI -> {

            }
            GameMode.FRIEND -> {

            }
            GameMode.RANDOM -> {

            }
        }
    }

    private fun checkWinner() {
        if (checkDiagonalPossibility() || checkVerticalPossibility() || checkHorizontalPossibility()){
            if (_isUserTurnsComplete.value){
                Log.i("WINNER", "checkWinner: Player 1 is WIN")
            }else{
                Log.i("WINNER", "checkWinner: Player 2 is WIN")
            }
            _isWinner.value = true
        }
    }

    private fun checkHorizontalPossibility() : Boolean{
        gameMap.forEach {
            if (checkListWithElement(it,1))
                return true
        }
        gameMap.forEach {
            if (checkListWithElement(it,2))
                return true
        }
        return false
    }

    private fun checkVerticalPossibility() : Boolean{
        val column1 = arrayListOf<Int>()
        column1.add(gameMap.get(0).get(0))
        column1.add(gameMap.get(1).get(0))
        column1.add(gameMap.get(2).get(0))

        val column2 = arrayListOf<Int>()
        column2.add(gameMap.get(0).get(1))
        column2.add(gameMap.get(1).get(1))
        column2.add(gameMap.get(2).get(1))

        val column3 = arrayListOf<Int>()
        column3.add(gameMap.get(0).get(2))
        column3.add(gameMap.get(1).get(2))
        column3.add(gameMap.get(2).get(2))


        if (checkListWithElement(column1,1))
            return true
        if (checkListWithElement(column2,1))
            return true
        if (checkListWithElement(column3,1))
            return true

        if (checkListWithElement(column1,2))
            return true
        if (checkListWithElement(column2,2))
            return true
        if (checkListWithElement(column3,2))
            return true

        return false
    }

    private fun checkDiagonalPossibility() : Boolean{
        val diagonal1 = arrayListOf<Int>()
        diagonal1.add(gameMap.get(0).get(0))
        diagonal1.add(gameMap.get(1).get(1))
        diagonal1.add(gameMap.get(2).get(2))

        val diagonal2 = arrayListOf<Int>()
        diagonal2.add(gameMap.get(0).get(2))
        diagonal2.add(gameMap.get(1).get(1))
        diagonal2.add(gameMap.get(2).get(0))

        if (checkListWithElement(diagonal1,1))
            return true
        if (checkListWithElement(diagonal2,1))
            return true
        if (checkListWithElement(diagonal1,2))
            return true
        if (checkListWithElement(diagonal2,2))
            return true

        return false
    }

    private fun checkListWithElement(row : ArrayList<Int>,element : Int) : Boolean{
        row.forEach {
            if (it != element)
                return false
        }
        return true
    }

    private fun setGameMap(index: Int) {
        val inputData = if (_isUserTurnsComplete.value) 1 else 2
        if (index in 0..2){
            gameMap.get(0).set(index,inputData)
        }else if (index in 3..5){
            gameMap.get(1).set(index-3,inputData)
        }else if(index in 6..8){
            gameMap.get(2).set(index-6,inputData)
        }
        Log.i("GameMap", "setGameMap: ${gameMap.toString()}")
    }

    private fun setUserTurn() {
        _isUserTurnsComplete.value = !_isUserTurnsComplete.value
    }

    private fun timeLimitStart(){
        job = viewModelScope.launch(Dispatchers.Default) {
            var timeBound = 15
            while (timeBound != 0) {
                delay(1000)
                _userTimer.value = (timeBound / 15F)
                timeBound--

            }
            _userTimer.value = 0F
        }
    }

    private fun resetTimeLimit(){
        job?.cancel()
        if (isZeroInGameMap())
            timeLimitStart()
        else _userTimer.value = 0F
    }

    private fun isZeroInGameMap() : Boolean{
        gameMap.forEach { row ->
            row.forEach {
                if (it == 0)
                    return true
            }
        }
        return false
    }

    private fun resetGameBoard(){
        gameMap = arrayListOf(
            arrayListOf(0,0,0),
            arrayListOf(0,0,0),
            arrayListOf(0,0,0),
        )
        _isUserTurnsComplete.value = false
        _playerOneIndex.value = arrayListOf()
        _playerTwoIndex.value = arrayListOf()
        _userTimer.value = 0F
        _isWinner.value = false
        job?.cancel()
        timeLimitStart()

    }

    fun setGameMode(gameMode: GameMode) {
        this.gameMode = gameMode
    }
}