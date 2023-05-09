package com.itsfrz.tictactoe.board

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.board.components.BoardTypeComponent
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.board.components.SelectedBoardIndicator
import com.itsfrz.tictactoe.board.usecase.SelectBoardUseCase
import com.itsfrz.tictactoe.common.components.CustomButton
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.functionality.NavOptions
import com.itsfrz.tictactoe.online.viewmodel.BoardViewModel
import com.itsfrz.tictactoe.online.viewmodel.BoardViewModelFactory
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.headerTitle

class SelectBoardFragment : Fragment() {

    private lateinit var viewmodel : BoardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelFactory = BoardViewModelFactory()
        viewmodel = ViewModelProvider(this,viewModelFactory)[BoardViewModel::class.java]
        val gameMode = requireArguments().getSerializable(BundleKey.GAME_MODE) as GameMode
        viewmodel.onEvent(SelectBoardUseCase.OnGameModeEvent(gameMode))
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val selectedIndex = viewmodel.selectedIndex.value
                val selectedLevel = viewmodel.level.value
                val boardType = viewmodel.boardType.value
                val gameMode = viewmodel.gameMode.value
                val listState = rememberLazyListState()
                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(color = PrimaryLight),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(25.dp))
                    Text(modifier = Modifier.fillMaxWidth(), text = "Choose Board", style = headerTitle.copy(color = ThemeBlue))
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp))
                    BoxWithConstraints {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.Center,
                            state = listState,
                            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
                        ){
                            item {
                                BoardTypeComponent(
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp, vertical = 15.dp)
                                        .fillParentMaxWidth(.88F)
                                        .clip(RoundedCornerShape(10.dp))
                                        .height(320.dp),
                                    boardLevelText = "Rookie",
                                    boardSizeText = "3x3",
                                    boardTypeVisual = R.drawable.ic_board_size_3,
                                    selectedIndex = isIndexSelected(selectedIndex,boardType,BoardType.THREEX3),
                                    onDifficultyEvent = { capsuleIndex -> viewmodel.onEvent(SelectBoardUseCase.OnBoardInfoEvent(Pair(BoardType.THREEX3,capsuleIndex))) },
                                    isAIMode = gameMode == GameMode.AI,
                                    gameBoardContentText = "Good to start with!"
                                )
                                Spacer(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp))
                            }
                            item {
                                BoardTypeComponent(
                                    modifier = Modifier
                                        .padding(horizontal = 40.dp, vertical = 15.dp)
                                        .fillParentMaxWidth(.88F)
                                        .clip(RoundedCornerShape(10.dp))
                                        .height(320.dp),
                                    boardLevelText = "Seasoned",
                                    boardSizeText = "4x4",
                                    boardTypeVisual = R.drawable.ic_board_size_4,
                                    selectedIndex = isIndexSelected(selectedIndex,boardType,BoardType.FOURX4),
                                    onDifficultyEvent = { capsuleIndex -> viewmodel.onEvent(SelectBoardUseCase.OnBoardInfoEvent(Pair(BoardType.FOURX4,capsuleIndex))) },
                                    isAIMode = false,
                                    gameBoardContentText = "Players with high IQ chooses this board!"
                                )
                                Spacer(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp))
                            }
                            item {
                                BoardTypeComponent(
                                    modifier = Modifier
                                        .padding(horizontal = 30.dp, vertical = 15.dp)
                                        .fillParentMaxWidth(.88F)
                                        .clip(RoundedCornerShape(10.dp))
                                        .height(320.dp),
                                    boardLevelText = "Pro",
                                    boardSizeText = "5x5",
                                    boardTypeVisual = R.drawable.ic_board_size_5,
                                    selectedIndex = isIndexSelected(selectedIndex,boardType,BoardType.FIVEX5),
                                    onDifficultyEvent = {capsuleIndex -> viewmodel.onEvent(SelectBoardUseCase.OnBoardInfoEvent(Pair(BoardType.FIVEX5,capsuleIndex))) },
                                    isAIMode = false,
                                    gameBoardContentText = "Hokage like minded people chooses this board!"
                                )
                                Spacer(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp))
                            }
                        }
                    }
                    SelectedBoardIndicator(listState.firstVisibleItemIndex){
                        viewmodel.onEvent(SelectBoardUseCase.OnBoardTypeChange(it))
                    }
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp))
                    CustomButton(
                        isButtonEnabled = if (gameMode == GameMode.AI && boardType == BoardType.THREEX3) selectedIndex != -1 else true,
                        onButtonClick = {
                            val bundle = bundleOf()
                            bundle.putSerializable(BundleKey.GAME_MODE,gameMode)
                            bundle.putSerializable(BundleKey.SELECTED_LEVEL,selectedLevel)
                            bundle.putSerializable(BundleKey.BOARD_TYPE,boardType)
                            findNavController().navigate(
                                resId = R.id.gameFragment,
                                args = bundle,
                                navOptions = NavOptions.navOptionStack
                            )
                        }
                    )
                }
            }
        }
    }

    private fun calculateIndexFromOffset(offset: Int): Int {
        return if (offset <= 500) 0 else if(offset in 501..600) 1 else 2
    }

    private fun isIndexSelected(selectedIndex: Int,selectedBoardType : BoardType,currentBoardType : BoardType): Int {
        return if (selectedBoardType == currentBoardType) selectedIndex else -1;
    }

}