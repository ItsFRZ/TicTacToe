package com.itsfrz.tictactoe.setting.usecase

import com.itsfrz.tictactoe.common.enums.SettingType

sealed class SettingUseCase{
    data class OnMusicToggle(val toggle : Boolean) : SettingUseCase()
    data class OnSoundToggle(val toggle : Boolean) : SettingUseCase()
    data class OnVibrationToggle(val toggle : Boolean) : SettingUseCase()
    data class OnNotificationToggle(val toggle : Boolean) : SettingUseCase()
    data class OnUpdateSettingType(val settingType : SettingType) : SettingUseCase()
    data class OnColorClickEvent(val colorIndex : Int) : SettingUseCase()
    data class OnLanguageClickEvent(val langIndex : Int) : SettingUseCase()
}
