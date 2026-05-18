package com.itsfrz.tictactoe.reward.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.reward.state.SlotSymbol
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SlotUiState(
    val reels: List<SlotSymbol> = List(3) { SlotSymbol.CHERRY },
    val coins: Int = 1000,
    val spinning: Boolean = false,
    val lastWin: Int = 0,
    val showCoins: Boolean = false
)

class SlotViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SlotUiState())
    val uiState: StateFlow<SlotUiState> = _uiState

    fun spin() {

        val state = _uiState.value

        if (state.spinning || state.coins < 10) return

        viewModelScope.launch {

            _uiState.value = state.copy(
                spinning = true,
                coins = state.coins - 10,
                lastWin = 0
            )

            delay(1400)

            val results = List(3) {
                SlotSymbol.entries.random()
            }

            val reward = calculateReward(results)

            _uiState.value = _uiState.value.copy(
                reels = results,
                coins = _uiState.value.coins + reward,
                spinning = false,
                lastWin = reward,
                showCoins = reward > 0
            )

            delay(1600)

            _uiState.value = _uiState.value.copy(
                showCoins = false
            )
        }
    }

    private fun calculateReward(
        reels: List<SlotSymbol>
    ): Int {

        return when {

            reels.distinct().size == 1 -> {
                reels.first().reward * 10
            }

            reels.groupBy { it }.any { it.value.size == 2 } -> {
                reels.maxOf { it.reward }
            }

            else -> 0
        }
    }
}