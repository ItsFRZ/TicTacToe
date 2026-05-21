package com.itsfrz.tictactoe.reward.state

/**
 * Slot symbols ordered by value (ascending).
 * multiplier = coin payout per matching line.
 */
enum class SlotNewSymbol(
    val emoji:      String,
    val label:      String,
    val multiplier: Int,
) {
    CHERRY ("🍒", "Cherry",  2),
    LEMON  ("🍋", "Lemon",   3),
    ORANGE ("🍊", "Orange",  4),
    GRAPE  ("🍇", "Grape",   5),
    BELL   ("🔔", "Bell",    8),
    BAR    ("🎰", "Bar",    12),
    DIAMOND("💎", "Diamond", 50),
}

data class SlotUiState(
    /** Currently displayed symbol on each of the 3 reels. */
    val reels:      List<SlotNewSymbol> = List(3) { SlotNewSymbol.CHERRY },
    /** Which reels are actively spinning. */
    val spinning:   List<Boolean>    = List(3) { false },
    /** Total coin balance. */
    val coins:      Int              = 0,
    /** Coins won in the last spin (0 = no win). */
    val lastWin:    Int              = 0,
    /** Result message shown after spin. */
    val resultMsg:  String           = "",
    /** Trigger coin rain animation. */
    val showCoins:  Boolean          = false,
    /** Cost per spin. */
    val betAmount:  Int              = 50,
    /** Total spins played. */
    val totalSpins: Int              = 0,
    /** Pre-computed win amount — applied the instant the last reel stops. */
    val pendingWin: Int              = 0,
    /** Pre-computed result message — applied the instant the last reel stops. */
    val pendingResultMsg: String     = "",
)