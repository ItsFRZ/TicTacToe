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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.GameDialogue
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.GameResult
import com.itsfrz.tictactoe.friend.usecase.FriendPageUseCase
import com.itsfrz.tictactoe.game.presentation.components.GameBoard
import com.itsfrz.tictactoe.game.presentation.components.GameDivider
import com.itsfrz.tictactoe.game.presentation.components.UserMove
import com.itsfrz.tictactoe.game.domain.usecase.GameUsecase
import com.itsfrz.tictactoe.game.presentation.viewmodel.GameViewModel
import com.itsfrz.tictactoe.game.presentation.viewmodel.GameViewModelFactory
import com.itsfrz.tictactoe.goonline.data.firebase.FirebaseDB
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.IGameStoreRepository
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.ThemeBlueLight
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class GameFragment : Fragment(){

    private var job : Job? = null
    private lateinit var viewModel: GameViewModel
    private lateinit var gameMode : GameMode
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
        val viewModelFactory = GameViewModelFactory(cloudRepository,dataStoreRepository)
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[GameViewModel::class.java]
        gameMode = requireArguments().getSerializable("GameMode") as GameMode
        Log.i("GAME_MODE", "onCreate: Game Mode ${gameMode}")
        viewModel.setGameMode(gameMode)
        viewModel.setAITurn()
        setUpNavArgs()

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


    private fun setUpNavArgs() {
        val friendUserId = requireArguments().getString("friendId")
        friendUserId?.let {
            if (it.isNotEmpty())
                viewModel.onEvent(GameUsecase.UpdateFriendUserId(it))
        }
        val userId = requireArguments().getString("userId")
        userId?.let {
            if (it.isNotEmpty())
                viewModel.onEvent(GameUsecase.UpdateUserId(it))
        }
        val gameSessionId = requireArguments().getString("sessionId")
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
                    .background(color = PrimaryLight),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp))
                    Row(modifier = Modifier
                        .padding(horizontal = 30.dp)
                        .height(30.dp)
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(modifier = Modifier.size(userTimeOutPulsatingWarning.dp), painter = painterResource(id = R.drawable.ic_timer), contentDescription = "Game Timer", tint = ThemeBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(modifier = Modifier
                            .height(6.dp)
                            .fillMaxWidth(),progress = timeLimitAnimation, color = ThemeBlue, backgroundColor = ThemeBlueLight)
                    }
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp))
                    GameDivider()
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp))
                    GameBoard(
                        crossList = if (gameMode == GameMode.FRIEND) playerTwoData else playerOneData,
                        rightList = if (gameMode == GameMode.FRIEND) playerOneData else playerTwoData,
                        userId = userId,
                        currentUserId = currentUserId,
                        gameMode = gameMode,
                        winnerIndexList = winnerIndexList,
                        isWinner = gameResult != GameResult.NONE,
                        isPlayerMoved = !playerTurns,
                        onMove = { index -> viewModel.onEvent(GameUsecase.OnUserTick(index)) },
                        onAIMove = {
                            viewModel.onEvent(GameUsecase.OnAIMove)
                        }
                    )
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp))
                    GameDivider()
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp))
                    if (gameMode == GameMode.FRIEND){
                        UserMove(username = if (currentUserId == userId) "Your " else "Opponent's Turn", isCross = currentUserId == friendUserId)
                    }else{
                        UserMove(username = if (playerTurns) "Player 2" else "Player 1", isCross = playerTurns)
                    }
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
                            .background(color = PrimaryLight)
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
                                        winnerUsername = if (playerTurns) "Player 1" else "Player 2",
                                        dialogueButtonText = "Play Again",
                                        onCloseEvent = {
                                            viewModel.onEvent(GameUsecase.GameExitEvent)
                                            findNavController().navigateUp()
                                        }
                                    ) {
                                        view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                        viewModel.onEvent(GameUsecase.OnGameRetry)
                                    }
                                }
                                GameResult.DRAW,GameResult.LOSE -> {
                                    if (!acceptDialogState){
                                        GameDialogue.GameDrawLoseDialogue(
                                            gameResult = gameResult,
                                            onCloseEvent = {
                                                viewModel.onEvent(GameUsecase.GameExitEvent)
                                                findNavController().navigateUp()
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
                            .background(color = PrimaryLight)
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
                                    findNavController().navigateUp()
                                    viewModel.onEvent(GameUsecase.GameExitEvent)
                                    viewModel.onEvent(GameUsecase.OnBackPress(false))
                                },
                                onContinueEvent = {
                                    viewModel.onEvent(GameUsecase.OnBackPress(false))
                                },
                                titleText = if (!inGame) "Opponent Left The Game\nDo you want to exit ?" else "Do you really want to exit ?"
                            )
                        }
                    }
                }
                if (requestDialogState){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = PrimaryLight)
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
                            .background(color = PrimaryLight)
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
                                    findNavController().navigateUp()
                                    viewModel.onEvent(GameUsecase.OnCancelPlayRequest)
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

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.onEvent(GameUsecase.GameExitEvent)
        job?.cancel()
    }

}