package com.itsfrz.tictactoe.goonline.datastore.setting

import android.util.Log
import androidx.datastore.core.DataStore
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull


class ISettingRepository(
    private val dataStore: DataStore<SettingDataStore>
): SettingRepository {

    private val TAG : String = "SETTING_STORE"

    override suspend fun updateGameSetting(settingDataStore: SettingDataStore) {
        try {
            dataStore.updateData {
                settingDataStore
            }

        }catch (e : Exception){
            Log.e(TAG, "updateGameSetting: ", )
        }
    }

    override suspend fun getGameSetting(): Flow<SettingDataStore> {
        return dataStore.data
    }

    override suspend fun getCoinInfo(): Int {
        return dataStore.data.first().coinInfo
    }

    override suspend fun getGameLevelInfo(): Set<Int> {
        return dataStore.data.first().gameLevelInfo
    }

    override suspend fun updateCoinInfo(typeDebit : Boolean) {
      getGameSetting().firstOrNull()?.let { store ->
          var coins = store.coinInfo
          if (typeDebit){
              if (coins == 0){
                  coins = store.coinInfo + 500
              }else if (coins <= 300){
                   coins = store.coinInfo - 300
              }else{
                  coins = store.coinInfo + 200
              }
          }else{
              coins = store.coinInfo + 500
          }

          updateGameSetting(store.copy(coinInfo = coins))
      }
    }

    override suspend fun updateCoinInfo(token: Int) {
        getGameSetting().firstOrNull()?.let { store ->
            updateGameSetting(store.copy(coinInfo = token))
        }
    }

    override suspend fun onPurchase(token: Int,levelId : Int) {
        getGameSetting().firstOrNull()?.let { store ->
            Log.i("PURCHASE_FLOW", "ISettingRepository: onPurchase ${token} & level id ${levelId}")
            if (store.coinInfo >= token){
                val levelInfo = getGameLevelInfo().toMutableSet()
                levelInfo.add(levelId)
                val balance = store.coinInfo - token
                updateGameSetting(store.copy(coinInfo = balance, gameLevelInfo = levelInfo))
                Log.i("PURCHASE_FLOW", "ISettingRepository: Purchase Success")
            }
        }
    }

    override suspend fun updateCash(amountUnit: Int) {
        getGameSetting().firstOrNull()?.let { store ->
            updateGameSetting(store.copy(cashAmount = amountUnit))
        }
    }

    override suspend fun getCashInfo(): Int {
        return dataStore.data.first().cashAmount
    }
}