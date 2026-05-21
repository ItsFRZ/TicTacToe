package com.itsfrz.tictactoe.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.SettingType
import com.itsfrz.tictactoe.common.functionality.GameSound
import com.itsfrz.tictactoe.common.functionality.NavOptions
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.setting.components.SettingHeader
import com.itsfrz.tictactoe.setting.components.SettingItem
import com.itsfrz.tictactoe.setting.components.SettingSubHeader
import com.itsfrz.tictactoe.setting.usecase.SettingUseCase
import com.itsfrz.tictactoe.setting.viewmodel.SettingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingFragment : Fragment() {

    private lateinit var viewmodel: SettingViewModel
    private lateinit var commonViewModel: CommonViewModel
    private lateinit var gameSound: GameSound

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commonViewModel = CommonViewModel.getInstance()
        SettingViewModel.registerSettingViewModel(commonViewModel.settingRepository)
        viewmodel = SettingViewModel.getInstance()
        gameSound = commonViewModel.gameSound
        CoroutineScope(Dispatchers.IO).launch {
            commonViewModel.loadUserPreference()
//            gameSound.toggleBackgroundMusic()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireActivity()).apply {
            setContent {
                val backgroundMusic = viewmodel.backgroundMusic.value
                val systemSound = viewmodel.gameSound.value
                val systemVibration = viewmodel.systemVibration.value
                val gameNotification = viewmodel.gameNotification.value
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ThemePicker.primaryColor.value)
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(
                        modifier = Modifier
                            .height(18.dp)
                            .fillMaxWidth()
                    )
                    SettingHeader(headerTitleText = "Settings") {
                        gameSound.clickSound()
                        commonViewModel.performHapticVibrate(requireView())
                        findNavController().navigateUp()
                    }
                    Spacer(
                        modifier = Modifier
                            .height(18.dp)
                            .fillMaxWidth()
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            SettingSubHeader(subHeaderTitleText = "Game")
                            Spacer(
                                modifier = Modifier
                                    .height(4.dp)
                                    .fillMaxWidth()
                            )
                            SettingItem(
                                icon = R.drawable.ic_music,
                                title = "Music",
                                isToggled = backgroundMusic,
                                toggleButtonEvent = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    viewmodel.onEvent(SettingUseCase.OnMusicToggle(it))
                                    restartApplication()
                                }, buttonEvent = {})
                            Spacer(
                                modifier = Modifier
                                    .height(4.dp)
                                    .fillMaxWidth()
                            )
                            SettingItem(
                                icon = R.drawable.ic_volume,
                                title = "Sound",
                                isToggled = systemSound,
                                toggleButtonEvent = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    viewmodel.onEvent(SettingUseCase.OnSoundToggle(it))
                                }, buttonEvent = {})
                            Spacer(
                                modifier = Modifier
                                    .height(4.dp)
                                    .fillMaxWidth()
                            )
                            SettingItem(
                                icon = R.drawable.ic_vibrate,
                                title = "Vibration",
                                isToggled = systemVibration,
                                toggleButtonEvent = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    viewmodel.onEvent(SettingUseCase.OnVibrationToggle(it))
                                }, buttonEvent = {})
                            Spacer(
                                modifier = Modifier
                                    .height(4.dp)
                                    .fillMaxWidth()
                            )
                            SettingItem(
                                icon = R.drawable.ic_notify,
                                title = "Notification",
                                isToggled = gameNotification,
                                toggleButtonEvent = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    viewmodel.onEvent(SettingUseCase.OnNotificationToggle(it))
                                }, buttonEvent = {})
                        }
                        item {
                            Spacer(
                                modifier = Modifier
                                    .height(18.dp)
                                    .fillMaxWidth()
                            )
                            SettingSubHeader(subHeaderTitleText = "Display")
                            Spacer(
                                modifier = Modifier
                                    .height(16.dp)
                                    .fillMaxWidth()
                            )
                            SettingItem(
                                icon = R.drawable.ic_pallete,
                                title = "Color",
                                isToggled = true,
                                toggleButtonEvent = {},
                                isAdvance = true,
                                buttonEvent = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    val bundle = bundleOf()
                                    bundle.putSerializable(
                                        BundleKey.SETTING_TYPE,
                                        SettingType.COLOR
                                    )
                                    findNavController().navigate(
                                        resId = R.id.settingContainerFragment2,
                                        args = bundle,
                                        navOptions = NavOptions.navOptionStack
                                    )
                                })
                            Spacer(
                                modifier = Modifier
                                    .height(16.dp)
                                    .fillMaxWidth()
                            )
                        }
                        item {
                            Spacer(
                                modifier = Modifier
                                    .height(18.dp)
                                    .fillMaxWidth()
                            )
                            SettingSubHeader(subHeaderTitleText = "Support")
                            Spacer(
                                modifier = Modifier
                                    .height(16.dp)
                                    .fillMaxWidth()
                            )
                            SettingItem(
                                icon = R.drawable.ic_love,
                                title = "Donation",
                                isToggled = false,
                                toggleButtonEvent = {},
                                isAdvance = true,
                                buttonEvent = {
                                    gameSound.clickSound()
                                    commonViewModel.performHapticVibrate(requireView())
                                    val bundle = bundleOf()
                                    bundle.putBoolean(BundleKey.FULL_SUPPORT,true)
                                    findNavController().navigate(
                                        resId = R.id.supportFragment,
                                        args = bundle,
                                        navOptions = NavOptions.navOptionStack
                                    )

                                    Toast.makeText(
                                        requireActivity(),
                                        "Thankyou for generosity :)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                            Spacer(
                                modifier = Modifier
                                    .height(16.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun restartApplication() {
        val ctx: Context = requireActivity().applicationContext
        val pm = ctx.getPackageManager()
        val intent = pm.getLaunchIntentForPackage(ctx.getPackageName())
        val mainIntent = Intent.makeRestartActivityTask(intent!!.getComponent())
        ctx.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}