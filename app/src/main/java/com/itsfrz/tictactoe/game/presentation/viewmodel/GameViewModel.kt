package com.itsfrz.tictactoe.game.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.common.enums.*
import com.itsfrz.tictactoe.common.functionality.GameWinner
import com.itsfrz.tictactoe.common.state.EssentialInfo
import com.itsfrz.tictactoe.game.brain.IGameBrain
import com.itsfrz.tictactoe.game.domain.usecase.GameUsecase
import com.itsfrz.tictactoe.goonline.data.models.BoardState
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.minimax.GameBrain
import com.itsfrz.tictactoe.minimax.Move
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
// BUG FIX: Removed `import kotlinx.coroutines.time.delay` — that overload takes
// java.time.Duration, not Long.  It shadowed the standard delay(Long) in
// timeLimitStart() and caused an ambiguity / wrong-overload resolution at runtime.
// The wildcard `import kotlinx.coroutines.*` already brings the correct delay(Long).

class GameViewModel(
    private val cloudRepository: CloudRepository,
    private val gameStoreRepository: GameStoreRepository,
    private val essentialInfo: EssentialInfo
) : ViewModel() {

    private val TAG = "GVM"

    private var job: Job? = null

    private var gameMode: GameMode
    private var gameLevel: GameLevel
    private var boardType: BoardType

    private val _userTimer: MutableState<Float> = mutableStateOf(1F)
    val userTimer: State<Float> = _userTimer

    private val _offlineUserTurn: MutableState<Boolean> = mutableStateOf(true)

    private val _isUserTurnsComplete: MutableState<Boolean> = mutableStateOf(false)
    val isUserTurnsComplete: State<Boolean> = _isUserTurnsComplete

    // BUG FIX: gameMap kept as ArrayList<ArrayList<Int>> for minimax compatibility,
    // but setGameMap now uses an O(1) formula (index / cols, index % cols) instead of
    // cascading if-else chains that were both verbose and fragile.
    private var gameMap: ArrayList<ArrayList<Int>> = arrayListOf(
        arrayListOf(0, 0, 0),
        arrayListOf(0, 0, 0),
        arrayListOf(0, 0, 0),
    )

    // BUG FIX: All player-index states are now typed as List<Int> (immutable snapshots).
    // The original code called .add() on the ArrayList directly, mutating it in place —
    // Compose sees the same reference and skips recomposition entirely (silent UI freeze).
    // Fix: every update creates a new list, guaranteeing Compose detects the change.
    private val _playerOneIndex: MutableState<List<Int>> = mutableStateOf(emptyList())
    val playerOneIndex: State<List<Int>> = _playerOneIndex

    private val _playerTwoIndex: MutableState<List<Int>> = mutableStateOf(emptyList())
    val playerTwoIndex: State<List<Int>> = _playerTwoIndex

    private val _playerThreeIndex: MutableState<List<Int>> = mutableStateOf(emptyList())
    val playerThreeIndex: State<List<Int>> = _playerThreeIndex

    private val _playerFourIndex: MutableState<List<Int>> = mutableStateOf(emptyList())
    val playerFourIndex: State<List<Int>> = _playerFourIndex

    private val _winnerIndexList: MutableState<List<Int>> = mutableStateOf(emptyList())
    val winnerIndexList: State<List<Int>> = _winnerIndexList

    private val _gameResult: MutableState<GameResult> = mutableStateOf(GameResult.NONE)
    val gameResult: State<GameResult> = _gameResult

    private val _onBackPress: MutableState<Boolean> = mutableStateOf(false)
    val onBackPress: State<Boolean> = _onBackPress

    private val _userWarning: MutableState<Boolean> = mutableStateOf(false)
    val userWarning: State<Boolean> = _userWarning

    private val _isDelayed: MutableState<Boolean> = mutableStateOf(false)
    val isDelayed: State<Boolean> = _isDelayed

    private val _playerCount: MutableState<PlayerCount> = mutableStateOf(PlayerCount.ONE)
    val playerCount: State<PlayerCount> = _playerCount

    // Online Config
    private val _friendUserId: MutableState<String> = mutableStateOf("")
    val friendUserId: State<String> = _friendUserId

    private val _userId: MutableState<String> = mutableStateOf("")
    val userId: State<String> = _userId

    private val _currentUserId: MutableState<String> = mutableStateOf("")
    val currentUserId: State<String> = _currentUserId

    private val _gameSessionId: MutableState<String> = mutableStateOf("")
    val gameSessionId: State<String> = _gameSessionId

    private val _gameBoardState: MutableState<BoardState> = mutableStateOf(BoardState())
    val gameBoardState: State<BoardState> = _gameBoardState

    private val _playGround: MutableState<Playground> = mutableStateOf(Playground())

    private val _onlineGameWinner: MutableState<String> = mutableStateOf("")
    val onlineGameWinner: State<String> = _onlineGameWinner

    private val _isGameDraw: MutableState<Boolean> = mutableStateOf(false)
    val isGameDraw: State<Boolean> = _isGameDraw

    private val _inGame: MutableState<Boolean> = mutableStateOf(true)
    val inGame: State<Boolean> = _inGame

    private val _requestDialogState: MutableState<Boolean> = mutableStateOf(false)
    val requestDialogState: State<Boolean> = _requestDialogState

    private val _acceptDialogState: MutableState<Boolean> = mutableStateOf(false)
    val acceptDialogState: State<Boolean> = _acceptDialogState

    // Multiplayer Config
    private val _playerTurn: MutableState<PlayerTurn> = mutableStateOf(PlayerTurn.NONE)
    val playerTurn: State<PlayerTurn> = _playerTurn

    private val _userMoveState: MutableState<Boolean> = mutableStateOf(false)
    val userMoveState: State<Boolean> = _userMoveState

    init {
        essentialInfo.getEssentialInfo().let {
            gameMode = it.gameMode
            gameLevel = it.gameLevel
            boardType = it.boardType
        }
        setUpDynamicBoard()
        timeLimitStart()
    }

    // ─── Board setup ────────────────────────────────────────────────────────────

    private fun setUpDynamicBoard() {
        gameMap = when (boardType) {
            BoardType.THREEX3 -> ArrayList((0 until 3).map { arrayListOf(0, 0, 0) })
            BoardType.FOURX4  -> ArrayList((0 until 5).map { arrayListOf(0, 0, 0, 0, 0) })
            BoardType.FIVEX5  -> ArrayList((0 until 6).map { arrayListOf(0, 0, 0, 0, 0, 0) })
        }
    }

    // ─── Turn helpers ───────────────────────────────────────────────────────────

    private fun getRandomTurnGenerator() {
        // BUG FIX: was `_offlineUserTurn.value == true` (redundant boolean comparison).
        _isUserTurnsComplete.value = _offlineUserTurn.value
        _offlineUserTurn.value = !_offlineUserTurn.value
    }

    private fun switchUserOnTimeOut() {
        if (_userTimer.value == 0F) {
            _userWarning.value = false
            setUserTurn()
            resetTimeLimit()
        }
    }

    // ─── Event dispatcher ───────────────────────────────────────────────────────

    fun onEvent(event: GameUsecase) {
        when (event) {
            is GameUsecase.UserMove -> {
                _userMoveState.value = event.state
            }

            is GameUsecase.OnUserTick -> {
                when (gameMode) {
                    GameMode.TWO_PLAYER -> {
                        if (_isUserTurnsComplete.value) {
                            // BUG FIX: was `.add()` in-place → Compose ignored it.
                            // Now reassigned to a new list so recomposition fires.
                            _playerOneIndex.value = _playerOneIndex.value + event.index
                        } else {
                            _playerTwoIndex.value = _playerTwoIndex.value + event.index
                        }
                        setGameMap(event.index)
                        playGame()
                    }

                    GameMode.AI -> {
                        if (_isUserTurnsComplete.value) {
                            _playerOneIndex.value = _playerOneIndex.value + event.index
                        }
                        setGameMap(event.index)
                        playGame()
                    }

                    GameMode.FRIEND -> updatePlayerData(event.index)

                    GameMode.FOUR_PLAYER -> {
                        when (_playerTurn.value) {
                            PlayerTurn.ONE   -> _playerOneIndex.value   = _playerOneIndex.value + event.index
                            PlayerTurn.TWO   -> _playerTwoIndex.value   = _playerTwoIndex.value + event.index
                            PlayerTurn.THREE -> _playerThreeIndex.value = _playerThreeIndex.value + event.index
                            PlayerTurn.FOUR  -> _playerFourIndex.value  = _playerFourIndex.value + event.index
                            else -> {}
                        }
                        setGameMap(event.index)
                        playGame()
                    }

                    else -> {}
                }
            }
            is GameUsecase.OnAIMoveImmediate -> {
                if (gameMode == GameMode.AI && gameResult.value == GameResult.NONE) {
                    viewModelScope.launch {
                        val thinkingDelay = when (boardType) {
                            BoardType.THREEX3 -> (250L..650L).random()
                            BoardType.FOURX4  -> (450L..1200L).random()
                            BoardType.FIVEX5  -> (700L..1800L).random()
                        }
                        val isEarlyGame = _playerOneIndex.value.size + _playerTwoIndex.value.size < 3
                        delay(if (isEarlyGame) thinkingDelay / 2 else thinkingDelay)

                        val aiValue = withContext(Dispatchers.Default) { aiMoveImmediate() }
                        delay((80L..180L).random())

                        // BUG FIX: same in-place mutation fix applied here.
                        _playerTwoIndex.value = _playerTwoIndex.value + aiValue
                        setGameMap(aiValue)
                        playGame()
                    }
                }
            }
            is GameUsecase.OnAIMove -> {
                if (gameMode == GameMode.AI && gameResult.value == GameResult.NONE) {
                    viewModelScope.launch {
                        val aiValue = withContext(Dispatchers.Default) { aiMove() }
                        _playerTwoIndex.value = _playerTwoIndex.value + aiValue
                        gameDelay()
                        setGameMap(aiValue)
                        playGame()
                    }
                }
            }

            is GameUsecase.OnGameRetry -> { /* play-again flow handled via OnClearGameBoard */ }

            is GameUsecase.OnBackPress -> _onBackPress.value = event.backPressState

            is GameUsecase.OnDelayLaunch -> _isDelayed.value = event.delayValue

            is GameUsecase.UpdateUserId -> _userId.value = event.userId

            is GameUsecase.UpdateFriendUserId -> _friendUserId.value = event.userId

            is GameUsecase.GameExitEvent -> {
                if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM) {
                    updateInGameInStore(false)
                    updatePlayGround()
                    removeGameBoard()
                }
                resetGameBoard()
            }

            is GameUsecase.OnUpdateGameSessionId -> _gameSessionId.value = event.sessionId

            is GameUsecase.OnClearGameBoard -> resetGameBoard()

            is GameUsecase.OnUpdateCurrentUserId -> _currentUserId.value = event.currentUserId

            // BUG FIX: OnUpdateInGameInfo had NO handler — the `else -> {}` swallowed
            // it silently, so `_inGame` was never updated from this event.
            is GameUsecase.OnUpdateInGameInfo -> _inGame.value = event.value

            is GameUsecase.OnCancelPlayRequest -> {
                _requestDialogState.value = false
                _acceptDialogState.value = false
            }

            is GameUsecase.OnAcceptPlayAgainRequest -> {
                _requestDialogState.value = false
                _acceptDialogState.value = false
                acceptPlayAgainRequest()
            }

            is GameUsecase.OnPlayerCountUpdate -> _playerCount.value = event.playerCount
        }
    }

    // ─── Play Again ─────────────────────────────────────────────────────────────

    private suspend fun gameDelay(){
        if (boardType != BoardType.THREEX3){
            return
        }
        if (boardType == BoardType.THREEX3){
            when(gameLevel){
                GameLevel.EASY -> { delay((180..150).random().toLong()) }
                GameLevel.MEDIUM -> {delay((100..130).random().toLong())}
                GameLevel.HARD -> {}
                GameLevel.NONE -> {}
            }
        }
    }

    private fun acceptPlayAgainRequest() {
        viewModelScope.launch(Dispatchers.IO) {
            val playAgainRequest = BoardState.PlayRequest(
                requesterId = _userId.value,
                retryRequest = false,
                acceptRequest = true
            )
            val resetBoardState = _gameBoardState.value.copy(
                playerOneState = BoardState.Player(
                    _gameBoardState.value.playerOneState?.userId ?: "",
                    indexes = emptyList()
                ),
                playerTwoState = BoardState.Player(
                    _gameBoardState.value.playerTwoState?.userId ?: "",
                    indexes = emptyList()
                ),
                currentUserTurnId = if (_onlineGameWinner.value == _userId.value)
                    _friendUserId.value else _userId.value,
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
            val playAgainRequest = BoardState.PlayRequest(
                requesterId = _userId.value,
                retryRequest = true,
                acceptRequest = false
            )
            val resetBoardState = _gameBoardState.value.copy(
                playerOneState = BoardState.Player(
                    _gameBoardState.value.playerOneState?.userId ?: "",
                    indexes = emptyList()
                ),
                playerTwoState = BoardState.Player(
                    _gameBoardState.value.playerTwoState?.userId ?: "",
                    indexes = emptyList()
                ),
                currentUserTurnId = if (_currentUserId.value == _friendUserId.value)
                    _userId.value else _friendUserId.value,
                gameWinnerId = "",
                resetTimer = true,
                gameDraw = false,
                playAgain = playAgainRequest
            )
            cloudRepository.playAgainRequest(_gameSessionId.value, resetBoardState)
        }
        resetGameBoard()
    }

    // ─── Game flow ──────────────────────────────────────────────────────────────

    private fun playGame() {
        when (gameMode) {
            // BUG FIX: TWO_PLAYER previously called setUserTurn() BEFORE checkGameWinner(),
            // so `_isUserTurnsComplete` was already flipped when the winner name was
            // resolved in the Fragment — causing P1 win to display "Player 2" and vice versa.
            // Fix: check winner first, only switch turn / reset timer when game continues.
            GameMode.TWO_PLAYER -> {
                checkGameWinner()
                checkDraw()
                if (_gameResult.value == GameResult.NONE) {
                    setUserTurn()
                    resetTimeLimit()
                }
            }
            GameMode.FOUR_PLAYER -> {
                checkGameWinner()
                checkDraw()
                if (_gameResult.value == GameResult.NONE) {
                    setMultiplayerTurn()
                    resetTimeLimit()
                }
            }
            GameMode.AI -> {
                checkGameWinner()
                checkDraw()
                if (_gameResult.value == GameResult.NONE) {
                    setUserTurn()
                    resetTimeLimit()
                }
            }
            GameMode.FRIEND -> {
                checkGameWinner()
                checkDraw()
                if (_gameResult.value == GameResult.NONE) resetTimeLimit()
            }
            GameMode.RANDOM -> {}
        }
    }

    fun setMultiplayerRandomTurn() {
        val randomTurn = (1..4).random()
        _playerTurn.value = when (randomTurn) {
            1    -> PlayerTurn.ONE
            2    -> PlayerTurn.TWO
            3    -> PlayerTurn.THREE
            else -> PlayerTurn.FOUR
        }
    }

    private fun setMultiplayerTurn() {
        _playerTurn.value = when (_playerTurn.value) {
            PlayerTurn.ONE  -> PlayerTurn.TWO
            PlayerTurn.TWO  -> PlayerTurn.THREE
            PlayerTurn.THREE -> PlayerTurn.FOUR
            PlayerTurn.FOUR -> PlayerTurn.ONE
            PlayerTurn.NONE -> PlayerTurn.ONE
        }
    }

    // ─── Win / Draw detection ───────────────────────────────────────────────────

    private fun checkDraw() {
        // BUG FIX: Original used nested forEach with a mutable flag — replaced with
        // a short-circuit `any` expression (no allocation, early exit on first zero).
        val isDraw = gameMap.none { row -> row.any { it == 0 } }
        when (gameMode) {
            GameMode.TWO_PLAYER -> if (isDraw && _gameResult.value == GameResult.NONE) _gameResult.value = GameResult.DRAW
            GameMode.AI         -> if (isDraw) _gameResult.value = GameResult.DRAW
            GameMode.FRIEND     -> if (isDraw) _gameResult.value = GameResult.DRAW
            // FOUR_PLAYER draw is not resolvable via full-board check alone (4 players,
            // different win-length) — left as a no-op pending game-specific draw logic.
            else -> {}
        }
    }

    private fun checkGameWinner() {
        var validateWinner = false
        Log.i(TAG, "checkGameWinner: boardType=$boardType mode=$gameMode")

        if (gameMode == GameMode.FOUR_PLAYER) {
            validateWinner = when (boardType) {
                BoardType.FOURX4 -> GameWinner.checkAllMultiplayerPositionsByBoard(gameMap, 4, 3)
                BoardType.FIVEX5 -> GameWinner.checkAllMultiplayerPositionsByBoard(gameMap, 5, 4)
                else             -> false
            }
        } else {
            validateWinner = when (boardType) {
                BoardType.THREEX3 -> checkDiagonalPossibility() || checkVerticalPossibility() || checkHorizontalPossibility()
                BoardType.FOURX4  -> GameWinner.checkAllPositionsByBoard(gameMap, 4)
                BoardType.FIVEX5  -> GameWinner.checkAllPositionsByBoard(gameMap, 5)
            }
        }

        if (!validateWinner) return

        when (gameMode) {
            GameMode.TWO_PLAYER -> {
                // BUG FIX: winner is now checked BEFORE setUserTurn() so
                // _isUserTurnsComplete still reflects who just played.
                Log.i(TAG, "checkGameWinner: ${if (_isUserTurnsComplete.value) "P1" else "P2"} wins")
                _gameResult.value = GameResult.WIN
            }
            GameMode.AI -> {
                _gameResult.value = if (_isUserTurnsComplete.value) GameResult.WIN else GameResult.LOSE
            }
            GameMode.FRIEND -> {
                if (_currentUserId.value == _userId.value) {
                    _onlineGameWinner.value = _friendUserId.value
                    _gameResult.value = GameResult.LOSE
                } else {
                    _onlineGameWinner.value = _userId.value
                    _gameResult.value = GameResult.WIN
                }
                initiateExitGameRequest()
            }
            GameMode.FOUR_PLAYER -> _gameResult.value = GameResult.WIN
            else -> {}
        }
    }

    // ─── 3×3 win checks (allocation-free) ──────────────────────────────────────

    private fun checkHorizontalPossibility(): Boolean {
        for (player in 1..2) {
            gameMap.forEachIndexed { rowIdx, row ->
                if (row.all { it == player }) {
                    setWinnerList(listOf(rowIdx * 3, rowIdx * 3 + 1, rowIdx * 3 + 2))
                    return true
                }
            }
        }
        return false
    }

    private fun checkVerticalPossibility(): Boolean {
        // BUG FIX: original allocated 3 new ArrayLists on every invocation.
        // Now reads cells directly from gameMap with no intermediate collection.
        for (col in 0..2) {
            for (player in 1..2) {
                if (gameMap[0][col] == player &&
                    gameMap[1][col] == player &&
                    gameMap[2][col] == player
                ) {
                    setWinnerList(listOf(col, col + 3, col + 6))
                    return true
                }
            }
        }
        return false
    }

    private fun checkDiagonalPossibility(): Boolean {
        // BUG FIX: same allocation fix — read cells directly.
        for (player in 1..2) {
            if (gameMap[0][0] == player && gameMap[1][1] == player && gameMap[2][2] == player) {
                setWinnerList(listOf(0, 4, 8)); return true
            }
            if (gameMap[0][2] == player && gameMap[1][1] == player && gameMap[2][0] == player) {
                setWinnerList(listOf(2, 4, 6)); return true
            }
        }
        return false
    }

    private fun setWinnerList(winIndexes: List<Int>) {
        _winnerIndexList.value = winIndexes
        Log.i(TAG, "setWinnerList: $winIndexes")
    }

    // ─── GameMap helpers ────────────────────────────────────────────────────────

    /**
     * BUG FIX: Original had two setGameMap overloads, each with multi-level if-else
     * range chains repeated per BoardType (dozens of branches).  Replaced with a single
     * O(1) formula: row = index / cols, col = index % cols.  Works for any board size.
     */
    private fun setGameMap(index: Int, inputData: Int) {
        val cols = gameMap[0].size
        val row = index / cols
        val col = index % cols
        if (row in gameMap.indices && col in 0 until cols) {
            gameMap[row][col] = inputData
        }
        Log.i("GameMap", "setGameMap[$row][$col]=$inputData map=${gameMap}")
    }

    private fun setGameMap(index: Int) {
        val inputData = if (gameMode == GameMode.FOUR_PLAYER) {
            when (_playerTurn.value) {
                PlayerTurn.ONE   -> 1
                PlayerTurn.TWO   -> 2
                PlayerTurn.THREE -> 3
                PlayerTurn.FOUR  -> 4
                else             -> 1
            }
        } else {
            if (_isUserTurnsComplete.value) 1 else 2
        }
        setGameMap(index, inputData)
    }

    private fun isZeroInGameMap(): Boolean =
        // BUG FIX: was a nested forEach with early return — replaced with idiomatic any().
        gameMap.any { row -> row.any { it == 0 } }

    // ─── Turn management ────────────────────────────────────────────────────────

    private fun setUserTurn() {
        if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM) {
            // BUG FIX: Original had a double-nested viewModelScope.launch(IO) { launch(IO) {…} }
            // — the inner launch was completely redundant and wasted a coroutine slot.
            viewModelScope.launch(Dispatchers.IO) {
                val nextTurnId = if (_gameBoardState.value.currentUserTurnId == _userId.value)
                    _friendUserId.value else _userId.value
                cloudRepository.updateGameBoard(
                    _gameSessionId.value,
                    _gameBoardState.value.copy(currentUserTurnId = nextTurnId, resetTimer = true)
                )
            }
        } else {
            _isUserTurnsComplete.value = !_isUserTurnsComplete.value
        }
    }

    // ─── Timer ──────────────────────────────────────────────────────────────────

    private fun timeLimitStart() {
        job = viewModelScope.launch(Dispatchers.Default) {
            var timeBound = if (boardType == BoardType.THREEX3) 10 else 30
            while (timeBound != 0) {
                delay(1000L)
                _userTimer.value = if (boardType == BoardType.THREEX3)
                    timeBound / 10F
                else
                    (timeBound / 10F) / 3F
                timeBound--
                if (timeBound == 3) _userWarning.value = true
            }
            _userTimer.value = 0F
            switchUserOnTimeOut()
        }
    }

    private fun resetTimeLimit() {
        job?.cancel()
        if (isZeroInGameMap()) timeLimitStart()
        else _userTimer.value = 0F
    }

    // ─── Board reset ─────────────────────────────────────────────────────────────

    private fun resetGameBoard() {
        job?.cancel()
        setUpDynamicBoard()
        _gameResult.value = GameResult.NONE
        _playerOneIndex.value = emptyList()
        _playerTwoIndex.value = emptyList()
        _playerThreeIndex.value = emptyList()
        _playerFourIndex.value = emptyList()
        _userTimer.value = 0F
        _userMoveState.value = false
        when (gameMode) {
            GameMode.AI          -> setAIRetryTurn()
            GameMode.FOUR_PLAYER -> setMultiplayerRandomTurn()
            else                 -> getRandomTurnGenerator()
        }
        _winnerIndexList.value = listOf(-1, -1, -1)
        onEvent(GameUsecase.OnDelayLaunch(false))
        _userWarning.value = false
        _onlineGameWinner.value = ""
        _gameBoardState.value = BoardState()
        timeLimitStart()
    }

    private fun setAIRetryTurn() {
        if (gameMode == GameMode.AI) {
            if (_offlineUserTurn.value) {
                _isUserTurnsComplete.value = true
                _offlineUserTurn.value = false
            } else {
                _isUserTurnsComplete.value = false
                _offlineUserTurn.value = true
            }
            if (!_isUserTurnsComplete.value){
                onEvent(GameUsecase.OnAIMoveImmediate)
            }
        }
    }

    fun setAITurn() {
        if (gameMode == GameMode.AI) {
            _offlineUserTurn.value = false
            _isUserTurnsComplete.value = true
        }
    }

    // ─── AI ─────────────────────────────────────────────────────────────────────

    private fun aiMoveImmediate(): Int {
        val minimax: GameBrain = IGameBrain
        minimax.setAITurn(true)
        val bestMove: Move = when (boardType) {
            BoardType.THREEX3 -> minimax.getFirstMove( 3)
            BoardType.FOURX4  -> minimax.getFirstMove( 4)
            BoardType.FIVEX5  -> minimax.getFirstMove( 5)
        }
        return (gameMap[0].size * bestMove.row) + bestMove.col
    }

    private fun aiMove(): Int {
        val minimax: GameBrain = IGameBrain
        minimax.setAITurn(true)
        val difficulty = getDifficultyLevel(gameLevel)
        val bestMove: Move = when (boardType) {
            BoardType.THREEX3 -> minimax.getOptimalMove(gameMap, 3, difficulty)
            BoardType.FOURX4  -> minimax.getOptimalMove(gameMap, 4, 2)
            BoardType.FIVEX5  -> minimax.getOptimalMove(gameMap, 5, 2)
        }
        return (gameMap[0].size * bestMove.row) + bestMove.col
    }

    // BUG FIX: renamed from `getDiificultyLevel` (typo in original).
    private fun getDifficultyLevel(level: GameLevel) = when (level) {
        GameLevel.EASY   -> 0
        GameLevel.MEDIUM -> 1
        GameLevel.HARD   -> 2
        GameLevel.NONE   -> 2
    }

    // ─── Online / Cloud ──────────────────────────────────────────────────────────

    fun gameBoardUpdate(gameBoard: BoardState) {
        Log.i(TAG, "gameBoardUpdate: $gameBoard")
        gameBoard.playAgain?.let {
            if (!it.acceptRequest) {
                if (it.requesterId == _userId.value) {
                    _requestDialogState.value = true
                    _acceptDialogState.value = false
                } else {
                    _requestDialogState.value = false
                    _acceptDialogState.value = true
                }
            } else {
                _requestDialogState.value = false
                _acceptDialogState.value = false
            }
        }
        if (gameBoard.resetTimer) resetTimeLimit()
        if (_onlineGameWinner.value.isNotEmpty() || isGameDraw.value) return

        _currentUserId.value = gameBoard.currentUserTurnId
        _friendUserId.value = if (_userId.value == _gameSessionId.value)
            gameBoard.playerTwoState?.userId ?: ""
        else
            gameBoard.playerOneState?.userId ?: ""

        if (_gameSessionId.value == _userId.value) {
            _playerOneIndex.value = gameBoard.playerOneState?.indexes ?: emptyList()
            _playerTwoIndex.value = gameBoard.playerTwoState?.indexes ?: emptyList()
        } else {
            _playerOneIndex.value = gameBoard.playerTwoState?.indexes ?: emptyList()
            _playerTwoIndex.value = gameBoard.playerOneState?.indexes ?: emptyList()
        }

        _gameBoardState.value = gameBoard
        updateGameMapIndexes(_playerOneIndex.value, 1)
        updateGameMapIndexes(_playerTwoIndex.value, 2)
        checkDraw()
        checkGameWinner()
    }

    private fun initiateExitGameRequest() {
        viewModelScope.launch(Dispatchers.IO) {
            _gameBoardState.value = _gameBoardState.value.copy(
                gameWinnerId = _onlineGameWinner.value,
                resetTimer = false,
                currentUserTurnId = ""
            )
            cloudRepository.updateGameBoard(_gameSessionId.value, _gameBoardState.value)
        }
    }

    private fun updateGameMapIndexes(indexes: List<Int>, playerMoveValue: Int) {
        indexes.forEach { setGameMap(it, playerMoveValue) }
    }

    private fun updatePlayerData(index: Int) {
        var boardState = _gameBoardState.value
        if (_gameSessionId.value == _userId.value) {
            val playerOneState = (boardState.playerOneState
                ?: BoardState.Player(userId = _gameSessionId.value, indexes = emptyList()))
                .let { it.copy(indexes = it.indexes + index) }
            boardState = boardState.copy(
                currentUserTurnId = _friendUserId.value,
                playerOneState = playerOneState,
                playerTwoState = _gameBoardState.value.playerTwoState
            )
        } else {
            val playerTwoState = (boardState.playerTwoState
                ?: BoardState.Player(userId = _userId.value, indexes = emptyList()))
                .let { it.copy(indexes = it.indexes + index) }
            boardState = boardState.copy(
                currentUserTurnId = _friendUserId.value,
                playerOneState = _gameBoardState.value.playerOneState,
                playerTwoState = playerTwoState
            )
        }
        boardState = boardState.copy(
            resetTimer = true,
            gameWinnerId = _onlineGameWinner.value,
            gameDraw = _isGameDraw.value
        )
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.updateGameBoard(_gameSessionId.value, boardState)
        }
    }

    private fun removeGameBoard() {
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

    private fun updateInGameInStore(inGame: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = gameStoreRepository.fetchPreference().firstOrNull()
            data?.playGround?.let {
                gameStoreRepository.updatePlayground(it.copy(inGame = inGame, randomSearch = false))
            }
        }
    }
}