package com.itsfrz.tictactoe.goonline.datastore.setting

import kotlinx.coroutines.flow.Flow

interface SettingRepository {
    suspend fun updateGameSetting(settingDataStore: SettingDataStore)
    suspend fun getGameSetting() : Flow<SettingDataStore>?
    suspend fun getCoinInfo() : Int
    suspend fun getGameLevelInfo() : Set<Int>
    suspend fun updateCoinInfo(typeDebit : Boolean) : Unit
    suspend fun updateCoinInfo(token : Int) : Unit
    suspend fun onPurchase(token : Int,levelId : Int) : Unit
    suspend fun updateCash(amountUnit : Int) : Unit
    suspend fun getCashInfo() : Int
}
