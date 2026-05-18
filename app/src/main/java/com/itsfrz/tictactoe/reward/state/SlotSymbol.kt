package com.itsfrz.tictactoe.reward.state

import androidx.compose.ui.graphics.Color

enum class SlotSymbol(
    val emoji: String,
    val reward: Int,
    val tint: Color
) {
    CHERRY("🍒", 10, Color(0xFFFF4D6D)),
    LEMON("🍋", 15, Color(0xFFFFD60A)),
    GRAPE("🍇", 20, Color(0xFF9B5DE5)),
    ORANGE("🍊", 30, Color(0xF2FFAF0E)),
    BELL("🔔", 50, Color(0xFFEAFF15)),
    BAR("🍫", 70, Color(0xFF541E00)),
    DIAMOND("💎", 100, Color(0xFF4CC9F0))
}