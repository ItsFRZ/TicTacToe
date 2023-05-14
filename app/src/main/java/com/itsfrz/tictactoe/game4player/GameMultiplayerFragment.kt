package com.itsfrz.tictactoe.game4player

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameLevel
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.game.domain.usecase.GameUsecase
import com.itsfrz.tictactoe.game4player.domain.usecase.GameMultiplayerUseCase
import com.itsfrz.tictactoe.game4player.presentation.viewmodel.GameMultiplayerViewModel
import com.itsfrz.tictactoe.game4player.presentation.viewmodel.GameMultiplayerViewModelFactory
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import kotlinx.coroutines.Job

class GameMultiplayerFragment : Fragment() {

    private var job : Job? = null
    private lateinit var viewModel: GameMultiplayerViewModel
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
                    viewModel.onEvent(GameMultiplayerUseCase.OnBackPress(true))
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameMode = requireArguments().getSerializable(BundleKey.GAME_MODE) as GameMode
        gameLevel = requireArguments().getSerializable(BundleKey.SELECTED_LEVEL) as GameLevel
        boardType = requireArguments().getSerializable(BundleKey.BOARD_TYPE) as BoardType
        Log.i("GAME_MODE", "onCreate: Game Mode ${gameMode}, Selected Level ${gameLevel}, Board Type ${boardType}")
        val viewModelFactory = GameMultiplayerViewModelFactory()
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[GameMultiplayerViewModel::class.java]
        commonViewModel = CommonViewModel.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {


            }
        }
    }
}