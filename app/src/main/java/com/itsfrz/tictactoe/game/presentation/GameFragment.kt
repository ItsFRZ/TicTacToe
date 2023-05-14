package com.itsfrz.tictactoe.game.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.GameDialogue
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameLevel
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.GameResult
import com.itsfrz.tictactoe.common.functionality.GameWinner
import com.itsfrz.tictactoe.common.functionality.NavOptions
import com.itsfrz.tictactoe.common.state.EssentialInfo
import com.itsfrz.tictactoe.common.state.IEssentialInfo
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.game.presentation.components.GameBoard
import com.itsfrz.tictactoe.game.presentation.components.GameDivider
import com.itsfrz.tictactoe.game.presentation.components.UserMove
import com.itsfrz.tictactoe.game.domain.usecase.GameUsecase
import com.itsfrz.tictactoe.game.presentation.components.ProgressTimer
import com.itsfrz.tictactoe.game.presentation.viewmodel.GameViewModel
import com.itsfrz.tictactoe.game.presentation.viewmodel.GameViewModelFactory
import com.itsfrz.tictactoe.goonline.data.firebase.FirebaseDB
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.IGameStoreRepository
import com.itsfrz.tictactoe.ui.theme.PrimaryMain
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.ThemeBlueLight
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class GameFragment : Fragment(){

    private var job : Job? = null
    private lateinit var viewModel: GameViewModel
    private lateinit var commonViewModel: CommonViewModel
    private lateinit var gameMode : GameMode
    private lateinit var gameLevel : GameLevel
    private lateinit var boardType: BoardType
    private lateinit var cloudRepository: CloudRepository
    private lateinit var dataStoreRepository  : GameStoreRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true){
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
        Log.i("GAME_MODE", "onCreate: Game Mode ${gameMode}, Selected Level ${gameLevel}, Board Type ${boardType}")
        val essentialInfo : EssentialInfo = IEssentialInfo(gameMode, gameLevel, boardType)
        val viewModelFactory = GameViewModelFactory(cloudRepository,dataStoreRepository,essentialInfo)
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[GameViewModel::class.java]
        commonViewModel = CommonViewModel.getInstance()
        viewModel.setAITurn()
        setUpNavArgs()
        if (gameMode == GameMode.RANDOM || gameMode == GameMode.FRIEND){
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
    }

    private fun setUpOnlineConfig() {
        val database = FirebaseDB
        val gameStore =  GameDataStore.getDataStore(requireContext())
        dataStoreRepository = IGameStoreRepository(gameStore)
        cloudRepository = CloudRepository(
            database = database,
            dataStoreRepository = dataStoreRepository,
            scope = CoroutineScope(Dispatchers.IO)
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
                val playerThreeData = viewModel.playerTwoIndex.value
                val playerFourData = viewModel.playerTwoIndex.value
                val winnerIndexList = viewModel.winnerIndexList.value
                val playerTurns = viewModel.isUserTurnsComplete.value
                val userTimeLimit = viewModel.userTimer.value
                val userTimeOutWarning = viewModel.userWarning.value
                val userTimeOutPulsatingWarning by rememberInfiniteTransition().animateFloat(
                    initialValue = if (userTimeOutWarning) 20F else 23F,
                    targetValue = 23F,
                    animationSpec = infiniteRepeatable(tween(200),RepeatMode.Reverse)
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

                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(color = PrimaryMain),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp))

                    ProgressTimer(
                        userTimeOutPulsatingWarning = userTimeOutPulsatingWarning,
                        timeLimitAnimation = timeLimitAnimation,
                        playerTurns = playerTurns,
                        currentUserId = currentUserId,
                        gameMode = gameMode,
                        userId = userId,
                        isCross = if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM) currentUserId == friendUserId else if (gameMode == GameMode.AI)  !playerTurns else playerTurns
                    )
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp))
                    GameDivider()
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp))
                    GameBoard(
                        crossList = if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM || gameMode == GameMode.AI) playerTwoData else playerOneData,
                        rightList = if (gameMode == GameMode.FRIEND || gameMode == GameMode.RANDOM || gameMode == GameMode.AI) playerOneData else playerTwoData,
                        userId = userId,
                        currentUserId = currentUserId,
                        gameMode = gameMode,
                        gameCellList = calculateCellList(),
                        columnCount = calculateBoardColumnCount(),
                        boardHeight = calculateBoardHeight(),
                        winnerIndexList = if (boardType == BoardType.THREEX3) winnerIndexList else GameWinner.winnerIndexList.value,
                        isWinner = gameResult != GameResult.NONE,
                        isPlayerMoved = !playerTurns,
                        onMove = { index -> viewModel.onEvent(GameUsecase.OnUserTick(index)) },
                        onAIMove = {
                            Log.i("AI_MOVE", "onCreateView: On AI Move")
                            viewModel.onEvent(GameUsecase.OnAIMove)
                        },
                        playerIcons = commonViewModel.getResourceIdList(),
                    )
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp))
                    GameDivider()
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp))
                }
                if (gameResult != GameResult.NONE){
                    LaunchedEffect(Unit){
                        if (gameResult == GameResult.DRAW || gameResult == GameResult.WIN)
                            delay(1000)
                        else delay(2000)
                        viewModel.onEvent(GameUsecase.OnDelayLaunch(true))
                    }
                }
                if (gameResult != GameResult.NONE && isDelayed){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = PrimaryMain)
                            .padding(horizontal = 30.dp),
                        contentAlignment = Alignment.Center
                    ){
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when(gameResult){
                                GameResult.WIN -> {
                                    GameDialogue.GameWinDialogue(
                                        winnerUsername = getWinnerName(playerTurns),
                                        dialogueButtonText = "Play Again",
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
                                GameResult.DRAW,GameResult.LOSE -> {
                                    if (!acceptDialogState){
                                        GameDialogue.GameDrawLoseDialogue(
                                            gameResult = gameResult,
                                            onCloseEvent = {
                                                findNavController().popBackStack()
                                                findNavController().navigateUp()
                                                viewModel.onEvent(GameUsecase.GameExitEvent)
                                            }
                                        ) {
                                            view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
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
                if (onBackPress || !inGame){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = PrimaryMain)
                            .clickable { }
                            .padding(horizontal = 30.dp),
                        contentAlignment = Alignment.Center
                    ){
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
                                titleText = "Do you really want to exit ?"
                            )
                        }
                    }
                }
                if (requestDialogState){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = PrimaryMain)
                            .clickable { }
                            .padding(horizontal = 30.dp),
                        contentAlignment = Alignment.Center
                    ){
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            GameDialogue.PlayRequestBox {
                                viewModel.onEvent(GameUsecase.OnCancelPlayRequest)
                                viewModel.onEvent(GameUsecase.OnClearGameBoard)
                                findNavController().navigateUp()
                            }
                        }
                    }
                }
                if (acceptDialogState){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = PrimaryMain)
                            .clickable { }
                            .padding(horizontal = 30.dp),
                        contentAlignment = Alignment.Center
                    ){
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
                                buttonText = "Yes"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getWinnerName(playerTurns: Boolean): String {
        return if(gameMode == GameMode.AI && !playerTurns) "You Win"
        else if (gameMode == GameMode.FRIEND && !playerTurns) "You Win"
        else if (playerTurns) "Player 1" else "Player 2"
    }

    private fun calculateCellList() : List<Int>{
        return when(boardType){
            BoardType.THREEX3 -> (1..9).toList()
            BoardType.FOURX4 -> (1..25).toList()
            BoardType.FIVEX5 -> (1..36).toList()
        }
    }

    private fun calculateBoardColumnCount() : Int {
        return when(boardType){
            BoardType.THREEX3 -> 3
            BoardType.FOURX4 -> 5
            BoardType.FIVEX5 -> 6
        }
    }

    private fun calculateBoardHeight() : Dp {
        return when(boardType){
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