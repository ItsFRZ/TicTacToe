package com.itsfrz.tictactoe.common.functionality

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.itsfrz.tictactoe.R


class GameSound(
    private var player: MediaPlayer,
    private val popUpSoundPlayer: MediaPlayer,
    private val clickSoundPlayer: MediaPlayer,
    private val selectSoundPlayer: MediaPlayer,
    private val pieceSoundClick1Player: MediaPlayer,
    private val pieceSoundClick2Player: MediaPlayer,
    private val starSoundPlayer: MediaPlayer
) {
    
    private var isMusicEnabled : MutableState<Boolean> = mutableStateOf(false)
    private var isRoomLock : MutableState<Boolean> = mutableStateOf(false)
    private var isSoundEnabled : MutableState<Boolean> = mutableStateOf(true)
    
    
    fun updateConditionAttributes(isMusicEnabled : Boolean,isSoundEnabled : Boolean){
        this.isSoundEnabled.value = isSoundEnabled
        this.isMusicEnabled.value = isMusicEnabled
    }

    fun updateRoomLockAttributes(context: Context,isLock : Boolean,isPanoView : Boolean = false){
        try {
            this.isRoomLock.value = isLock
            if (!isPanoView){
                if (isLock){
                    stopBackgroundMusic()
                    this.player = MediaPlayer.create(context, R.raw.background_game_track)
                    resumeBackgroundMusic()
                }else{
                    stopBackgroundMusic()
                    this.player = MediaPlayer.create(context, R.raw.background_track)
                    resumeBackgroundMusic()
                }
            }else{
                stopBackgroundMusic()
                this.player = MediaPlayer.create(context, R.raw.pano_track)
                resumeBackgroundMusic()
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    
    fun startBackgroundMusic(){
        try {
            if (isMusicEnabled.value && !player.isPlaying){
                player.setVolume(0.4f,0.4f)
                player.isLooping = true
                player.start()
            }
        }catch (e : Exception){
            e.printStackTrace()
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

    private fun stopBackgroundMusic(){
        player.stop()
        player.release()
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
            popUpSoundPlayer.setVolume(0.22f, 0.22f)
            popUpSoundPlayer.start()
        }
    }

    fun clickSound(){
        if (isSoundEnabled.value){
            clickSoundPlayer.setVolume(0.10f, 0.10f)
            clickSoundPlayer.start()
        }
    }

    fun selectSound(){
        if (isSoundEnabled.value){
            selectSoundPlayer.setVolume(0.12f, 0.12f)
            selectSoundPlayer.start()
        }
    }

    fun pieceClick1MovingSound(){
        if (isSoundEnabled.value){
            pieceSoundClick1Player.setVolume(0.12f, 0.12f)
            pieceSoundClick1Player.start()
        }
    }

    fun pieceClick2MovingSound(){
        if (isSoundEnabled.value){
            pieceSoundClick2Player.setVolume(0.12f, 0.12f)
            pieceSoundClick2Player.start()
        }
    }

    fun activeStarSound(){
        if (isSoundEnabled.value){
            starSoundPlayer.setVolume(0.22f, 0.22f)
            starSoundPlayer.start()
        }
    }
}