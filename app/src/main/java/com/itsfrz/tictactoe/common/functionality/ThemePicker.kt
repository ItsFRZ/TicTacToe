package com.itsfrz.tictactoe.common.functionality

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.itsfrz.tictactoe.common.enums.GameTheme
import com.itsfrz.tictactoe.ui.theme.*

object ThemePicker {

    private var _primaryColor : MutableState<Color> = mutableStateOf(PrimaryMain)
    val primaryColor = _primaryColor

    private var _secondaryColor : MutableState<Color> = mutableStateOf(ThemeBlue)
    val secondaryColor = _secondaryColor


    private var _themeButtonBackgroundColor : MutableState<Color> = mutableStateOf(ThemeButtonBackground)
    val themeButtonBackgroundColor = _themeButtonBackgroundColor


    private var _themeButtonBackgroundDisabled : MutableState<Color> = mutableStateOf(ThemeButtonBackgroundDisabled)
    val themeButtonBackgroundDisabled = _themeButtonBackgroundDisabled


    private var _themeButtonBorder : MutableState<Color> = mutableStateOf(ThemeButtonBorder)
    val themeButtonBorder = _themeButtonBorder



    private var _themeDialogBackground : MutableState<Color> = mutableStateOf(ThemeDialogBackground)
    val themeDialogBackground = _themeDialogBackground

    private var _themeBoardBackground : MutableState<Color> = mutableStateOf(ThemeBoardBackground)
    val themeBoardBackground = _themeBoardBackground

    private var _themeStatsTextHeading : MutableState<Color> = mutableStateOf(ThemeBoardBackground)
    val themeStatsTextHeading = _themeStatsTextHeading






    fun colorPicker(picker : GameTheme){
        when(picker){
            GameTheme.THEME_BLUE -> {
                _primaryColor.value =  PrimaryMain
                _secondaryColor.value = ThemeBlue
                _themeButtonBackgroundColor.value = ThemeButtonBackground
                _themeButtonBackgroundDisabled.value = ThemeButtonBackgroundDisabled
                _themeButtonBorder.value = ThemeButtonBorder
                _themeDialogBackground.value = ThemeDialogBackground
                _themeBoardBackground.value = ThemeBoardBackground
                _themeStatsTextHeading.value = ThemeStatsTextHeading
            }
            GameTheme.DARK_RED -> {
                _primaryColor.value =  DarkRedPrimary
                _secondaryColor.value = DarkRedSecondary
                _themeButtonBackgroundColor.value = ThemeDarkRedButtonBackground
                _themeButtonBackgroundDisabled.value = ThemeDarkRedButtonBackgroundDisabled
                _themeButtonBorder.value = ThemeDarkRedButtonBorder
                _themeDialogBackground.value = ThemeDarkRedDialogBackground
                _themeBoardBackground.value = ThemeDarkRedBoardBackground
                _themeStatsTextHeading.value = ThemeDarkRedStatsTextHeading
            }
            GameTheme.POPPY_ORANGE -> {
                _primaryColor.value =  PoppyOrangePrimary
                _secondaryColor.value = PoppyOrangeSecondary
                _themeButtonBackgroundColor.value = ThemePoppyOrangeButtonBackground
                _themeButtonBackgroundDisabled.value = ThemePoppyOrangeButtonBackgroundDisabled
                _themeButtonBorder.value = ThemePoppyOrangeButtonBorder
                _themeDialogBackground.value = ThemePoppyOrangeDialogBackground
                _themeBoardBackground.value = ThemePoppyOrangeBoardBackground
                _themeStatsTextHeading.value = ThemePoppyOrangeStatsTextHeading
            }
            GameTheme.DRACULA_GREEN -> {
                _primaryColor.value =  DraculaGreenPrimary
                _secondaryColor.value = DraculaGreenSecondary
                _themeButtonBackgroundColor.value = ThemeDraculaGreenButtonBackground
                _themeButtonBackgroundDisabled.value = ThemeDraculaGreenButtonBackgroundDisabled
                _themeButtonBorder.value = ThemeDraculaGreenButtonBorder
                _themeDialogBackground.value = ThemeDraculaGreenDialogBackground
                _themeBoardBackground.value = ThemeDraculaGreenBoardBackground
                _themeStatsTextHeading.value = ThemeDraculaGreenStatsTextHeading
            }
        }
    }




}