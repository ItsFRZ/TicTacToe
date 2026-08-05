package com.itsfrz.tictactoe.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.background.refined.AnimatedGameBackground
import com.itsfrz.tictactoe.common.components.CrystalOrbButton
import com.itsfrz.tictactoe.common.components.CustomCircleIconButton
import com.itsfrz.tictactoe.common.components.CustomOutlinedButton
import com.itsfrz.tictactoe.common.components.GameDialogue
import com.itsfrz.tictactoe.common.components.GameDialogue.GamePurchaseDialogue
import com.itsfrz.tictactoe.common.components.SlotMachineIconStripComponent
import com.itsfrz.tictactoe.common.components.TitleTextComponent
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.GameLevel
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.PlayerCount
import com.itsfrz.tictactoe.common.functionality.GameSound
import com.itsfrz.tictactoe.common.functionality.InternetHelper
import com.itsfrz.tictactoe.common.functionality.NavOptions
import com.itsfrz.tictactoe.common.functionality.ShareInfo
import com.itsfrz.tictactoe.common.functionality.isScreenTV
import com.itsfrz.tictactoe.common.usecase.CommonUseCase
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.IGameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.ISettingRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingDataStore
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingRepository
import com.itsfrz.tictactoe.home.usecase.HomePageUseCase
import com.itsfrz.tictactoe.home.viewmodel.HomePageViewModel
import com.itsfrz.tictactoe.home.viewmodel.HomePageViewModelFactory
import com.itsfrz.tictactoe.reward.audio.SlotSoundManager
import com.itsfrz.tictactoe.reward.state.GameLevelMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var backgroundPlayer: MediaPlayer
    private lateinit var popUpSoundPlayer: MediaPlayer
    private lateinit var pieceSoundClick1Player: MediaPlayer
    private lateinit var pieceSoundClick2Player: MediaPlayer
    private lateinit var clickSoundPlayer: MediaPlayer
    private lateinit var selectSoundPlayer: MediaPlayer
    private lateinit var starSoundPlayer: MediaPlayer
    private lateinit var gameWinSoundPlayer: MediaPlayer
    private lateinit var gameLossSoundPlayer: MediaPlayer
    private lateinit var coinAccumulateSoundPlayer: MediaPlayer
    private lateinit var gameSound: GameSound
    private lateinit var viewModel: HomePageViewModel
    private lateinit var cloudRepository: CloudRepository
    private lateinit var dataStoreRepository: GameStoreRepository
    private lateinit var settingRepository: SettingRepository
    private lateinit var commonViewModel: CommonViewModel
    private lateinit var soundManager : SlotSoundManager
    private var currentPurchaseLevel : Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("VM_CHECK", "onCreate: Fragment Created")
        commonViewModel = CommonViewModel.getInstance()
        setupGameSound()
        setUpOnlineConfig()
        val viewModelFactory = HomePageViewModelFactory(cloudRepository, dataStoreRepository)
        viewModel =
            ViewModelProvider(viewModelStore, viewModelFactory)[HomePageViewModel::class.java]
        viewModel.onEvent(HomePageUseCase.OnCopyUserIdEvent)
        viewModel.userId.value.let {
            commonViewModel.registerViewModel(
                dataStoreRepository,
                cloudRepository,
                settingRepository,
                gameSound,
                it
            )
        }
        lifecycleScope.launch((Dispatchers.Default)) {
            commonViewModel.loadEmojiData(requireContext())
        }
        lifecycleScope.launch(Dispatchers.IO) {
            commonViewModel.loadUserPreference()
        }
        soundManager = SlotSoundManager(requireContext())
    }

    private fun setUpOnlineConfig() {
        val gameStore = GameDataStore.getDataStore(requireContext())
        dataStoreRepository = IGameStoreRepository(gameStore)
        cloudRepository = CloudRepository(
            dataStoreRepository = dataStoreRepository,
            scope = CoroutineScope(Dispatchers.IO)
        )
        val settingStore = SettingDataStore.getDataStore(requireContext())
        settingRepository = ISettingRepository(settingStore)
    }

    private fun setupGameSound() {
        backgroundPlayer = MediaPlayer.create(requireContext(), R.raw.background_track)
        popUpSoundPlayer = MediaPlayer.create(requireContext(), R.raw.popup_sound)
        clickSoundPlayer = MediaPlayer.create(requireContext(), R.raw.button_click)
        selectSoundPlayer = MediaPlayer.create(requireContext(), R.raw.emoji_select_click)
        pieceSoundClick1Player = MediaPlayer.create(requireContext(), R.raw.piece_click_1)
        pieceSoundClick2Player = MediaPlayer.create(requireContext(), R.raw.piece_click_2)
        starSoundPlayer = MediaPlayer.create(requireContext(), R.raw.star_active)
        gameSound = GameSound(
            backgroundPlayer,
            popUpSoundPlayer,
            clickSoundPlayer,
            selectSoundPlayer,
            pieceSoundClick1Player,
            pieceSoundClick2Player,
            starSoundPlayer
        )
        gameSound.startBackgroundMusic()
        GameDialogue.setDialogSound(gameSound)
    }

    private fun onCrystalEvent(levelIndex : Int){
        currentPurchaseLevel = levelIndex
        commonViewModel.loadUserPreference()
        if(commonViewModel.checkIsLevelUnlocked(levelIndex)){
            val bundle = Bundle()
            val url = GameLevelMeta.getLevel(currentPurchaseLevel+1)
            bundle.putBoolean(BundleKey.SLOT_MACHINE,false)
            bundle.putInt(BundleKey.GAME_LEVEL,currentPurchaseLevel + 1)
            bundle.putString(BundleKey.REWARD_URL, url)
            findNavController().navigate(resId = R.id.rewardFragment, navOptions = NavOptions.navOptionStack, args = bundle)
            gameSound.clickSound()
            commonViewModel.performHapticVibrate(requireView())
        }else{
            viewModel.onEvent(HomePageUseCase.OnPurchaseDialogEvent(true))
        }
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
                val infinite = rememberInfiniteTransition()
                val t by infinite.animateFloat(
                    initialValue = 0f,
                    targetValue = (2f * Math.PI).toFloat(),
                    animationSpec = infiniteRepeatable(
                        animation = tween(18000, easing = LinearEasing)
                    )
                )
                val listState = rememberLazyListState()
                val purchaseDialog = viewModel.purchaseDialog.value
                val purchase = stringResource(R.string.happy_purchase)
                val no_balance = stringResource(R.string.no_balance)

                Box {
                    AnimatedGameBackground(modifier = Modifier.fillMaxSize())

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Image(painter = painterResource(R.drawable.bg_wave), contentDescription = "Wave Background", contentScale = ContentScale.FillBounds)

                        LazyRow(
                            modifier = Modifier
                                .height(136.dp)
                                .fillMaxWidth(),
                            state = listState,
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            ) {
                            items(100){ item ->
                                val layoutInfo = listState.layoutInfo
                                val visibleItem = layoutInfo.visibleItemsInfo
                                    .firstOrNull { it.index == item }
                                val screenCenter =
                                    layoutInfo.viewportEndOffset / 2
                                val itemCenter =
                                    (visibleItem?.offset ?: 0) +
                                            (visibleItem?.size ?: 0) / 2
                                val distance =
                                    (itemCenter - screenCenter).toFloat()
                                val normalized =
                                    (distance / screenCenter)
                                        .coerceIn(-1f, 1f)
                                val yOffset =
                                    kotlin.math.abs(normalized) * 85f
                                val scale =
                                    1f - (kotlin.math.abs(normalized) * 0.25f)
                                val alpha =
                                    1f - (kotlin.math.abs(normalized) * 0.5f)
                                Column(modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        translationY = yOffset
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha

                                        rotationZ = normalized * 12f
                                    }) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Row(modifier = Modifier.fillMaxSize()) {
                                            Column(modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight()) {
                                                Row(modifier = Modifier.fillMaxHeight(0.5F)) {  }
                                                Row(modifier = Modifier.fillMaxHeight()) {
                                                    Column(
                                                        modifier = Modifier.size(48.dp),
                                                    ) {
                                                        CrystalOrbButton(onClick = {
                                                            onCrystalEvent(item)
                                                        }, text = "${item+1}")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
//                            .appBackgroundCompat(ThemePicker.primaryColor.value.copy(alpha = 0.95f),t)
                        ,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SlotMachineIconStripComponent {
                                val bundle = Bundle()
                                bundle.putBoolean(BundleKey.SLOT_MACHINE,true)
                                bundle.putInt(BundleKey.GAME_LEVEL,0)
                                bundle.putString(BundleKey.REWARD_URL,"")
                                findNavController().navigate(resId = R.id.rewardFragment, navOptions = NavOptions.navOptionStack, args = bundle)
                            }
                            Row(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${commonViewModel.goldTokens}", style = TextStyle(color = Color.White, textAlign = TextAlign.Start, fontSize = 18.sp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Image(modifier = Modifier.size(32.dp), painter = painterResource(R.drawable.ic_gold_coin), contentDescription = "Gold Tokens")
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                        if (isScreenTV(requireContext())){
                            Spacer(modifier = Modifier.fillMaxHeight(0.2F).fillMaxWidth())
                        }else{
                            Spacer(modifier = Modifier.fillMaxHeight(0.04F).fillMaxWidth())
                        }
                        TitleTextComponent()
                        Spacer(modifier = Modifier.fillMaxHeight(0.08F).fillMaxWidth())
                        Spacer(modifier = Modifier.fillMaxWidth().height(6.dp))
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                CustomOutlinedButton(
                                    enabled = true,
                                    buttonClick = {
                                        gameSound.clickSound()
                                        commonViewModel.performHapticVibrate(requireView())
                                        gameBundle.putSerializable(BundleKey.GAME_MODE, GameMode.AI)
                                        gameBundle.putSerializable(
                                            BundleKey.PLAYER_COUNT,
                                            PlayerCount.ONE
                                        )
                                        findNavController().navigate(
                                            R.id.emojiPickerFragment, gameBundle,
                                            navOptions = NavOptions.navOptionStack
                                        )
                                    },
                                    buttonText = stringResource(R.string.computer)
                                )
                                Spacer(modifier = Modifier.height(18.dp).fillMaxWidth())
                                CustomOutlinedButton(
                                    enabled = true,
                                    buttonClick = {
                                        gameSound.clickSound()
                                        commonViewModel.performHapticVibrate(requireView())
                                        gameBundle.putSerializable(
                                            BundleKey.GAME_MODE,
                                            GameMode.TWO_PLAYER
                                        )
                                        gameBundle.putSerializable(
                                            BundleKey.PLAYER_COUNT,
                                            PlayerCount.TWO
                                        )
                                        findNavController().navigate(
                                            resId = R.id.emojiPickerFragment,
                                            args = gameBundle,
                                            navOptions = NavOptions.navOptionStack
                                        )
                                    },
                                    buttonText = stringResource(R.string.two_player)
                                )
                                Spacer(modifier = Modifier.height(18.dp).fillMaxWidth())
                                CustomOutlinedButton(
                                    enabled = true,
                                    buttonClick = {
                                        gameSound.clickSound()
                                        commonViewModel.performHapticVibrate(requireView())
                                        gameBundle.putSerializable(
                                            BundleKey.GAME_MODE,
                                            GameMode.FOUR_PLAYER
                                        )
                                        gameBundle.putSerializable(
                                            BundleKey.PLAYER_COUNT,
                                            PlayerCount.FOUR
                                        )
                                        findNavController().navigate(
                                            resId = R.id.emojiPickerFragment,
                                            args = gameBundle,
                                            navOptions = NavOptions.navOptionStack
                                        )
                                    },
                                    buttonText = stringResource(R.string.four_player)
                                )
//                                Spacer(modifier = Modifier.height(24.dp).fillMaxWidth())
//                                CustomOutlinedButton(
//                                    buttonClick = {
//                                        gameSound.clickSound()
//                                        commonViewModel.performHapticVibrate(requireView())
//                                        findNavController().navigate(
//                                            resId = R.id.onlineModeFragment,
//                                            args = null,
//                                            navOptions = NavOptions.navOptionStack
//                                        )
//                                    },
//                                    buttonText = stringResource(R.string.online_player)
//                                )
                                Spacer(modifier = Modifier.height(24.dp).fillMaxWidth())
                                CustomCircleIconButton(iconButtonClick = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    scope.launch(Dispatchers.Main) {
                                        gameSound.clickSound()
                                        commonViewModel.performHapticVibrate(requireView())
                                        val bundle = bundleOf()
                                        bundle.putBoolean(BundleKey.FULL_SUPPORT,false)
                                        findNavController().navigate(
                                            resId = R.id.supportFragment,
                                            args = bundle,
                                            navOptions = NavOptions.navOptionStack
                                        )
                                        Toast.makeText(
                                            requireActivity(),
                                            purchase,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }, buttonIcon = R.drawable.ic_purchase)
                                Spacer(modifier = Modifier.height(8.dp).fillMaxWidth())
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
                                            async { viewModel.onEvent(HomePageUseCase.OnCopyUserIdEvent) }.await()
                                            val message =
                                                "${ShareInfo.SHARE_HEADER}\n${ShareInfo.SHARE_TITLE}\n${ShareInfo.SHARE_SUBTITLE}\n\nUserId : ✄-x${userId}x-✄"
                                            val intent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, message)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Share"))
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
                                Spacer(modifier = Modifier.height(28.dp).fillMaxWidth())
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = purchaseDialog,
                        enter = fadeIn() + expandIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            GamePurchaseDialogue(
                                context = requireContext(),
                                levelId = currentPurchaseLevel + 1,
                                commonViewModel = commonViewModel,
                                onCloseEvent = {
                                    viewModel.onEvent(HomePageUseCase.OnPurchaseDialogEvent(false))
                                },
                                onDialogueEvent = {
                                    if (commonViewModel.goldTokens < (currentPurchaseLevel + 1) * 1000){
                                        Toast.makeText(requireContext(), no_balance, Toast.LENGTH_SHORT).show()
                                    }else{
                                        Log.i("PURCHASE_FLOW", "GamePurchaseDialogue: onDialogueEvent")
                                        commonViewModel.onEvent(CommonUseCase.OnLevelPurchase(token = (currentPurchaseLevel + 1) * 1000, levelId = currentPurchaseLevel+1))
                                        soundManager.cash()
                                    }
                                    viewModel.onEvent(HomePageUseCase.OnPurchaseDialogEvent(false))
                                }
                            )
                        }

                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        gameSound.updateRoomLockAttributes(requireContext(),false,false)
        commonViewModel.updateOnlineStatus(isOnline = InternetHelper.isOnline(requireContext()))
    }

    override fun onStop() {
        super.onStop()
        commonViewModel.updateOnlineStatus(isOnline = false)
    }


    override fun onDestroy() {
        super.onDestroy()
//        gameSound.pauseBackgroundMusic()
    }

}