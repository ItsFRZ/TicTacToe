package com.itsfrz.tictactoe.common.functionality

import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.itsfrz.tictactoe.R


class GameSound(
    private val player: MediaPlayer,
    private val popUpSoundPlayer: MediaPlayer,
    private val clickSoundPlayer: MediaPlayer,
    private val selectSoundPlayer: MediaPlayer,
    private val pieceSoundClick1Player: MediaPlayer,
    private val pieceSoundClick2Player: MediaPlayer,
    private val starSoundPlayer: MediaPlayer
) {
    
    private var isMusicEnabled : MutableState<Boolean> = mutableStateOf(false)
    private var isSoundEnabled : MutableState<Boolean> = mutableStateOf(true)
    
    
    fun updateConditionAttributes(isMusicEnabled : Boolean,isSoundEnabled : Boolean){
        this.isSoundEnabled.value = isSoundEnabled
        this.isMusicEnabled.value = isMusicEnabled
    }
    
    fun startBackgroundMusic(){
        try {
            if (isMusicEnabled.value && !player.isPlaying){
                player.setVolume(0.2f,0.2f)
                player.isLooping = true
                player.start()
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    fun toggleBackgroundMusic(){
        if (isMusicEnabled.value){
            player.pause()
            val length = player.currentPosition
            player.seekTo(length)
            player.start()
        }else{
            player.pause()
        }
    }

    fun resumeBackgroundMusic(){
        Log.i("MUSIC", "resumeBackgroundMusic: Started Playing")
        if (!player.isPlaying){
            startBackgroundMusic()
        }
    }

    fun pauseBackgroundMusic(){
        if (player.isPlaying){
            player.pause()
        }
    }

    suspend fun stopAllMusic(){
        player.stop()
        popUpSoundPlayer.stop()
        clickSoundPlayer.stop()
        selectSoundPlayer.stop()
        pieceSoundClick1Player.stop()
        pieceSoundClick2Player.stop()
        starSoundPlayer.stop()
    }

    fun releaseAllMusic(){
        player.release()
        popUpSoundPlayer.release()
        clickSoundPlayer.release()
        selectSoundPlayer.release()
        pieceSoundClick1Player.release()
        pieceSoundClick2Player.release()
        starSoundPlayer.release()
    }
    fun triggerPopSound(){
        if (isSoundEnabled.value){
            popUpSoundPlayer.setVolume(100F,100F)
            popUpSoundPlayer.start()
        }
    }

    fun clickSound(){
        if (isSoundEnabled.value){
            clickSoundPlayer.setVolume(100F,100F)
            clickSoundPlayer.start()
        }
    }

    fun selectSound(){
        if (isSoundEnabled.value){
            selectSoundPlayer.setVolume(100F,100F)
            selectSoundPlayer.start()
        }
    }

    fun pieceClick1MovingSound(){
        if (isSoundEnabled.value){
            pieceSoundClick1Player.setVolume(100F,100F)
            pieceSoundClick1Player.start()
        }
    }

    fun pieceClick2MovingSound(){
        if (isSoundEnabled.value){
            pieceSoundClick2Player.setVolume(100F,100F)
            pieceSoundClick2Player.start()
        }
    }

    fun activeStarSound(){
        if (isSoundEnabled.value){
            starSoundPlayer.setVolume(100F,100F)
            starSoundPlayer.start()
        }
    }

    fun isInitialized() : Boolean{
        return this != null
    }

}