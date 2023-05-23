package com.itsfrz.tictactoe.goonline.datastore.setting

import android.util.Log
import androidx.datastore.core.DataStore
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameDataStore
import kotlinx.coroutines.flow.Flow


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
}