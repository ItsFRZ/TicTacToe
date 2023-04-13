package com.itsfrz.tictactoe.friend

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.fragment.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.GameDialogue
import com.itsfrz.tictactoe.common.components.UserItemLayout
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.friend.components.FriendSearchBar
import com.itsfrz.tictactoe.friend.usecase.FriendPageUseCase
import com.itsfrz.tictactoe.friend.viewmodel.FriendPageViewModel
import com.itsfrz.tictactoe.friend.viewmodel.FriendPageViewModelFactory
import com.itsfrz.tictactoe.goonline.data.firebase.FirebaseDB
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.IGameStoreRepository
import com.itsfrz.tictactoe.homepage.viewmodel.HomePageViewModel
import com.itsfrz.tictactoe.homepage.viewmodel.HomePageViewModelFactory
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.headerTitle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class FriendFragment : Fragment() {
    private val TAG = "FRIEND_FRAG"
    private var job : Job? = null
    private lateinit var viewModel: FriendPageViewModel
    private lateinit var cloudRepository: CloudRepository
    private lateinit var dataStoreRepository  : GameStoreRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpOnlineConfig()
        val viewModelFactory = FriendPageViewModelFactory(cloudRepository,dataStoreRepository)
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[FriendPageViewModel::class.java]
        setupGameEngine()

    }


    fun setupGameEngine(){
        job = CoroutineScope(Dispatchers.IO).launch {
            dataStoreRepository.fetchPreference().collectLatest {
                viewModel.setupUserDetail(it.userProfile)
                viewModel.updateFriendList(it.playGround?.friendList)
                viewModel.updateActiveRequestList(it.playGround?.activeRequest)
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
        return ComposeView(requireActivity()).apply {
            setContent {
                val userId = viewModel.usernameSearchId.value
                val friendList = viewModel.friendList.value
                val requestList = viewModel.playRequestList.value
                val isLoaderActive = viewModel.loaderState.value
                val playRequestLoader = viewModel.playRequestLoader.value
                val inGame = viewModel.inGameState.value
                if (inGame){
                    val gameBundle = bundleOf()
                    gameBundle.putSerializable("GameMode", GameMode.FRIEND)
                    gameBundle.putString("userId",viewModel.userId.value)
                    gameBundle.putString("friendId",viewModel.friendRequestId.value)
                    gameBundle.putString("sessionId",viewModel.gameSessionId.value)
                    findNavController().navigate(R.id.gameFragment,gameBundle)
                    viewModel.onEvent(FriendPageUseCase.OnRequestLoaderVisibilityToggle(false))
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = PrimaryLight)
                            .alpha(if (isLoaderActive || playRequestLoader) 0.3F else 1F)
                    ) {
                        FriendSearchBar(
                            username = userId,
                            onUserNameChange = {
                                viewModel.onEvent(FriendPageUseCase.OnUserIdChange(it))
                            },
                            onAddEvent = {viewModel.onEvent(FriendPageUseCase.SearchUserEvent)}
                        )
                        Spacer(modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .fillMaxWidth()
                            .height(0.6.dp)
                            .background(color = ThemeBlue)
                        )
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 15.dp, vertical = 10.dp)
                                .fillMaxWidth(),
                            text = "Friend List",
                            style = headerTitle.copy(fontSize = 15.sp, color = ThemeBlue, textAlign = TextAlign.Start, fontWeight = FontWeight.SemiBold)
                        )
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(modifier = Modifier.weight(1F)){
                                itemsIndexed(
                                    friendList
                                ){ index,item ->
                                    UserItemLayout(
                                        username = item.username,
                                        isUserOnline = item.online,
                                        onItemEvent = {
                                            viewModel.onEvent(FriendPageUseCase.OnRequestFriendEvent(index))
                                        },
                                        onAcceptEvent = { /*TODO*/ })
                                }

                            }
                            Spacer(modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .fillMaxWidth()
                                .height(0.6.dp)
                                .background(color = ThemeBlue)
                            )
                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 15.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                text = "Play Request's",
                                style = headerTitle.copy(fontSize = 15.sp, color = ThemeBlue, textAlign = TextAlign.Start, fontWeight = FontWeight.SemiBold)
                            )
                            LazyColumn(modifier = Modifier.weight(1F)){
                                itemsIndexed(requestList) { index, item ->
                                    UserItemLayout(
                                        username = item.requesterUsername,
                                        isUserOnline = item.online,
                                        isFriendList = false,
                                        onItemEvent = { /*TODO*/ },
                                        onAcceptEvent = {
                                            viewModel.onEvent(FriendPageUseCase.OnAcceptFriendRequestEvent(index))
                                        })
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
                            GameDialogue.PlayRequestBox{
                                viewModel.onEvent(FriendPageUseCase.OnCancelPlayRequest)
                            }
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
        job?.cancel()
    }

}