package com.itsfrz.tictactoe.friend

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.GameDialogue
import com.itsfrz.tictactoe.common.components.Separator
import com.itsfrz.tictactoe.common.components.UserItemLayout
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameLevel
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.functionality.GameSound
import com.itsfrz.tictactoe.common.functionality.InternetHelper
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.friend.components.FriendSearchBar
import com.itsfrz.tictactoe.friend.usecase.FriendPageUseCase
import com.itsfrz.tictactoe.friend.viewmodel.FriendPageViewModel
import com.itsfrz.tictactoe.friend.viewmodel.FriendPageViewModelFactory
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.IGameStoreRepository
import com.itsfrz.tictactoe.ui.theme.headerTitle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class FriendFragment : Fragment() {
    private val TAG = "FRIEND_FRAG"
    private var job : Job? = null
    private lateinit var viewModel: FriendPageViewModel
    private lateinit var cloudRepository: CloudRepository
    private lateinit var dataStoreRepository  : GameStoreRepository
    private lateinit var commonViewModel: CommonViewModel
    private lateinit var gameSound: GameSound

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpOnlineConfig()
        val viewModelFactory = FriendPageViewModelFactory(cloudRepository,dataStoreRepository)
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[FriendPageViewModel::class.java]
        commonViewModel = CommonViewModel.getInstance()
        gameSound = commonViewModel.gameSound
        setupGameEngine()
        viewModel.clearGameSession()

    }


    fun setupGameEngine(){
        job = CoroutineScope(Dispatchers.IO).launch {
            dataStoreRepository.fetchPreference().collectLatest {
                viewModel.userId.value.let { if (it.isNotEmpty()) { cloudRepository.fetchPlaygroundInfoAndStore(it) } }
                viewModel.setupUserDetail(it.userProfile)
                viewModel.updateFriendList(it.playGround?.friendList)
                viewModel.updateActiveRequestList(it.playGround?.activeRequest)
                it.playGround?.let { viewModel.onEvent(FriendPageUseCase.OnUpdateUserInGameInfo(it.inGame)) }
            }
        }
    }

    private fun setUpOnlineConfig() {
        val gameStore =  GameDataStore.getDataStore(requireContext())
        dataStoreRepository = IGameStoreRepository(gameStore)
        cloudRepository = CloudRepository(
            dataStoreRepository = dataStoreRepository,
            scope = CoroutineScope(Dispatchers.IO)
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val userId = viewModel.usernameSearchId.value
                val friendList = viewModel.friendList.value
                val requestList = viewModel.playRequestList.value
                val isLoaderActive = viewModel.loaderState.value
                val playRequestLoader = viewModel.playRequestLoader.value
                if (viewModel.inGameState.value){
                    val gameBundle = bundleOf()
                    gameBundle.putSerializable(BundleKey.GAME_MODE, GameMode.FRIEND)
                    gameBundle.putSerializable(BundleKey.BOARD_TYPE, BoardType.THREEX3)
                    gameBundle.putSerializable(BundleKey.SELECTED_LEVEL,GameLevel.NONE)
                    gameBundle.putString(BundleKey.USER_ID,viewModel.userId.value)
                    gameBundle.putString(BundleKey.FRIEND_ID,viewModel.friendRequestId.value)
                    gameBundle.putString(BundleKey.SESSION_ID,viewModel.gameSessionId.value)
                    findNavController().navigate(R.id.gameFragment,gameBundle)
                    viewModel.onEvent(FriendPageUseCase.OnRequestLoaderVisibilityToggle(false))
                    viewModel.onEvent(FriendPageUseCase.OnUpdateUserInGameInfo(false))
                    viewModel.onEvent(FriendPageUseCase.OnCancelPlayRequest)
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = ThemePicker.primaryColor.value)
                            .alpha(if (isLoaderActive || playRequestLoader) 0.3F else 1F)
                    ) {
                        FriendSearchBar(
                            username = userId,
                            onUserNameChange = {
                                viewModel.onEvent(FriendPageUseCase.OnUserIdChange(it))
                            },
                            onAddEvent = {
                                gameSound.clickSound()
                                if (InternetHelper.isOnline(requireContext())){
                                    viewModel.onEvent(FriendPageUseCase.SearchUserEvent)
                                }else{
                                    Toast.makeText(requireContext(), getString(R.string.internet_not_available), Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        Separator(ThemePicker.secondaryColor.value)
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 15.dp, vertical = 10.dp)
                                .fillMaxWidth(),
                            text = "Friend List",
                            style = headerTitle.copy(fontSize = 15.sp, color = ThemePicker.secondaryColor.value, textAlign = TextAlign.Start, fontWeight = FontWeight.SemiBold)
                        )
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.weight(1F),
                            ){
                                itemsIndexed(
                                    items = friendList,
                                    key = { index, item -> item.userId.hashCode()+index }
                                ){ index,item ->
                                    UserItemLayout(
                                        modifier = Modifier.animateItemPlacement(
                                            animationSpec = tween(durationMillis = 600)
                                        ),
                                        username = item.username,
                                        isUserOnline = item.online,
                                        playRequestEvent = {
                                            if (InternetHelper.isOnline(requireContext())){
                                                viewModel.onEvent(FriendPageUseCase.OnRequestFriendEvent(index))
                                            }else{
                                                Toast.makeText(requireContext(), getString(R.string.internet_not_available), Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        acceptRequestEvent = {
                                            if (InternetHelper.isOnline(requireContext())){
                                                viewModel.onEvent(FriendPageUseCase.OnAcceptFriendRequestEvent(index))
                                            }else{
                                                Toast.makeText(requireContext(), getString(R.string.internet_not_available), Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        isRequested = item.playRequest
                                    )
                                }
                            }
                        }
                    }
                    if (isLoaderActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            GameDialogue.CommonLoadingScreen()
                        }
                    }

                    if (playRequestLoader){
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            GameDialogue.PlayRequestBox(commonViewModel = commonViewModel, onCloseClick = {
                                viewModel.onEvent(FriendPageUseCase.OnCancelPlayRequest)
                            })
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        job?.cancel()
        commonViewModel.updateOnlineStatus(InternetHelper.isOnline(requireContext()))
        setupGameEngine()
    }
    override fun onStop() {
        super.onStop()
        commonViewModel.updateOnlineStatus(isOnline = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }

}