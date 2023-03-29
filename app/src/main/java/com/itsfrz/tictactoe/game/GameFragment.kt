package com.itsfrz.tictactoe.game

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgs
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.GameDialogue
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.GameResult
import com.itsfrz.tictactoe.game.components.GameBoard
import com.itsfrz.tictactoe.game.components.GameDivider
import com.itsfrz.tictactoe.game.components.UserMove
import com.itsfrz.tictactoe.game.usecase.GameUsecase
import com.itsfrz.tictactoe.game.viewmodel.GameViewModel
import com.itsfrz.tictactoe.game.viewmodel.GameViewModelFactory
import com.itsfrz.tictactoe.homepage.HomePageFragment
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.ThemeBlueLight
import java.io.Serializable

class GameFragment : Fragment(){

    private lateinit var viewModel: GameViewModel
    private lateinit var gameMode : GameMode

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
        val viewModelFactory = GameViewModelFactory()
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[GameViewModel::class.java]
        gameMode = requireArguments().getSerializable("GameMode") as GameMode
        Log.i("GAME_MODE", "onCreate: Game Mode ${gameMode}")
        viewModel.setGameMode(gameMode)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {

                val playerOneData = viewModel.playerOneIndex.value
                val playerTwoData = viewModel.playerTwoIndex.value
                val playerTurns = viewModel.isUserTurnsComplete.value
                val userTimeLimit = viewModel.userTimer.value
                val timeLimitAnimation by animateFloatAsState(
                    targetValue = userTimeLimit,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                )
                val gameResult = viewModel.gameResult.value
                val onBackPress = viewModel.onBackPress.value
                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(color = PrimaryLight),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp))
                    Row(modifier = Modifier
                        .padding(horizontal = 30.dp)
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_timer), contentDescription = "Game Timer", tint = ThemeBlue)
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
                        .height(80.dp))
                    GameBoard(
                        crossList = playerOneData,
                        rightList = playerTwoData,
                        gameMode = gameMode,
                        isPlayerMoved = !playerTurns,
                        onMove = { index ->
                        viewModel.onEvent(GameUsecase.OnUserTick(index))
                    })
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp))
                    GameDivider()
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp))
                    UserMove(username = if (playerTurns) "Player 2" else "Player 1", isCross = playerTurns)
                }
                if (gameResult != GameResult.NONE){
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
                                            findNavController().navigateUp()
                                        }
                                    ) {
                                        view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                        viewModel.onEvent(GameUsecase.OnGameRetry)
//                                Toast.makeText(requireContext(), "${if (playerTurns) "Player 2" else "Player 1"} has Won \t\uD83E\uDD29", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                GameResult.DRAW,GameResult.LOSE -> {
                                    GameDialogue.GameDrawLoseDialogue(
                                        gameResult = gameResult,
                                        onCloseEvent = {
                                            findNavController().navigateUp()
                                        }
                                    ) {
                                        view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                        viewModel.onEvent(GameUsecase.OnGameRetry)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
                if (onBackPress){
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
                            GameDialogue.GameExitDialogue(
                                onExitEvent = { findNavController().navigateUp() },
                                onContinueEvent = {
                                    viewModel.onEvent(GameUsecase.OnBackPress(false))
                                }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}