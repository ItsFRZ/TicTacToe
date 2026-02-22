package com.itsfrz.tictactoe.online

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.CustomOutlinedButton
import com.itsfrz.tictactoe.common.components.GameDialogue
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameLevel
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.functionality.GameSound
import com.itsfrz.tictactoe.common.functionality.InternetHelper
import com.itsfrz.tictactoe.common.functionality.NavOptions
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.IGameStoreRepository
import com.itsfrz.tictactoe.online.usecase.OnlineModeUseCase
import com.itsfrz.tictactoe.online.viewmodel.OnlineModeViewModel
import com.itsfrz.tictactoe.online.viewmodel.OnlineModeViewModelFactory
import com.itsfrz.tictactoe.ui.theme.LightWhite
import com.itsfrz.tictactoe.ui.theme.headerSubTitle
import com.itsfrz.tictactoe.ui.theme.headerTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OnlineModeFragment : Fragment() {
    private lateinit var viewmodel: OnlineModeViewModel
    private lateinit var commonViewModel: CommonViewModel
    private lateinit var cloudRepository: CloudRepository
    private lateinit var dataStoreRepository: GameStoreRepository
    private lateinit var gameSound: GameSound
    private var job: Job? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpOnlineConfig()
        val viewModelFactory = OnlineModeViewModelFactory(cloudRepository, dataStoreRepository)
        viewmodel =
            ViewModelProvider(viewModelStore, viewModelFactory)[OnlineModeViewModel::class.java]
        commonViewModel = CommonViewModel.getInstance()
        gameSound = commonViewModel.gameSound
        setupGameEngine()
    }

    fun setupGameEngine() {
        job = CoroutineScope(Dispatchers.IO).launch {
            dataStoreRepository.fetchPreference().collectLatest { gameStore ->
                viewmodel.userId.value.let {
                    if (it.isNotEmpty()) {
                        cloudRepository.fetchPlaygroundInfoAndStore(it)
                    }
                }
                viewmodel.setupUserDetail(gameStore.userProfile)
                gameStore.playGround?.let {
                    viewmodel.onEvent(OnlineModeUseCase.OnUpdateUserInGameInfo(it.inGame))
                }
                gameStore.boardState?.let { boardState ->
                    boardState.playerOneState?.let { playerOne ->
                        boardState.playerTwoState?.let { playerTwo ->

                            if (boardState.currentUserTurnId == playerOne.userId && playerOne.userId == viewmodel.userId.value) {
                                viewmodel.onEvent(OnlineModeUseCase.UpdateFriendUserId(playerTwo.userId))
                            } else {
                                viewmodel.onEvent(OnlineModeUseCase.UpdateFriendUserId(playerOne.userId))
                            }
                            viewmodel.onEvent(OnlineModeUseCase.OnUpdateGameSessionId(boardState.currentUserTurnId))

                        }
                    }
                }
            }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val playerSearchState = viewmodel.playerSearchState.value
                val inGameState = viewmodel.inGameState.value
                val playerOneID = viewmodel.userId.value
                val playerTwoID = viewmodel.friendRequestId.value
                if (inGameState && playerOneID.isNotEmpty() && playerTwoID.isNotEmpty()) {
                    val gameBundle = bundleOf()
                    gameBundle.putSerializable(BundleKey.GAME_MODE, GameMode.FRIEND)
                    gameBundle.putSerializable(BundleKey.BOARD_TYPE, BoardType.THREEX3)
                    gameBundle.putSerializable(BundleKey.SELECTED_LEVEL, GameLevel.NONE)
                    gameBundle.putString(BundleKey.USER_ID, viewmodel.userId.value)
                    gameBundle.putString(BundleKey.FRIEND_ID, viewmodel.friendRequestId.value)
                    gameBundle.putString(BundleKey.SESSION_ID, viewmodel.gameSessionId.value)
                    findNavController().navigate(R.id.gameFragment, gameBundle)
                    viewmodel.onEvent(OnlineModeUseCase.OnRandomPlayerSearch(false))
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = ThemePicker.primaryColor.value),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth()
                        )
                        Text(
                            style = headerTitle.copy(color = Color.White),
                            text = buildAnnotatedString {
                                append("Online")
                                withStyle(style = SpanStyle(color = ThemePicker.secondaryColor.value)) {
                                    append(" Mode")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )
                        Spacer(
                            modifier = Modifier
                                .height(10.dp)
                                .fillMaxWidth()
                        )
                        Text(
                            style = headerSubTitle.copy(color = LightWhite),
                            text = "Play with random person\n--- OR ---\nEnter your friend’s id to play.",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )
                        Spacer(
                            modifier = Modifier
                                .height(78.dp)
                                .fillMaxWidth()
                        )
                        CustomOutlinedButton(
                            buttonClick = {
                                if (InternetHelper.isOnline(requireContext())) {
                                    viewmodel.onEvent(OnlineModeUseCase.OnRandomPlayerSearch(true))
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.internet_not_available),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            buttonText = "Random"
                        )
                        Spacer(
                            modifier = Modifier
                                .height(20.dp)
                                .fillMaxWidth()
                        )
                        CustomOutlinedButton(
                            buttonClick = {
                                gameSound.clickSound()
                                findNavController()
                                    .navigate(
                                        resId = R.id.friendFragment,
                                        args = null,
                                        navOptions = NavOptions.navOptionStack
                                    )
                            },
                            buttonText = "Friend"
                        )
                    }
                    if (playerSearchState) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            GameDialogue.PlayRequestBox(
                                commonViewModel = commonViewModel,
                                onCloseClick = {
                                    viewmodel.onEvent(OnlineModeUseCase.OnRandomPlayerSearch(false))
                                    viewmodel.onEvent(OnlineModeUseCase.OnRemoveRandomModeConfig)
                                })
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        commonViewModel.updateOnlineStatus(isOnline = InternetHelper.isOnline(requireContext()))
    }

}