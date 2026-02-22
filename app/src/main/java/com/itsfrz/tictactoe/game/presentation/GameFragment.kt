package com.itsfrz.tictactoe.game.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.GameDialogue
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameLevel
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.GameResult
import com.itsfrz.tictactoe.common.enums.PlayerTurn
import com.itsfrz.tictactoe.common.functionality.GameSound
import com.itsfrz.tictactoe.common.functionality.GameWinner
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.common.state.EssentialInfo
import com.itsfrz.tictactoe.common.state.IEssentialInfo
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.game.domain.usecase.GameUsecase
import com.itsfrz.tictactoe.game.presentation.components.GameBoard
import com.itsfrz.tictactoe.game.presentation.components.GameDivider
import com.itsfrz.tictactoe.game.presentation.components.ProgressTimer
import com.itsfrz.tictactoe.game.presentation.viewmodel.GameViewModel
import com.itsfrz.tictactoe.game.presentation.viewmodel.GameViewModelFactory
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.IGameStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var job: Job? = null
    private lateinit var viewModel: GameViewModel
    private lateinit var commonViewModel: CommonViewModel
    private lateinit var gameMode: GameMode
    private lateinit var gameLevel: GameLevel
    private lateinit var boardType: BoardType
    private lateinit var cloudRepository: CloudRepository
    private lateinit var dataStoreRepository: GameStoreRepository
    private lateinit var gameSound: GameSound

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onEvent(GameUsecase.OnBackPress(true))
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpOnlineConfig()
        gameMode = requireArguments().getSerializable(BundleKey.GAME_MODE) as GameMode
        gameLevel = requireArguments().getSerializable(BundleKey.SELECTED_LEVEL) as GameLevel
        boardType = requireArguments().getSerializable(BundleKey.BOARD_TYPE) as BoardType
        Log.i(
            "GAME_MODE",
            "onCreate: Game Mode ${gameMode}, Selected Level ${gameLevel}, Board Type ${boardType}"
        )
        val essentialInfo: EssentialInfo = IEssentialInfo(gameMode, gameLevel, boardType)
        val viewModelFactory =
            GameViewModelFactory(cloudRepository, dataStoreRepository, essentialInfo)
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[GameViewModel::class.java]
        commonViewModel = CommonViewModel.getInstance()
        gameSound = commonViewModel.gameSound
        viewModel.setAITurn()
        setUpNavArgs()
        if (gameMode == GameMode.RANDOM || gameMode == GameMode.FRIEND) {
            job = CoroutineScope(Dispatchers.IO).launch {
                dataStoreRepository.fetchPreference().collectLatest {
                    viewModel.onEvent(GameUsecase.UpdateUserId(it.userProfile?.userId ?: ""))
                    viewModel.onEvent(GameUsecase.OnUpdateInGameInfo(it.playGround?.inGame ?: true))
                    cloudRepository.fetchPlaygroundInfoAndStore(viewModel.userId.value)
                    cloudRepository.fetchGameBoardInfoAndStore(viewModel.gameSessionId.value)
                    Log.i("BPLAY_AGAIN", "onCreate: Board State ${it.boardState}")
                    it.boardState?.let {
                        viewModel.gameBoardUpdate(it)
                    }
                    it.playGround?.let {
                        viewModel.playGroundUpdate(it)
                    }
                }
            }
        }
        if (gameMode == GameMode.FOUR_PLAYER) {
            viewModel.setMultiplayerRandomTurn()
        }
    }

    private fun setUpOnlineConfig() {
        val gameStore = GameDataStore.getDataStore(requireContext())
        dataStoreRepository = IGameStoreRepository(gameStore)
        cloudRepository = CloudRepository(
            dataStoreRepository = dataStoreRepository,
            scope = CoroutineScope(Dispatchers.IO)
        )
    }

    private fun setUpNavArgs() {
        val friendUserId = requireArguments().getString(BundleKey.FRIEND_ID)
        friendUserId?.let {
            if (it.isNotEmpty())
                viewModel.onEvent(GameUsecase.UpdateFriendUserId(it))
        }
        val userId = requireArguments().getString(BundleKey.USER_ID)
        userId?.let {
            if (it.isNotEmpty())
                viewModel.onEvent(GameUsecase.UpdateUserId(it))
        }
        val gameSessionId = requireArguments().getString(BundleKey.SESSION_ID)
        gameSessionId?.let {
            if (it.isNotEmpty()) {
                viewModel.onEvent(GameUsecase.OnUpdateGameSessionId(it))
                viewModel.onEvent(GameUsecase.OnUpdateCurrentUserId(it))
            }
        }
        Log.i(
            "ID_CHECK",
            "setUpNavArgs: Friend User ID ${friendUserId} Session Id ${gameSessionId} User Id ${userId}"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {

                val playerOneData = viewModel.playerOneIndex.value
                val playerTwoData = viewModel.playerTwoIndex.value
                val playerThreeData = viewModel.playerThreeIndex.value
                val playerFourData = viewModel.playerFourIndex.value
                val winnerIndexList = viewModel.winnerIndexList.value
                val playerTurns = viewModel.isUserTurnsComplete.value
                val multiplayerTurn = viewModel.playerTurn.value
                val userTimeLimit = viewModel.userTimer.value
                val userTimeOutWarning = viewModel.userWarning.value
                val userTimeOutPulsatingWarning by rememberInfiniteTransition().animateFloat(
                    initialValue = if (userTimeOutWarning) 20F else 23F,
                    targetValue = 23F,
                    animationSpec = infiniteRepeatable(tween(200), RepeatMode.Reverse)
                )
                val timeLimitAnimation by animateFloatAsState(
                    targetValue = userTimeLimit,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                )
                val gameResult = viewModel.gameResult.value
                val onBackPress = viewModel.onBackPress.value
                val isDelayed = viewModel.isDelayed.value
                val userId = viewModel.userId.value
                val currentUserId = viewModel.currentUserId.value
                val friendUserId = viewModel.friendUserId.value
                val inGame = viewModel.inGame.value
                val requestDialogState = viewModel.requestDialogState.value
                val acceptDialogState = viewModel.acceptDialogState.value

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = ThemePicker.primaryColor.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    )

                    ProgressTimer(
                        userTimeOutPulsatingWarning = userTimeOutPulsatingWarning,
                        timeLimitAnimation = timeLimitAnimation,
                        playerTurns = playerTurns,
                        avatar = getCurrentAvatar(
                            gameMode,
                            currentUserId,
                            friendUserId,
                            playerTurns,
                            multiplayerTurn
                        ),
                        currentUserId = currentUserId,
                        gameMode = gameMode,
                        userId = userId,
                        playerTurn = multiplayerTurn,
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    )
                    GameDivider()
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )
                    GameBoard(
                        crossList = if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM || gameMode == GameMode.AI || gameMode == GameMode.FOUR_PLAYER) playerTwoData else playerOneData,
                        rightList = if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM || gameMode == GameMode.AI || gameMode == GameMode.FOUR_PLAYER) playerOneData else playerTwoData,
                        player3List = playerThreeData,
                        player4List = playerFourData,
                        userId = userId,
                        currentUserId = currentUserId,
                        gameMode = gameMode,
                        gameCellList = calculateCellList(),
                        columnCount = calculateBoardColumnCount(),
                        boardHeight = calculateBoardHeight(),
                        winnerIndexList = if (boardType == BoardType.THREEX3) winnerIndexList else GameWinner.winnerIndexList.value,
                        isWinner = gameResult != GameResult.NONE && gameResult != GameResult.DRAW,
                        isPlayerMoved = !playerTurns,
                        onMove = { index ->
                            Log.i("HIDDEN_BUG", "onCreateView: ${index}")
                            gameSound.pieceClick1MovingSound()
                            viewModel.onEvent(GameUsecase.OnUserTick(index))
                        },
                        onAIMove = {
                            Log.i("AI_MOVE", "onCreateView: On AI Move")
                            gameSound.pieceClick2MovingSound()
                            viewModel.onEvent(GameUsecase.OnAIMove)
                        },
                        playerIcons = commonViewModel.getResourceIdList(),
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )
                    GameDivider()
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    )
                }
                if (gameResult != GameResult.NONE) {
                    LaunchedEffect(Unit) {
                        if (gameResult == GameResult.DRAW || gameResult == GameResult.WIN)
                            delay(1000)
                        else delay(2000)
                        viewModel.onEvent(GameUsecase.OnDelayLaunch(true))
                    }
                }
                if (gameResult != GameResult.NONE && isDelayed) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = ThemePicker.primaryColor.value)
                            .padding(horizontal = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (gameResult) {
                                GameResult.WIN -> {
                                    GameDialogue.GameWinDialogue(
                                        winnerUsername = getWinnerName(playerTurns),
                                        dialogueButtonText = "Play Again",
                                        onCloseEvent = {
                                            findNavController().popBackStack()
                                            findNavController().navigateUp()
                                            viewModel.onEvent(GameUsecase.GameExitEvent)
                                        },
                                        commonViewModel = commonViewModel
                                    ) {
                                        viewModel.onEvent(GameUsecase.OnGameRetry)
                                        if ((gameMode == GameMode.TWO_PLAYER || gameMode == GameMode.FOUR_PLAYER || gameMode == GameMode.AI))
                                            viewModel.onEvent(GameUsecase.OnClearGameBoard)
                                    }
                                }

                                GameResult.DRAW, GameResult.LOSE -> {
                                    if (!acceptDialogState) {
                                        GameDialogue.GameDrawLoseDialogue(
                                            gameResult = gameResult,
                                            commonViewModel = commonViewModel,
                                            onCloseEvent = {
                                                findNavController().popBackStack()
                                                findNavController().navigateUp()
                                                viewModel.onEvent(GameUsecase.GameExitEvent)
                                            }
                                        ) {
                                            viewModel.onEvent(GameUsecase.OnGameRetry)
                                            if ((gameMode == GameMode.TWO_PLAYER || gameMode == GameMode.AI))
                                                viewModel.onEvent(GameUsecase.OnClearGameBoard)
                                        }
                                    }
                                }

                                else -> {}
                            }
                        }
                    }
                }
                if (onBackPress || !inGame) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = ThemePicker.primaryColor.value)
                            .clickable { }
                            .padding(horizontal = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            GameDialogue.GameDialog(
                                onExitEvent = {
                                    viewModel.onEvent(GameUsecase.GameExitEvent)
                                    findNavController().popBackStack()
                                    findNavController().navigateUp()
                                    viewModel.onEvent(GameUsecase.OnBackPress(false))
                                },
                                onContinueEvent = {
                                    viewModel.onEvent(GameUsecase.OnBackPress(false))
                                },
                                titleText = "Do you really want to exit ?",
                                commonViewModel = commonViewModel
                            )
                        }
                    }
                }
                if (requestDialogState) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = ThemePicker.primaryColor.value)
                            .clickable { }
                            .padding(horizontal = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            GameDialogue.PlayRequestBox(
                                commonViewModel = commonViewModel,
                                onCloseClick = {
                                    viewModel.onEvent(GameUsecase.OnCancelPlayRequest)
                                    viewModel.onEvent(GameUsecase.OnClearGameBoard)
                                    findNavController().navigateUp()
                                })
                        }
                    }
                }
                if (acceptDialogState) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = ThemePicker.primaryColor.value)
                            .clickable { }
                            .padding(horizontal = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            GameDialogue.GameDialog(
                                onExitEvent = {
                                    viewModel.onEvent(GameUsecase.OnAcceptPlayAgainRequest)
                                },
                                onContinueEvent = {
                                    viewModel.onEvent(GameUsecase.OnCancelPlayRequest)
                                    findNavController().popBackStack()
                                    findNavController().navigateUp()
                                },
                                headerText = "Play Request",
                                titleText = "Do you want to play again ?",
                                buttonText = "Yes",
                                commonViewModel = commonViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getCurrentAvatar(
        gameMode: GameMode,
        currentUserId: String,
        friendUserId: String,
        playerTurns: Boolean,
        multiplayerTurn: PlayerTurn
    ): Int {
        return if (gameMode == GameMode.FOUR_PLAYER) {
            when (multiplayerTurn) {
                PlayerTurn.ONE -> commonViewModel.getResourceIdList()[0]
                PlayerTurn.TWO -> commonViewModel.getResourceIdList()[1]
                PlayerTurn.THREE -> commonViewModel.getResourceIdList()[2]
                PlayerTurn.FOUR -> commonViewModel.getResourceIdList()[3]
                else -> commonViewModel.getResourceIdList()[0]
            }
        } else {
            if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM) {
                if (currentUserId == friendUserId) {
                    R.drawable.ic_cross_i
                } else {
                    R.drawable.ic_tick_i
                }
            } else if (gameMode == GameMode.AI) {
                if (!playerTurns) {
                    R.drawable.ic_ai_emoji
                } else {
                    commonViewModel.getResourceIdList()[0]
                }
            } else if (gameMode == GameMode.TWO_PLAYER) {
                if (!playerTurns) {
                    commonViewModel.getResourceIdList()[0]
                } else
                    commonViewModel.getResourceIdList()[1]
            } else {
                R.drawable.ic_tick_i
            }
        }
    }

    private fun getWinnerName(playerTurns: Boolean): String {
        return if (gameMode == GameMode.AI && !playerTurns) "You Win"
        else if (gameMode == GameMode.FRIEND && !playerTurns) "You Win"
        else if (playerTurns) "Player 1" else "Player 2"
    }

    private fun calculateCellList(): List<Int> {
        return when (boardType) {
            BoardType.THREEX3 -> (1..9).toList()
            BoardType.FOURX4 -> (1..25).toList()
            BoardType.FIVEX5 -> (1..36).toList()
        }
    }

    private fun calculateBoardColumnCount(): Int {
        return when (boardType) {
            BoardType.THREEX3 -> 3
            BoardType.FOURX4 -> 5
            BoardType.FIVEX5 -> 6
        }
    }

    private fun calculateBoardHeight(): Dp {
        return when (boardType) {
            BoardType.THREEX3 -> 100.dp
            BoardType.FOURX4 -> 70.dp
            BoardType.FIVEX5 -> 55.dp
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onEvent(GameUsecase.GameExitEvent)
        job?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.onEvent(GameUsecase.GameExitEvent)
        job?.cancel()
    }
}