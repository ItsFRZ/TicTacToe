package com.itsfrz.tictactoe.game.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameLevel
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.GameResult
import com.itsfrz.tictactoe.common.functionality.GameWinner
import com.itsfrz.tictactoe.common.state.EssentialInfo
import com.itsfrz.tictactoe.game.domain.usecase.GameUsecase
import com.itsfrz.tictactoe.goonline.data.models.BoardState
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import com.itsfrz.tictactoe.minimax.GameBrain
import com.itsfrz.tictactoe.minimax.IGameBrain
import com.itsfrz.tictactoe.minimax.Move
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull

class GameViewModel(
    private val cloudRepository: CloudRepository,
    private val gameStoreRepository: GameStoreRepository,
    private val essentialInfo: EssentialInfo
) : ViewModel() {

    private val TAG = "GVM"

    private var job : Job? = null

    private var gameMode : GameMode
    private var gameLevel: GameLevel
    private var boardType: BoardType

    private val _userTimer : MutableState<Float> = mutableStateOf(1F)
    val userTimer : State<Float> = _userTimer

    private val _offlineUserTurn : MutableState<Boolean> = mutableStateOf(true)

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


    // Online Config
    private val _friendUserId: MutableState<String> = mutableStateOf("")
    val friendUserId = _friendUserId

    private val _userId: MutableState<String> = mutableStateOf("")
    val userId = _userId

    private val _currentUserId: MutableState<String> = mutableStateOf("")
    val currentUserId = _currentUserId

    private val _gameSessionId : MutableState<String> = mutableStateOf("")
    val gameSessionId = _gameSessionId

    private val _gameBoardState : MutableState<BoardState> = mutableStateOf(BoardState())
    val gameBoardState = _gameBoardState

    private val _playGround : MutableState<Playground> = mutableStateOf(Playground())

    private val _onlineGameWinner : MutableState<String> = mutableStateOf("")
    val onlineGameWinner = _onlineGameWinner

    private val _isGameDraw: MutableState<Boolean> = mutableStateOf(false)
    val isGameDraw = _isGameDraw

    private val _inGame: MutableState<Boolean> = mutableStateOf(true)
    val inGame = _inGame

    private val _requestDialogState : MutableState<Boolean> = mutableStateOf(false)
    val requestDialogState = _requestDialogState

    private val _acceptDialogState : MutableState<Boolean> = mutableStateOf(false)
    val acceptDialogState = _acceptDialogState

    init {
        essentialInfo.getEssentialInfo().let {
            gameMode = it.gameMode
            gameLevel = it.gameLevel
            boardType = it.boardType
        }
        setUpDynamicBoard()
        timeLimitStart()
    }

    private fun setUpDynamicBoard() {
        when(boardType){
            BoardType.THREEX3 -> {
                gameMap =arrayListOf(
                    arrayListOf(0,0,0),
                    arrayListOf(0,0,0),
                    arrayListOf(0,0,0),
                )
            }
            BoardType.FOURX4 -> {
                gameMap = arrayListOf(
                    arrayListOf(0,0,0,0,0),
                    arrayListOf(0,0,0,0,0),
                    arrayListOf(0,0,0,0,0),
                    arrayListOf(0,0,0,0,0),
                    arrayListOf(0,0,0,0,0),
                )
            }
            BoardType.FIVEX5 -> {
                gameMap = arrayListOf(
                    arrayListOf(0,0,0,0,0,0),
                    arrayListOf(0,0,0,0,0,0),
                    arrayListOf(0,0,0,0,0,0),
                    arrayListOf(0,0,0,0,0,0),
                    arrayListOf(0,0,0,0,0,0),
                    arrayListOf(0,0,0,0,0,0),
                )
            }
        }
    }

    private fun getRandomTurnGenerator() {
        _isUserTurnsComplete.value = _offlineUserTurn.value == true
        _offlineUserTurn.value = !_offlineUserTurn.value
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

                if (gameMode == GameMode.FRIEND){
                    updatePlayerData(event.index)
                }
            }
            is GameUsecase.OnAIMove -> {
                if (gameMode == GameMode.AI && gameResult.value == GameResult.NONE){
                    viewModelScope.launch {
                        val aiValue = viewModelScope.async(Dispatchers.Default) {
                            delay(if (boardType == BoardType.THREEX3) 50 else 0)
                            aiMove()
                        }.await()
                        _playerTwoIndex.value.add(aiValue)
                        setGameMap(aiValue)
                        playGame()
                    }
                }
            }
            is GameUsecase.OnGameRetry -> {
                if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM){
                    _requestDialogState.value = true
                    playAgainRequest()
                }
            }
            is GameUsecase.OnBackPress -> {
                _onBackPress.value = event.backPressState
            }
            is GameUsecase.OnDelayLaunch -> {
                _isDelayed.value = event.delayValue
            }
            is GameUsecase.UpdateUserId -> {
                _userId.value = event.userId
            }
            is GameUsecase.UpdateFriendUserId -> {
                _friendUserId.value = event.userId
            }
            is GameUsecase.GameExitEvent -> {
                if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM) {
                    updateInGameInStore(false)
                    updatePlayGround()
                    removeGameBoard()
                }
                resetGameBoard()
            }
            is GameUsecase.OnUpdateGameSessionId -> {
                _gameSessionId.value = event.sessionId
            }
            is GameUsecase.OnClearGameBoard -> {
                if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM) {
                    removeGameBoard()
                    updatePlayGround()
                }
                resetGameBoard()
            }
            is GameUsecase.OnUpdateCurrentUserId -> {
                _currentUserId.value = event.currentUserId
            }
            is GameUsecase.OnCancelPlayRequest -> {
                _requestDialogState.value = false
                _acceptDialogState.value = false
            }
            is GameUsecase.OnAcceptPlayAgainRequest -> {
                _requestDialogState.value = false
                _acceptDialogState.value = false
                acceptPlayAgainRequest()
            }
            else -> {}
        }
    }

    private fun acceptPlayAgainRequest(){
        viewModelScope.launch(Dispatchers.IO) {
            val playAgainRequest =
                BoardState.PlayRequest(requesterId = _userId.value, retryRequest = false,acceptRequest = true)
            val resetBoardState = _gameBoardState.value.copy(
                playerOneState = BoardState.Player(_gameBoardState.value.playerOneState?.userId ?: "",indexes = emptyList()),
                playerTwoState = BoardState.Player(_gameBoardState.value.playerTwoState?.userId ?: "", indexes = emptyList()),
                currentUserTurnId = if (_onlineGameWinner.value == _userId.value) _friendUserId.value else _userId.value,
                gameWinnerId = "",
                resetTimer = true,
                gameDraw = false,
                playAgain = playAgainRequest
            )
            cloudRepository.acceptPlayAgainRequest(_gameSessionId.value, resetBoardState)
        }
        resetGameBoard()
    }

    private fun playAgainRequest() {
        viewModelScope.launch(Dispatchers.IO) {
            val playAgainRequest = BoardState.PlayRequest(requesterId = _userId.value,retryRequest = true,acceptRequest = false)
            val resetBoardState = _gameBoardState.value.copy(
                playerOneState = BoardState.Player(_gameBoardState.value.playerOneState?.userId ?: "",indexes = emptyList()),
                playerTwoState = BoardState.Player(_gameBoardState.value.playerTwoState?.userId ?: "",indexes = emptyList()),
                currentUserTurnId = if (_currentUserId.value == _friendUserId.value) _userId.value else _friendUserId.value,
                gameWinnerId = "",
                resetTimer = true,
                gameDraw = false,
                playAgain = playAgainRequest
            )
            cloudRepository.playAgainRequest(_gameSessionId.value,resetBoardState)
        }
        resetGameBoard()
    }

    private fun updateInGameInStore(inGame: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = gameStoreRepository.fetchPreference().firstOrNull()
            data?.let {
                it.playGround?.let {
                    gameStoreRepository.updatePlayground(it.copy(inGame = inGame))
                }
            }
        }
    }

    private fun aiMove() : Int {
        val minimax : GameBrain = IGameBrain
        minimax.setAITurn(true)
        Log.i("AI_MOVE", "aiMove: Before Optimal Move")
        val difficultyLevel = getDiificultyLevel(gameLevel)
        val bestOptimalMove : Move = when(boardType){
            BoardType.THREEX3 -> minimax.getOptimalMove(gameMap,3,difficultyLevel)
            BoardType.FOURX4 -> minimax.getOptimalMove(gameMap,4,difficultyLevel)
            BoardType.FIVEX5 -> minimax.getOptimalMove(gameMap,5,difficultyLevel)
        }
        Log.i("AI_MOVE", "aiMove: After Optimal Move ${getIndexFromMove(bestOptimalMove)}")
        return getIndexFromMove(bestOptimalMove)
    }

    private fun getDiificultyLevel(gameLevel : GameLevel) = when (gameLevel) {
        GameLevel.EASY -> 0
        GameLevel.MEDIUM -> 1
        GameLevel.HARD -> 2
        GameLevel.NONE -> 2
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
            GameMode.FOUR_PLAYER -> {
//                setUserTurn()
//                resetTimeLimit()
//                checkGameWinner()
//                checkDraw()
            }
            GameMode.AI -> {
                resetTimeLimit()
                checkGameWinner()
                checkDraw()
                setUserTurn()
            }
            GameMode.FRIEND -> {
                resetTimeLimit()
                checkGameWinner()
                checkDraw()
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
            GameMode.FRIEND -> {
                if (isDraw)
                    _gameResult.value = GameResult.DRAW
            }
            else -> {}
        }
    }

    private fun checkGameWinner() {
        var validateWinner = false
        val TAG = "GAME_WIN"
        Log.i(TAG, "checkGameWinner: Inside Game Condition ${boardType}")
        when(boardType) {
            BoardType.THREEX3 -> {
                validateWinner = checkDiagonalPossibility() || checkVerticalPossibility() || checkHorizontalPossibility()
            }
            BoardType.FOURX4 -> {
                validateWinner = GameWinner.checkAllPositionsByBoard(gameMap,4)
                Log.i(TAG, "checkGameWinner: 4x4 Board : ${gameMap}")
            }
            BoardType.FIVEX5 -> {
                validateWinner = GameWinner.checkAllPositionsByBoard(gameMap,5)
                Log.i(TAG, "checkGameWinner: 5x5 Board : ${gameMap}")
            }

        }
        if (validateWinner){
            when(gameMode){
                GameMode.TWO_PLAYER -> {
                    if (_isUserTurnsComplete.value){
                        Log.i("WINNER_NAME", "checkWinner: Player 1 is WIN")
                    }else{
                        Log.i("WINNER_NAME", "checkWinner: Player 2 is WIN")
                    }
                    _gameResult.value = GameResult.WIN
                }
                GameMode.AI -> {
                    if (_isUserTurnsComplete.value){
                        _gameResult.value = GameResult.WIN
                    }else{
                        _gameResult.value = GameResult.LOSE
                    }
                }
                GameMode.FRIEND -> {
                    if (_currentUserId.value == _userId.value){
                        _onlineGameWinner.value = _friendUserId.value
                        _gameResult.value = GameResult.LOSE
                    }else {
                        _onlineGameWinner.value = _userId.value
                        _gameResult.value = GameResult.WIN
                    }
                    initiateExitGameRequest()
                }
                else -> {
                }
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


    private fun setGameMap(index: Int,inputData : Int) {
        if (index in 0..2){
            gameMap.get(0).set(index,inputData)
        }else if (index in 3..5){
            gameMap.get(1).set(index-3,inputData)
        }else if(index in 6..8){
            gameMap.get(2).set(index-6,inputData)
        }
        Log.i("GameMap", "setGameMap: ${gameMap.toString()}")
    }

    private fun setGameMap(index: Int) {
        val inputData = if (_isUserTurnsComplete.value) 1 else 2
        when(boardType){
            BoardType.THREEX3 -> {
                if (index in 0..2){
                    gameMap.get(0).set(index,inputData)
                }else if (index in 3..5){
                    gameMap.get(1).set(index-3,inputData)
                }else if(index in 6..8){
                    gameMap.get(2).set(index-6,inputData)
                }
            }
            BoardType.FOURX4 -> {
                if (index in 0..4){
                    gameMap.get(0).set(index,inputData)
                }else if (index in 5..9){
                    gameMap.get(1).set(index-5,inputData)
                }else if(index in 10..14){
                    gameMap.get(2).set(index-10,inputData)
                }else if(index in 15..19){
                    gameMap.get(3).set(index-15,inputData)
                }else if(index in 20..24){
                    gameMap.get(4).set(index-20,inputData)
                }
            }
            BoardType.FIVEX5 -> {
                if (index in 0..5){
                    gameMap.get(0).set(index,inputData)
                }else if (index in 6..11){
                    gameMap.get(1).set(index-6,inputData)
                }else if(index in 12..17){
                    gameMap.get(2).set(index-12,inputData)
                }else if(index in 18..23){
                    gameMap.get(3).set(index-18,inputData)
                }else if(index in 24..29){
                    gameMap.get(4).set(index-24,inputData)
                }else if(index in 30..35){
                    gameMap.get(5).set(index-30,inputData)
                }
            }
        }

        Log.i("GameMap", "setGameMap: ${gameMap.toString()}")
    }

    private fun setUserTurn() {
        if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM){
            viewModelScope.launch(Dispatchers.IO) {
                viewModelScope.launch(Dispatchers.IO) {
                    val nextUserTurn = if (_gameBoardState.value.currentUserTurnId == _userId.value) _friendUserId.value else _userId.value
                    cloudRepository.updateGameBoard(_gameSessionId.value,_gameBoardState.value.copy(currentUserTurnId = nextUserTurn, resetTimer = true))
                }
            }
        }else
            _isUserTurnsComplete.value = !_isUserTurnsComplete.value
    }

    private fun timeLimitStart(){
        job = viewModelScope.launch(Dispatchers.Default) {
            var timeBound = if (boardType == BoardType.THREEX3) 10 else 30
            while (timeBound != 0) {
                delay(1000)
                _userTimer.value = if (boardType == BoardType.THREEX3) (timeBound / 10F) else (timeBound / 10F)/3F
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
        setUpDynamicBoard()
        _gameResult.value = GameResult.NONE
        _playerOneIndex.value = arrayListOf()
        _playerTwoIndex.value = arrayListOf()
        _userTimer.value = 0F
        job?.cancel()
        timeLimitStart()
        if (gameMode == GameMode.AI)
            setAIRetryTurn()
        else getRandomTurnGenerator()
//        setAITurn()
        _winnerIndexList.value = ArrayList(listOf(-1,-1,-1))
        onEvent(GameUsecase.OnDelayLaunch(false))
        _userWarning.value = false
        _onlineGameWinner.value = ""
        _gameBoardState.value = BoardState()
    }

    private fun setAIRetryTurn() {
        if (gameMode == GameMode.AI){
            if (_offlineUserTurn.value){
                _isUserTurnsComplete.value = true
                _offlineUserTurn.value = false
            }else{
                _isUserTurnsComplete.value = false
                _offlineUserTurn.value = true
            }
            if (!_isUserTurnsComplete.value)
                onEvent(GameUsecase.OnAIMove)
        }
    }

    fun setAITurn() {
        if (gameMode == GameMode.AI){
            _offlineUserTurn.value = false
            _isUserTurnsComplete.value = true
        }
    }

    // Set user to play its move
    fun gameBoardUpdate(gameBoard: BoardState) {
        Log.i(TAG, "gameBoardUpdate: gameBoardState ${gameBoard}")
        gameBoard.playAgain?.let {
            if (!it.acceptRequest){
                if (it.requesterId == _userId.value){
                    _requestDialogState.value = true
                    _acceptDialogState.value = false
                }else{
                    _requestDialogState.value = false
                    _acceptDialogState.value = true
                }
            }else{
                _requestDialogState.value = false
                _acceptDialogState.value = false
            }
        }
        if (gameBoard.resetTimer)
            resetTimeLimit()
        if (_onlineGameWinner.value.isNotEmpty() || isGameDraw.value){
            return
        }
        _currentUserId.value = gameBoard.currentUserTurnId
        _friendUserId.value = if (_userId.value == _gameSessionId.value) gameBoard.playerTwoState?.userId ?: ""
        else gameBoard.playerOneState?.userId ?: ""
        if (_gameSessionId.value == _userId.value){ // Player 1
            _playerOneIndex.value = ArrayList(gameBoard.playerOneState?.indexes ?: emptyList())
            _playerTwoIndex.value = ArrayList(gameBoard.playerTwoState?.indexes ?: emptyList())
        }else{
            _playerOneIndex.value = ArrayList(gameBoard.playerTwoState?.indexes ?: emptyList())
            _playerTwoIndex.value = ArrayList(gameBoard.playerOneState?.indexes ?: emptyList())
        }
        Log.i(TAG, "gameBoardUpdate: Player One Index ${playerOneIndex.value}")
        Log.i(TAG, "gameBoardUpdate: Player Two Index ${playerTwoIndex.value}")
        _gameBoardState.value = gameBoard
        updateGameMapIndexes(_playerOneIndex.value,1)
        updateGameMapIndexes(_playerTwoIndex.value,2)
        checkDraw()
        checkGameWinner()
    }

    private fun initiateExitGameRequest() {
        viewModelScope.launch(Dispatchers.IO) {
            _gameBoardState.value = _gameBoardState.value.copy(gameWinnerId = _onlineGameWinner.value, resetTimer = false, currentUserTurnId = "")
            cloudRepository.updateGameBoard(_gameSessionId.value, gameBoardState = _gameBoardState.value)
        }
    }

    private fun updateGameMapIndexes(indexes: ArrayList<Int>,playerMoveValue : Int) {
        indexes.forEach {
            setGameMap(it,playerMoveValue)
        }
    }

    private fun updatePlayerData(index : Int) {
        var boardState = _gameBoardState.value
        if (_gameSessionId.value == _userId.value){ // Player 1
            var playerOneState : BoardState.Player = boardState?.playerOneState ?: BoardState.Player(userId = _gameSessionId.value, indexes = emptyList())

            var indexList = ArrayList(boardState.playerOneState?.indexes ?: emptyList())
            indexList.add(index)
            playerOneState = playerOneState.copy(
                indexes = indexList
            )

            boardState = boardState.copy(
                currentUserTurnId = _friendUserId.value,
                playerOneState = playerOneState,
                playerTwoState = _gameBoardState.value.playerTwoState
            )
        }else{
            var playerTwoState : BoardState.Player = boardState?.playerTwoState ?: BoardState.Player(userId = _userId.value, indexes = emptyList())

            var indexList = ArrayList(boardState.playerTwoState?.indexes ?: emptyList())
            indexList.add(index)
            playerTwoState = playerTwoState.copy(
                indexes = indexList
            )

            boardState = boardState.copy(
                currentUserTurnId = _friendUserId.value,
                playerOneState = _gameBoardState.value.playerOneState,
                playerTwoState = playerTwoState
            )
        }
        Log.i("CURRENT_USER_ID", "updatePlayerData: Current UserId ${_currentUserId.value}")
        Log.i("CURRENT_USER_ID", "updatePlayerData: Updated Friend UserId ${_friendUserId.value}")
        boardState = boardState.copy(resetTimer = true, gameWinnerId = _onlineGameWinner.value, gameDraw = _isGameDraw.value)
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.updateGameBoard(_gameSessionId.value,boardState)
        }
    }

    private fun removeGameBoard(){
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.removeGameBoard(_gameSessionId.value)
        }
        viewModelScope.launch {
            gameStoreRepository.clearGameBoard()
        }
    }

    fun playGroundUpdate(playground: Playground) {
        _playGround.value = playground
    }

    private fun updatePlayGround() {
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.updatePlayground(_playGround.value.copy(inGame = false))
        }
    }

}