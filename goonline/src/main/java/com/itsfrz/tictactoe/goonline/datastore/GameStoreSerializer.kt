package com.itsfrz.tictactoe.goonline.datastore

import android.util.Log
import androidx.datastore.core.Serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object GameStoreSerializer : Serializer<GameDataStore>{
    private val TAG = "GAME_SERIALIZER"
    override val defaultValue: GameDataStore
        get() = GameDataStore()

    override suspend fun readFrom(input: InputStream): GameDataStore {
        return try {
            Json.decodeFromString(
                deserializer = GameDataStore.serializer(),
                string = input.readBytes().decodeToString()
            )
        }catch (e : Exception){
            Log.e(TAG, "readFrom: ")
            defaultValue
        }
    }

    override suspend fun writeTo(t: GameDataStore, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = GameDataStore.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}