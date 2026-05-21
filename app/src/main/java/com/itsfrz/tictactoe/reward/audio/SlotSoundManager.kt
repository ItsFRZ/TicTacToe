package com.itsfrz.tictactoe.reward.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.itsfrz.tictactoe.R
import kotlinx.coroutines.delay

class SlotSoundManager(context: Context) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .build()
        )
        .build()

    private val spin = pool.load(context, R.raw.slot_spin, 1)
    private val win = pool.load(context, R.raw.win, 1)
    private val click = pool.load(context, R.raw.click, 1)
    private val cash = pool.load(context, R.raw.cash_in, 1)
    private val fail = pool.load(context, R.raw.slot_fail, 1)
    private val jackpot = pool.load(context, R.raw.jackpot_coins, 1)
    private val coinRain = pool.load(context, R.raw.coin_rain, 1)

    suspend fun spin(lastWin : Int,message : String) {
        pool.play(spin, 1f, 1f, 1, 0, 1f)
        delay(4700)
        if (message.isEmpty() && lastWin == 0){
            pool.play(win, 1f, 1f, 1, 0, 1f)
        }else if (lastWin == 0 && message.startsWith("Try again")){
            pool.play(fail, 1f, 1f, 1, 0, 1f)
        }else if (message.startsWith("Nice")){
            pool.play(cash, 1f, 1f, 1, 0, 1f)
        }else if (message.startsWith("Big Win!")){
            pool.play(cash, 1f, 1f, 1, 0, 1f)
        }else if (message.startsWith("JACKPOT")){
            pool.play(jackpot, 1f, 1f, 1, 0, 1f)
        }else{
            pool.play(cash, 1f, 1f, 1, 0, 1f)
        }
        delay(1000)
    }

    fun cash(){
        pool.play(cash, 1f, 1f, 1, 0, 1f)
    }

    fun coinRain() {
        pool.play(coinRain, 1f, 1f, 1, 0, 1f)
    }

    fun win() {
        pool.play(win, 1f, 1f, 1, 0, 1f)
    }

    fun click() {
        pool.play(click, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }
}