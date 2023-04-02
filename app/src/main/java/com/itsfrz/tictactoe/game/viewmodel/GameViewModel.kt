package com.itsfrz.tictactoe.game.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.GameResult
import com.itsfrz.tictactoe.game.usecase.GameUsecase
import com.itsfrz.tictactoe.minimax.GameBrain
import com.itsfrz.tictactoe.minimax.IGameBrain
import com.itsfrz.tictactoe.minimax.Move
import kotlinx.coroutines.*
import kotlin.random.Random

class GameViewModel : ViewModel() {

    private val TAG = "GVM"

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

    // Player two is also robot
    private val _playerTwoIndex : MutableState<ArrayList<Int>> = mutableStateOf(arrayListOf())
    val playerTwoIndex = _playerTwoIndex

    private val _winnerIndexList : MutableState<ArrayList<Int>> = mutableStateOf(arrayListOf())
    val winnerIndexList = _winnerIndexList

    private val _gameResult : MutableState<GameResult> = mutableStateOf(GameResult.NONE)
    val gameResult : State<GameResult> = _gameResult

    private val _onBackPress : MutableState<Boolean> = mutableStateOf(false)
    val onBackPress = _onBackPress

    private val _userWarning : MutableState<Boolean> = mutableStateOf(false)
    val userWarning = _userWarning

    private val _isDelayed: MutableState<Boolean> = mutableStateOf(false)
    val isDelayed = _isDelayed

    init {
        getRandomTurnGenerator()
        timeLimitStart()
    }

    private fun getRandomTurnGenerator() {
        _isUserTurnsComplete.value = Random.nextBoolean()
    }

    private fun switchUserOnTimeOut() {
        if (_userTimer.value == 0F){
            _userWarning.value = false
            setUserTurn()
            resetTimeLimit()
        }
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
                    setGameMap(event.index)
                    playGame()
                }

                if (gameMode == GameMode.AI){
                    if (_isUserTurnsComplete.value){
                        _playerOneIndex.value.add(event.index)
                    }
                    setGameMap(event.index)
                    playGame()
                }
            }
            is GameUsecase.OnAIMove -> {
                viewModelScope.launch {
                    val aiValue = viewModelScope.async(Dispatchers.Default) { aiMove() }.await()
                    _playerTwoIndex.value.add(aiValue)
                    setGameMap(aiValue)
                    playGame()
                }
            }
            is GameUsecase.OnGameRetry -> {
                resetGameBoard()
            }
            is GameUsecase.OnBackPress -> {
                _onBackPress.value = event.backPressState
            }
            is GameUsecase.OnDelayLaunch -> {
                _isDelayed.value = event.delayValue
            }
        }
    }

    private fun aiMove() : Int {
        val minimax : GameBrain = IGameBrain
        minimax.setAITurn(true)
        val bestOptimalMove : Move = minimax.getOptimalMove(gameMap)
        Log.i("AI_MOVE", "aiMove: AI Move ${bestOptimalMove}")
        return getIndexFromMove(bestOptimalMove)
    }

    private fun getIndexFromMove(move: Move): Int {
        return (gameMap[0].size * move.row) + move.col
    }

    private fun playGame(){
        when(gameMode){
            GameMode.TWO_PLAYER -> {
                setUserTurn()
                resetTimeLimit()
                checkGameWinner()
                checkDraw()
            }
            GameMode.AI -> {
                setUserTurn()
                resetTimeLimit()
                checkGameWinner()
                checkDraw()
            }
            GameMode.FRIEND -> {

            }
            GameMode.RANDOM -> {

            }
        }
    }

    private fun checkDraw() {
        var isDraw : Boolean = true
        gameMap.forEach { row ->
            row.forEach { col ->
                if (col == 0) {
                    isDraw = false
                }
            }
        }
        when(gameMode){
            GameMode.TWO_PLAYER -> {
                if (isDraw && _gameResult.value == GameResult.NONE){
                    _gameResult.value = GameResult.DRAW
                }
            }
            GameMode.AI -> {
                if (isDraw){
                    _gameResult.value = GameResult.DRAW
                }
            }
            else -> {}
        }
    }

    private fun checkGameWinner() {
        if (checkDiagonalPossibility() || checkVerticalPossibility() || checkHorizontalPossibility()){
            when(gameMode){
                GameMode.TWO_PLAYER -> {
                    if (_isUserTurnsComplete.value){
                        Log.i("WINNER", "checkWinner: Player 1 is WIN")
                    }else{
                        Log.i("WINNER", "checkWinner: Player 2 is WIN")
                    }
                    _gameResult.value = GameResult.WIN
                }
                GameMode.AI -> {
                    if (_isUserTurnsComplete.value){
                        _gameResult.value = GameResult.LOSE
                    }else{
                        _gameResult.value = GameResult.WIN
                    }
                }
                else -> {}
            }
        }
    }

    private fun checkHorizontalPossibility() : Boolean{
        gameMap.forEachIndexed { index, list ->
            if (checkListWithElement(list,1)) {
                when (index) {
                    0 -> setWinnerList(listOf(0,1,2))
                    1 -> setWinnerList(listOf(3,4,5))
                    else ->setWinnerList(listOf(6,7,8))
                }
                return true
            }
        }
        gameMap.forEachIndexed { index,list->
            if (checkListWithElement(list,2)) {
                when (index) {
                    0 -> setWinnerList(listOf(0,1,2))
                    1 -> setWinnerList(listOf(3,4,5))
                    else -> setWinnerList(listOf(6,7,8))
                }
                return true
            }
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


        if (checkListWithElement(column1,1)) {
            setWinnerList(listOf<Int>(0,3,6))
            return true
        }
        if (checkListWithElement(column2,1)) {
            setWinnerList(listOf<Int>(1,4,7))
            return true
        }
        if (checkListWithElement(column3,1)) {
            setWinnerList(listOf<Int>(2,5,8))
            return true
        }

        if (checkListWithElement(column1,2)) {
            setWinnerList(listOf<Int>(0,3,6))
            return true
        }
        if (checkListWithElement(column2,2)) {
            setWinnerList(listOf<Int>(1,4,7))
            return true
        }
        if (checkListWithElement(column3,2)) {
            setWinnerList(listOf<Int>(2,5,8))
            return true
        }

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

        if (checkListWithElement(diagonal1,1)) {
            setWinnerList(listOf<Int>(0,4,8))
            return true
        }
        if (checkListWithElement(diagonal2,1)) {
            setWinnerList(listOf<Int>(2,4,6))
            return true
        }
        if (checkListWithElement(diagonal1,2)) {
            setWinnerList(listOf<Int>(0,4,8))
            return true
        }
        if (checkListWithElement(diagonal2,2)) {
            setWinnerList(listOf<Int>(2,4,6))
            return true
        }

        return false
    }

    private fun checkListWithElement(row : ArrayList<Int>,element : Int) : Boolean{
        row.forEach {
            if (it != element)
                return false
        }
        return true
    }

    private fun setWinnerList(winIndexes: List<Int>) {
        _winnerIndexList.value = ArrayList(winIndexes)
        Log.i(TAG, "setWinnerList: ${winIndexes}")
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
            var timeBound = 10
            while (timeBound != 0) {
                delay(1000)
                _userTimer.value = (timeBound / 10F)
                timeBound--
                if (timeBound == 3)
                    _userWarning.value = true

            }
            _userTimer.value = 0F
            switchUserOnTimeOut()
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
        getRandomTurnGenerator()
        _gameResult.value = GameResult.NONE
        _playerOneIndex.value = arrayListOf()
        _playerTwoIndex.value = arrayListOf()
        _userTimer.value = 0F
        job?.cancel()
        timeLimitStart()
        setAITurn()
        _winnerIndexList.value = ArrayList(listOf(-1,-1,-1))
        onEvent(GameUsecase.OnDelayLaunch(false))
        _userWarning.value = false
    }

    fun setGameMode(gameMode: GameMode) {
        this.gameMode = gameMode
    }

    fun setAITurn() {
        if (gameMode == GameMode.AI){
            if (!_isUserTurnsComplete.value)
                onEvent(GameUsecase.OnAIMove)
        }
    }
}