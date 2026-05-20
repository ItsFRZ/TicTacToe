package com.itsfrz.tictactoe.reward.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.common.usecase.CommonUseCase
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.reward.state.SlotNewSymbol
import com.itsfrz.tictactoe.reward.state.SlotUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SlotNewViewModel : ViewModel() {

    private val _ui = MutableStateFlow(SlotUiState())
    val uiState: StateFlow<SlotUiState> = _ui.asStateFlow()

    private lateinit var cvm : CommonViewModel

    private val symbols = SlotNewSymbol.entries
    // Weighted pool: rarer symbols appear less often
    private val weightedPool: List<SlotNewSymbol> = buildList {
        repeat(20) { add(SlotNewSymbol.CHERRY) }
        repeat(16) { add(SlotNewSymbol.LEMON) }
        repeat(14) { add(SlotNewSymbol.ORANGE) }
        repeat(12) { add(SlotNewSymbol.GRAPE) }
        repeat(8)  { add(SlotNewSymbol.BELL) }
        repeat(5)  { add(SlotNewSymbol.BAR) }
        repeat(1)  { add(SlotNewSymbol.DIAMOND) }
    }

    fun updateCoinInfo(token : Int){
        _ui.update {
            it.copy(coins = token)
        }
    }

    fun provideCVMInstance(commonViewModel: CommonViewModel){
        this.cvm = commonViewModel
    }

    fun spin() {
        val state = _ui.value
        if (state.spinning.any { it } || state.coins < state.betAmount) return

        val result = List(3) { weightedPool.random() }

        viewModelScope.launch {
            // Deduct bet, start all reels spinning
            _ui.update {
                it.copy(
                    coins     = it.coins - it.betAmount,
                    spinning  = List(3) { true },
                    showCoins = false,
                    lastWin   = 0,
                    resultMsg = "",
                    totalSpins = it.totalSpins + 1
                )
            }

            // Stop each reel with stagger — reel 0 stops first, then 1, then 2
            val stopDelays = listOf(900L, 1500L, 2200L)
            stopDelays.forEachIndexed { i, delayMs ->
                delay(delayMs)
                _ui.update { state ->
                    val newReels    = state.reels.toMutableList().also { it[i] = result[i] }
                    val newSpinning = state.spinning.toMutableList().also { it[i] = false }
                    state.copy(reels = newReels, spinning = newSpinning)
                }
            }

            // Evaluate win
            delay(200)
            val win     = evaluate(result, state.betAmount)
            val bigWin  = win >= state.betAmount * 5
            _ui.update {
                it.copy(
                    coins     = it.coins + win,
                    lastWin   = win,
                    showCoins = bigWin,
                    resultMsg = when {
                        win == 0            -> "Try again!"
                        win < state.betAmount * 3 -> "Nice! +$win 🪙"
                        win < state.betAmount * 8 -> "Big Win! +$win 🪙"
                        else                -> "JACKPOT! +$win 💎"
                    }
                )
            }

            // Hide coin rain after 3 s
            if (bigWin) {
                delay(3_000)
                _ui.update { it.copy(showCoins = false) }
            }

            cvm.onEvent(CommonUseCase.OnSlotMasterTokenUpdate(_ui.value.coins))
        }
    }

    fun changeBet(amount: Int) {
        _ui.update { it.copy(betAmount = amount.coerceIn(5, 1000)) }
        cvm.onEvent(CommonUseCase.OnSlotMasterTokenUpdate(_ui.value.coins))
    }

    private fun evaluate(reels: List<SlotNewSymbol>, bet: Int): Int {
        // All three match → full payout
        if (reels[0] == reels[1] && reels[1] == reels[2])
            return bet * reels[0].multiplier * 3

        // Any two match → half payout
        val pairs = listOf(
            reels[0] == reels[1],
            reels[1] == reels[2],
            reels[0] == reels[2],
        )
        if (pairs.any { it }) {
            val matched = if (reels[0] == reels[1]) reels[0]
            else if (reels[1] == reels[2]) reels[1]
            else reels[0]
            return bet * matched.multiplier
        }
        return 0
    }
}