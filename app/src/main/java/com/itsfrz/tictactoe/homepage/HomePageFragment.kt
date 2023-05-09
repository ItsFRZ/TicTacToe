package com.itsfrz.tictactoe.homepage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.common.components.CustomCircleIconButton
import com.itsfrz.tictactoe.common.components.CustomOutlinedButton
import com.itsfrz.tictactoe.homepage.viewmodel.HomePageViewModel
import com.itsfrz.tictactoe.homepage.viewmodel.HomePageViewModelFactory
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.headerTitle
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.functionality.ShareInfo
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.functionality.NavOptions
import com.itsfrz.tictactoe.goonline.data.firebase.FirebaseDB
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.IGameStoreRepository
import com.itsfrz.tictactoe.homepage.usecase.HomePageUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomePageFragment : Fragment() {
    private lateinit var viewModel: HomePageViewModel
    private lateinit var cloudRepository: CloudRepository
    private lateinit var dataStoreRepository  : GameStoreRepository
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpOnlineConfig()
        val viewModelFactory = HomePageViewModelFactory(cloudRepository,dataStoreRepository)
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[HomePageViewModel::class.java]
        viewModel.onEvent(HomePageUseCase.OnCopyUserIdEvent)
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

    @SuppressLint("ServiceCast")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val gameBundle = bundleOf()
                val userId = viewModel.userId.value
                val scope = rememberCoroutineScope()
                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(color = PrimaryLight),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth())
                    Text(
                        style = headerTitle.copy(color = ThemeBlue),
                        text = "Choose Your Play Mode",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier
                        .height(78.dp)
                        .fillMaxWidth())
                    CustomOutlinedButton(
                        buttonClick = {
                            gameBundle.putSerializable(BundleKey.GAME_MODE,GameMode.AI)
                            findNavController().navigate(
                                R.id.selectBoard,gameBundle,
                                navOptions = NavOptions.navOptionStack
                            )
                        },
                        buttonText = "AI"
                    )
                    Spacer(modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth())
                    CustomOutlinedButton(
                        buttonClick = {
                            findNavController().navigate(
                                R.id.onlineModeFragment,null,
                                navOptions = NavOptions.navOptionStack
                            )
                        },
                        buttonText = "Online"
                    )
                    Spacer(modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth())
                    CustomOutlinedButton(
                        buttonClick = {
                            gameBundle.putSerializable(BundleKey.GAME_MODE,GameMode.TWO_PLAYER)
                            findNavController().navigate(
                                resId = R.id.selectBoard,
                                args = gameBundle,
                                navOptions = NavOptions.navOptionStack
                            )
                        },
                        buttonText = "2 Player"
                    )
                    Spacer(modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth())
                    CustomCircleIconButton(iconButtonClick = { /*TODO*/ }, buttonIcon = R.drawable.ic_stats)
                    Spacer(modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 80.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CustomCircleIconButton(iconButtonClick = {
                            scope.launch(Dispatchers.Main) {
                                async {  viewModel.onEvent(HomePageUseCase.OnCopyUserIdEvent) }.await()
                                val message = "${ShareInfo.SHARE_HEADER}\n${ShareInfo.SHARE_TITLE}\n${ShareInfo.SHARE_SUBTITLE}\n\nUserId : ✄-x${userId}x-✄"
                                val intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT,message)
                                }
                                context.startActivity(Intent.createChooser(intent,"Share"))
                            }
                        }, buttonIcon = R.drawable.ic_share)
                        CustomCircleIconButton(iconButtonClick = { /*TODO*/ }, buttonIcon = R.drawable.ic_settings)
                    }
                }
            }
        }
    }

}