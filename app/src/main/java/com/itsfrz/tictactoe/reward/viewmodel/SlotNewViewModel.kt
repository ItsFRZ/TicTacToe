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

        // Decide result upfront — UI animates to these symbols
        val result = List(3) { weightedPool.random() }
        // Pre-compute win so onReelStopped can apply it the moment the last reel lands
        val win       = evaluate(result, state.betAmount)
        val isJackpot = win >= state.betAmount * 8
        val isBigWin  = win >= state.betAmount * 3
        val resultMsg = when {
            win == 0               -> randomLoseMessage()
            isBigWin && !isJackpot -> "BIG WIN!  +$win 🪙"
            isJackpot              -> "💎 JACKPOT!  +$win 💎"
            else                   -> "Nice!  +$win 🪙"
        }

        _ui.update {
            it.copy(
                coins      = it.coins - it.betAmount,
                reels      = result,
                spinning   = List(3) { true },
                lastWin    = 0,
                resultMsg  = "Spinning...",
                totalSpins = it.totalSpins + 1,
                // Cache result so onReelStopped can apply it without recomputing
                pendingWin       = win,
                pendingResultMsg = resultMsg,
            )
        }

        // If win is huge, schedule clearing coin rain after display
        if (win >= 500) {
            viewModelScope.launch {
                // Wait for all reels + result display; coin rain lingers 3.5s after result
                // We listen for spinning==false on all reels via state — but simpler:
                // delay enough for max animation + result display time
                delay(10_000L)
                _ui.update { it.copy(lastWin = 0) }
            }
        }
    }

    /**
     * Called by the UI (PremiumReel) the instant reel [index]'s animation completes.
     * When the last reel (index 2) reports done, we apply the result immediately.
     */
    fun onReelStopped(index: Int) {
        _ui.update { s ->
            val newSpinning = s.spinning.toMutableList().also { it[index] = false }
            val allStopped  = newSpinning.none { it }
            if (allStopped) {
                // All reels settled — show result right now
                val w = s.pendingWin
                s.copy(
                    spinning   = newSpinning,
                    coins      = s.coins + w,
                    lastWin    = w,
                    resultMsg  = s.pendingResultMsg,
                ).also { syncCoins(it.coins) }
            } else {
                s.copy(spinning = newSpinning)
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
    private fun syncCoins(coins: Int = _ui.value.coins) {
        cvm?.onEvent(CommonUseCase.OnSlotMasterTokenUpdate(coins))
    }

    private fun randomLoseMessage(): String = listOf(
        "So close... 🎰",
        "Try again! 🍀",
        "Almost! 🌀",
        "Bad luck! 🎲",
        "One more! ⚡"
    ).random()
}