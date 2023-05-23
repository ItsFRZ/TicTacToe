package com.itsfrz.tictactoe.goonline.datastore.setting

import kotlinx.coroutines.flow.Flow

interface SettingRepository {
    suspend fun updateGameSetting(settingDataStore: SettingDataStore)
    suspend fun getGameSetting() : Flow<SettingDataStore>?
}