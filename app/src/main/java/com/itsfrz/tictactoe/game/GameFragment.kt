package com.itsfrz.tictactoe.game

import android.content.Context.VIBRATOR_SERVICE
import android.os.Bundle
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.GameWinDialogue
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.game.components.GameBoard
import com.itsfrz.tictactoe.game.components.GameDivider
import com.itsfrz.tictactoe.game.components.UserMove
import com.itsfrz.tictactoe.game.usecase.GameUsecase
import com.itsfrz.tictactoe.game.viewmodel.GameViewModel
import com.itsfrz.tictactoe.game.viewmodel.GameViewModelFactory
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.ThemeBlueLight

class GameFragment : Fragment() {

    private lateinit var viewModel: GameViewModel
    private lateinit var gameMode : GameMode
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelFactory = GameViewModelFactory()
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[GameViewModel::class.java]
        gameMode = GameMode.TWO_PLAYER
        viewModel.setGameMode(GameMode.TWO_PLAYER)
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
                    targetValue =userTimeLimit,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                )
                val isWinner = viewModel.isWinner.value

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
//                    val crossList = listOf<Int>(0,2,5)
//                    val rightList = listOf<Int>(1,4,7)
                    GameBoard(crossList = playerOneData, rightList = playerTwoData, onMove = { index ->
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
                if (isWinner){
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
                            GameWinDialogue(winnerUsername = if (playerTurns) "Player 1" else "Player 2") {
                                view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                viewModel.onEvent(GameUsecase.OnGameRetry)
//                                Toast.makeText(requireContext(), "${if (playerTurns) "Player 2" else "Player 1"} has Won \t\uD83E\uDD29", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}