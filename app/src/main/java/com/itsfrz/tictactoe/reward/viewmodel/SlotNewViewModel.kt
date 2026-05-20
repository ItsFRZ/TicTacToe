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

    private var cvm: CommonViewModel? = null

    // ─────────────────────────────────────────
    // Weighted pool — rarer = lower repeat count
    // ─────────────────────────────────────────
    private val weightedPool: List<SlotNewSymbol> = buildList {
        repeat(22) { add(SlotNewSymbol.CHERRY) }
        repeat(18) { add(SlotNewSymbol.LEMON) }
        repeat(14) { add(SlotNewSymbol.ORANGE) }
        repeat(10) { add(SlotNewSymbol.GRAPE) }
        repeat(7)  { add(SlotNewSymbol.BELL) }
        repeat(4)  { add(SlotNewSymbol.BAR) }
        repeat(1)  { add(SlotNewSymbol.DIAMOND) }
    }

    // ─────────────────────────────────────────
    // Reel animation durations (ms) — must be
    // LONGER than UI animation total per reel.
    // UI does: stagger delay + phase1 + phase2
    // + overshoot + spring ≈ 4200–5200ms max.
    // We stop state AFTER that window.
    // ─────────────────────────────────────────
    private val reelStopDelays = listOf(4200L, 4500L, 5000L)

    fun updateCoinInfo(token: Int) {
        _ui.update { it.copy(coins = token) }
        syncCoins()
    }

    fun provideCVMInstance(commonViewModel: CommonViewModel) {
        this.cvm = commonViewModel
    }

    fun spin() {
        val state = _ui.value
        if (state.spinning.any { it } || state.coins < state.betAmount) return

        // Determine result upfront — UI will animate to these
        val result = List(3) { weightedPool.random() }

        viewModelScope.launch {

            // Deduct bet, mark all reels spinning, push target symbols immediately
            // so the UI reel engine knows where to stop
            _ui.update {
                it.copy(
                    coins      = it.coins - it.betAmount,
                    reels      = result,           // targets set NOW so animation aims correctly
                    spinning   = List(3) { true },
                    lastWin    = 0,
                    resultMsg  = "Spinning...",
                    totalSpins = it.totalSpins + 1
                )
            }

            // Stop each reel AFTER UI animation has fully settled
            reelStopDelays.forEachIndexed { i, delayMs ->
                delay(delayMs)
                _ui.update { s ->
                    val newSpinning = s.spinning.toMutableList().also { it[i] = false }
                    s.copy(spinning = newSpinning)
                }

                if (i >= 1){
                    _ui.update{
                        it.copy(resultMsg = "Pending ...")
                    }
                }
            }


            _ui.update{
                it.copy(resultMsg = "Finalizing ...")
            }
            // Small buffer after last reel stops before showing result
            delay(300L)

            val win = evaluate(result, state.betAmount)
            val isJackpot = win >= state.betAmount * 8
            val isBigWin  = win >= state.betAmount * 3

            _ui.update {
                it.copy(
                    coins     = it.coins + win,
                    lastWin   = win,
                    resultMsg = when {
                        win == 0   -> randomLoseMessage()
                        isBigWin && !isJackpot -> "BIG WIN!  +$win 🪙"
                        isJackpot  -> "💎 JACKPOT!  +$win 💎"
                        else       -> "Nice!  +$win 🪙"
                    }
                )
            }

            syncCoins()

            // Auto-clear coin rain after 3.5s
            if (win >= 500) {
                delay(3_500L)
                _ui.update { it.copy(lastWin = 0) }
            }
        }
    }

    fun changeBet(amount: Int) {
        _ui.update { it.copy(betAmount = amount.coerceIn(50, 1000)) }
        // No coin sync here — bet change doesn't affect balance
    }

    // ─────────────────────────────────────────
    // Evaluate
    // ─────────────────────────────────────────
    private fun evaluate(reels: List<SlotNewSymbol>, bet: Int): Int {
        val (a, b, c) = reels

        // Three of a kind
        if (a == b && b == c) return bet * a.multiplier * 3

        // Two of a kind — find the matched symbol correctly
        return when {
            a == b -> bet * a.multiplier
            b == c -> bet * b.multiplier
            a == c -> bet * a.multiplier
            else   -> 0
        }
    }

    // ─────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────
    private fun syncCoins() {
        cvm?.onEvent(CommonUseCase.OnSlotMasterTokenUpdate(_ui.value.coins))
    }

    private fun randomLoseMessage(): String = listOf(
        "So close... 🎰",
        "Try again! 🍀",
        "Almost! 🌀",
        "Bad luck! 🎲",
        "One more! ⚡"
    ).random()
}