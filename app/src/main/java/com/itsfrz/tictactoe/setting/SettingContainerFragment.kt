package com.itsfrz.tictactoe.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.SettingType
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.setting.components.SettingColorPickerComponent
import com.itsfrz.tictactoe.setting.components.SettingHeader
import com.itsfrz.tictactoe.setting.components.SettingItem
import com.itsfrz.tictactoe.setting.components.SettingSubHeader
import com.itsfrz.tictactoe.setting.usecase.SettingUseCase
import com.itsfrz.tictactoe.setting.viewmodel.SettingViewModel
import com.itsfrz.tictactoe.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingContainerFragment : Fragment() {

    private lateinit var viewmodel : SettingViewModel
    private lateinit var commonViewModel: CommonViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingType = requireArguments().getSerializable(BundleKey.SETTING_TYPE) as SettingType
        commonViewModel = CommonViewModel.getInstance()
        viewmodel = SettingViewModel.getInstance()
        viewmodel.onEvent(SettingUseCase.OnUpdateSettingType(settingType))

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val settingTypeLayout = viewmodel.settingType.value
                val themeIndex = viewmodel.backgroundColorIndex.value
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ThemePicker.primaryColor.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier
                        .height(18.dp)
                        .fillMaxWidth())
                    SettingHeader(headerTitleText = if (settingTypeLayout == SettingType.COLOR) "Select Background Color" else "Select Language") {
                        findNavController().navigateUp()
                    }
                    Spacer(modifier = Modifier
                        .height(18.dp)
                        .fillMaxWidth())
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        item {
                            if (settingTypeLayout == SettingType.COLOR){
                                SettingColorPickerComponent(
                                    primaryColor = PrimaryMain,
                                    secondaryColor = ThemeBlue,
                                    themeTitle = "Theme Blue",
                                    isSelected = themeIndex == 0
                                ) {
                                    commonViewModel.performHapticVibrate(requireView())
                                    commonViewModel.gameSound.clickSound()
                                    viewmodel.onEvent(SettingUseCase.OnColorClickEvent(0))
                                    restartApplication()
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                SettingColorPickerComponent(
                                    primaryColor = DarkRedPrimary,
                                    secondaryColor = DarkRedSecondary,
                                    themeTitle = "Dark Red",
                                    isSelected = themeIndex == 1
                                ) {
                                    commonViewModel.performHapticVibrate(requireView())
                                    commonViewModel.gameSound.clickSound()
                                    viewmodel.onEvent(SettingUseCase.OnColorClickEvent(1))
                                    restartApplication()
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                SettingColorPickerComponent(
                                    primaryColor = PoppyOrangePrimary,
                                    secondaryColor = PoppyOrangeSecondary,
                                    themeTitle = "Poppy Orange",
                                    isSelected = themeIndex == 2
                                ) {
                                    commonViewModel.performHapticVibrate(requireView())
                                    commonViewModel.gameSound.clickSound()
                                    viewmodel.onEvent(SettingUseCase.OnColorClickEvent(2))
                                    restartApplication()
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                SettingColorPickerComponent(
                                    primaryColor = DraculaGreenPrimary,
                                    secondaryColor = DraculaGreenSecondary,
                                    themeTitle = "Dracula Green",
                                    isSelected = themeIndex == 3
                                ) {
                                    commonViewModel.performHapticVibrate(requireView())
                                    commonViewModel.gameSound.clickSound()
                                    viewmodel.onEvent(SettingUseCase.OnColorClickEvent(3))
                                    restartApplication()
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun restartApplication() {
        CoroutineScope(Dispatchers.Main).launch {
            val intent = requireActivity().intent
            requireActivity().finish()
            startActivity(intent)
        }
    }

}