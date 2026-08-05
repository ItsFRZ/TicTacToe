package com.itsfrz.tictactoe.setting

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
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
                val thankText = stringResource(R.string.thank_text)
                var languageExpanded = viewmodel.languageExpanded.value
                var selectedLanguage = viewmodel.selectedLanguage.value
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
                    SettingHeader(headerTitleText = stringResource(R.string.setting_text)) {
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
                            SettingSubHeader(subHeaderTitleText =  stringResource(R.string.game_text))
                            Spacer(
                                modifier = Modifier
                                    .height(4.dp)
                                    .fillMaxWidth()
                            )
                            SettingItem(
                                icon = R.drawable.ic_music,
                                title =  stringResource(R.string.music_text),
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
                                title = stringResource(R.string.sound_text),
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
                                title = stringResource(R.string.vibration_text),
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
                                title = stringResource(R.string.notification_text),
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
                            SettingSubHeader(subHeaderTitleText = stringResource(R.string.display_text))
                            Spacer(
                                modifier = Modifier
                                    .height(16.dp)
                                    .fillMaxWidth()
                            )
                            SettingItem(
                                icon = R.drawable.ic_pallete,
                                title =  stringResource(R.string.color_text),
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
                            SettingSubHeader(subHeaderTitleText =  stringResource(R.string.setting_support_text))
                            Spacer(
                                modifier = Modifier
                                    .height(16.dp)
                                    .fillMaxWidth()
                            )
                            SettingItem(
                                icon = R.drawable.ic_love,
                                title =  stringResource(R.string.donation_text),
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
                                        thankText +" :)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                            Spacer(
                                modifier = Modifier
                                    .height(16.dp)
                                    .fillMaxWidth()
                            )
                        }
                        item{
                            Spacer(
                                modifier = Modifier
                                    .height(18.dp)
                                    .fillMaxWidth()
                            )
                            SettingSubHeader(subHeaderTitleText = stringResource(R.string.select_language_text))
                            Spacer(
                                modifier = Modifier
                                    .height(18.dp)
                                    .fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier.fillMaxWidth(0.86f)
                            ) {
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { viewmodel.onEvent(SettingUseCase.OnLangToggle(true)) },
                                    value = selectedLanguage.first,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Rounded.Language, contentDescription = null, tint = Color.White)
                                    },
                                    trailingIcon = {
                                        Icon(imageVector = Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = Color.White)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = Color.White.copy(alpha = .25f),
                                        disabledTextColor = Color.White,
                                        disabledLeadingIconColor = Color.White,
                                        disabledTrailingIconColor = Color.White,
                                        disabledContainerColor = Color.Transparent,
                                        focusedBorderColor = ThemePicker.secondaryColor.value,
                                        unfocusedBorderColor = Color.White.copy(alpha = .25f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )

                                DropdownMenu (
                                    expanded = languageExpanded,
                                    onDismissRequest = {
                                        viewmodel.onEvent(SettingUseCase.OnLangToggle(false))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    viewmodel.supportedLanguages.forEach { language ->
                                        DropdownMenuItem(
                                            text = {  Text("${language.first} (${language.second})") },
                                            onClick = {
                                                viewmodel.onEvent(SettingUseCase.OnLanguageChange(language))
                                                viewmodel.onEvent(SettingUseCase.OnLangToggle(false))
                                            }
                                        )
                                    }
                                }
                            }
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