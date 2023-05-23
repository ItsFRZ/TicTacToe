package com.itsfrz.tictactoe.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.common.components.CustomCircleIconButton
import com.itsfrz.tictactoe.common.components.CustomOutlinedButton
import com.itsfrz.tictactoe.home.viewmodel.HomePageViewModel
import com.itsfrz.tictactoe.home.viewmodel.HomePageViewModelFactory
import com.itsfrz.tictactoe.ui.theme.headerTitle
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.GameDialogue
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.PlayerCount
import com.itsfrz.tictactoe.common.functionality.*
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.goonline.data.firebase.FirebaseDB
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.IGameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.ISettingRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingDataStore
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingRepository
import com.itsfrz.tictactoe.home.usecase.HomePageUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var backgroundPlayer : MediaPlayer
    private lateinit var popUpSoundPlayer : MediaPlayer
    private lateinit var pieceSoundClick1Player : MediaPlayer
    private lateinit var pieceSoundClick2Player : MediaPlayer
    private lateinit var clickSoundPlayer : MediaPlayer
    private lateinit var selectSoundPlayer : MediaPlayer
    private lateinit var starSoundPlayer : MediaPlayer
    private lateinit var gameWinSoundPlayer : MediaPlayer
    private lateinit var gameLossSoundPlayer : MediaPlayer
    private lateinit var coinAccumulateSoundPlayer : MediaPlayer
    private lateinit var gameSound: GameSound

    private lateinit var viewModel: HomePageViewModel
    private lateinit var cloudRepository: CloudRepository
    private lateinit var dataStoreRepository  : GameStoreRepository
    private lateinit var settingRepository: SettingRepository
    private lateinit var commonViewModel: CommonViewModel
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commonViewModel = CommonViewModel.getInstance()
        setupGameSound()
        setUpOnlineConfig()
        val viewModelFactory = HomePageViewModelFactory(cloudRepository,dataStoreRepository)
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[HomePageViewModel::class.java]
        viewModel.onEvent(HomePageUseCase.OnCopyUserIdEvent)
        viewModel.userId.value.let {
            commonViewModel.registerViewModel(dataStoreRepository,cloudRepository,settingRepository,gameSound,it)
        }
        CoroutineScope(Dispatchers.Default).launch {
            commonViewModel.loadEmojiData(requireContext())
        }
        CoroutineScope(Dispatchers.IO).launch {
            commonViewModel.loadUserPreference()
        }
    }

    private fun setupGameSound() {
        backgroundPlayer = MediaPlayer.create(requireContext(),R.raw.background_track)
        popUpSoundPlayer = MediaPlayer.create(requireContext(),R.raw.popup_sound)
        clickSoundPlayer = MediaPlayer.create(requireContext(),R.raw.button_click)
        selectSoundPlayer = MediaPlayer.create(requireContext(),R.raw.emoji_select_click)
        pieceSoundClick1Player = MediaPlayer.create(requireContext(),R.raw.piece_click_1)
        pieceSoundClick2Player = MediaPlayer.create(requireContext(),R.raw.piece_click_2)
        starSoundPlayer = MediaPlayer.create(requireContext(),R.raw.star_active)
        gameSound = GameSound(backgroundPlayer,popUpSoundPlayer,clickSoundPlayer,selectSoundPlayer,pieceSoundClick1Player,pieceSoundClick2Player,starSoundPlayer)
        gameSound.startBackgroundMusic()
        GameDialogue.setDialogSound(gameSound)
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
        val settingStore = SettingDataStore.getDataStore(requireContext())
        settingRepository = ISettingRepository(settingStore)
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
                    .background(color = ThemePicker.primaryColor.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth())
                    Text(
                        style = headerTitle.copy(color = Color.White),
                        text = buildAnnotatedString {
                            append("Choose Your")
                            withStyle(style = SpanStyle(color = ThemePicker.secondaryColor.value)){
                                append(" Play Mode")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth())
                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        item {
                            CustomOutlinedButton(
                                buttonClick = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    gameBundle.putSerializable(BundleKey.GAME_MODE,GameMode.AI)
                                    gameBundle.putSerializable(BundleKey.PLAYER_COUNT,PlayerCount.ONE)
                                    findNavController().navigate(
                                        R.id.emojiPickerFragment,gameBundle,
                                        navOptions = NavOptions.navOptionStack
                                    )
                                },
                                buttonText = "Robot"
                            )
                            Spacer(modifier = Modifier
                                .height(20.dp)
                                .fillMaxWidth())
                            CustomOutlinedButton(
                                buttonClick = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    gameBundle.putSerializable(BundleKey.GAME_MODE,GameMode.TWO_PLAYER)
                                    gameBundle.putSerializable(BundleKey.PLAYER_COUNT,PlayerCount.TWO)
                                    findNavController().navigate(
                                        resId = R.id.emojiPickerFragment,
                                        args = gameBundle,
                                        navOptions = NavOptions.navOptionStack
                                    )
                                },
                                buttonText = "2 Player"
                            )
                            Spacer(modifier = Modifier
                                .height(20.dp)
                                .fillMaxWidth())
                            CustomOutlinedButton(
                                buttonClick = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    gameBundle.putSerializable(BundleKey.GAME_MODE,GameMode.FOUR_PLAYER)
                                    gameBundle.putSerializable(BundleKey.PLAYER_COUNT,PlayerCount.FOUR)
                                    findNavController().navigate(
                                        resId = R.id.emojiPickerFragment,
                                        args = gameBundle,
                                        navOptions = NavOptions.navOptionStack
                                    )
                                },
                                buttonText = "4 Player"
                            )
                            Spacer(modifier = Modifier
                                .height(20.dp)
                                .fillMaxWidth())
                            CustomOutlinedButton(
                                buttonClick = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    findNavController().navigate(
                                        resId = R.id.onlineModeFragment,
                                        args = null,
                                        navOptions = NavOptions.navOptionStack
                                    )
                                },
                                buttonText = "Online"
                            )
                            Spacer(modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth())
                            CustomCircleIconButton(iconButtonClick = {
                                gameSound.clickSound()
                                commonViewModel.performHapticVibrate(requireView())
                                gameBundle.putSerializable(BundleKey.USER_ID,userId)
                                findNavController().navigate(
                                    resId = R.id.statsFragment,
                                    args = gameBundle,
                                    navOptions = NavOptions.navOptionStack
                                )
                            }, buttonIcon = R.drawable.ic_stats)
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
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
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
                                CustomCircleIconButton(iconButtonClick = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    findNavController().navigate(
                                        resId = R.id.settingContainerFragment,
                                        args = null,
                                        navOptions = NavOptions.navOptionStack
                                    )
                                }, buttonIcon = R.drawable.ic_settings)
                            }
                            Spacer(modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth())
                        }
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        gameSound.resumeBackgroundMusic()
        commonViewModel.updateOnlineStatus(isOnline = InternetHelper.isOnline(requireContext()))
    }

    override fun onStop() {
        super.onStop()
        commonViewModel.updateOnlineStatus(isOnline = false)
    }

    


    override fun onDestroy() {
        super.onDestroy()
        gameSound.pauseBackgroundMusic()
    }

}