package com.itsfrz.tictactoe.goonline.datastore.setting

import android.util.Log
import androidx.datastore.core.Serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object GameSettingSerializer : Serializer<SettingDataStore> {
    private val TAG = "SETTING_SERIALIZER"
    override val defaultValue: SettingDataStore
        get() = SettingDataStore()

    override suspend fun readFrom(input: InputStream): SettingDataStore {
        return try {
            Json.decodeFromString(
                deserializer = SettingDataStore.serializer(),
                string = input.readBytes().decodeToString()
            )
        }catch (e : Exception){
            Log.e(TAG, "readFrom: ")
            defaultValue
        }
    }

    override suspend fun writeTo(t: SettingDataStore, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = SettingDataStore.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}