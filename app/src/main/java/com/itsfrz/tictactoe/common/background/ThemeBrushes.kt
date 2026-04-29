package com.itsfrz.tictactoe.common.background

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.itsfrz.tictactoe.ui.theme.AmberSunrise
import com.itsfrz.tictactoe.ui.theme.CosmicDepth
import com.itsfrz.tictactoe.ui.theme.DawnMauve
import com.itsfrz.tictactoe.ui.theme.GoldenMoment
import com.itsfrz.tictactoe.ui.theme.PreDawnIndigo
import com.itsfrz.tictactoe.ui.theme.SaffronHorizon
import com.itsfrz.tictactoe.ui.theme.SunGlowBright
import com.itsfrz.tictactoe.ui.theme.SunGlowDeep
import com.itsfrz.tictactoe.ui.theme.SunGlowMid
import com.itsfrz.tictactoe.ui.theme.TwilightViolet
import com.itsfrz.tictactoe.ui.theme.WarmIvory

object ThemeBrushes {
    fun sky(breath: Float) = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to CosmicDepth,
            0.16f to PreDawnIndigo,
            0.32f to TwilightViolet,
            0.50f to DawnMauve.copy(alpha = 0.95f + 0.05f * breath),
            0.64f to SaffronHorizon,
            0.78f to AmberSunrise,
            0.90f to GoldenMoment,
            1.00f to WarmIvory
        )
    )

    fun sunGlow(w: Float, h: Float, breath: Float): Brush {
        val center = Offset(w * 0.5f, h * (1.06f + 0.01f * breath))

        return Brush.radialGradient(
            colorStops = arrayOf(
                0.00f to SunGlowBright.copy(alpha = 0.18f + 0.06f * breath),
                0.30f to SunGlowMid.copy(alpha = 0.14f + 0.04f * breath),
                0.55f to SunGlowDeep.copy(alpha = 0.10f + 0.03f * breath),
                1.00f to Color.Transparent
            ),
            center = center,
            radius = w * (0.85f + 0.03f * breath)
        )
    }

    fun horizonBloom(w: Float, h: Float, breath: Float): Brush {
        val center = Offset(w * 0.5f, h * (0.80f + 0.01f * breath))

        return Brush.radialGradient(
            colors = listOf(
                AmberSunrise.copy(alpha = 0.10f + 0.03f * breath),
                SaffronHorizon.copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = center,
            radius = w * 0.6f
        )
    }
}
