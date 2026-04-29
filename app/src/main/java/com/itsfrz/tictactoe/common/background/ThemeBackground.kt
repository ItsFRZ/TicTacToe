package com.itsfrz.tictactoe.common.background

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

fun Modifier.appBackground(t: Float): Modifier = this.drawBehind {
    val w = size.width
    val h = size.height
    val breath = 0.5f + 0.5f * kotlin.math.sin(t)
    drawRect(
        brush = ThemeBrushes.sky(breath),
        size = size
    )
    drawRect(
        brush = ThemeBrushes.sunGlow(w, h, breath),
        size = size
    )
    drawRect(
        brush = ThemeBrushes.horizonBloom(w, h, breath),
        size = size
    )
    drawStars(w, h, t)
    drawSacredMandala(
        center = Offset(w * 0.5f, h * 0.78f),
        radius = minOf(w, h) * (0.255f + 0.01f * breath),
        alpha = 0.05f + 0.02f * breath
    )
    val cornerAlpha = 0.06f + 0.02f * breath
    drawKamonCorner(Offset(0f, 0f), w * 0.09f, 0, cornerAlpha)
    drawKamonCorner(Offset(w, 0f), w * 0.09f, 1, cornerAlpha)
    drawKamonCorner(Offset(0f, h), w * 0.09f, 3, cornerAlpha * 0.8f)
    drawKamonCorner(Offset(w, h), w * 0.09f, 2, cornerAlpha * 0.8f)
    drawHorizonLines(w, h, breath)
    drawLiquidGlass(w, h, t)
}