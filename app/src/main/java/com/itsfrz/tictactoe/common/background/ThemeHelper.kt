package com.itsfrz.tictactoe.common.background

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.itsfrz.tictactoe.ui.theme.StarlightWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.drawStars(w: Float, h: Float, t: Float) {
    val stars = listOf(
        Triple(0.077f, 0.033f, 2.4f),
        Triple(0.377f, 0.040f, 2.8f),
        Triple(0.728f, 0.036f, 2.6f),
        Triple(0.157f, 0.069f, 2.2f),
        Triple(0.636f, 0.089f, 2.4f),
        Triple(0.940f, 0.127f, 1.4f),
    )
    stars.forEachIndexed { i, (x, y, r) ->

        val twinkle = 0.5f + 0.5f *
                kotlin.math.sin(t * 2f + i)

        val fade = when {
            y < 0.05f -> 0.9f
            y < 0.1f -> 0.7f
            else -> 0.4f
        }

        drawCircle(
            color = StarlightWhite,
            radius = r,
            center = Offset(w * x, h * y),
            alpha = fade * twinkle
        )
    }
}


internal fun DrawScope.drawLiquidGlass(w: Float, h: Float, t: Float) {
    val shimmer = 0.02f + 0.015f *
            kotlin.math.sin(t * 1.2f)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = shimmer),
                Color.Transparent
            )
        ),
        size = size
    )

    val x = (0.2f + 0.6f * (0.5f + 0.5f * kotlin.math.sin(t * 0.6f))) * w
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.06f),
                Color.Transparent
            ),
            center = Offset(x, h * 0.4f),
            radius = w * 0.25f
        ),
        size = size
    )
}

internal fun DrawScope.drawHorizonLines(w: Float, h: Float, breath: Float) {
    val baseY = h * 0.76f
    listOf(
        baseY,
        baseY + 12f,
        baseY + 22f
    ).forEachIndexed { i, y ->

        val alpha = (0.04f + 0.03f * breath) * (1f - i * 0.3f)

        drawLine(
            color = StarlightWhite.copy(alpha = alpha),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 0.5f
        )
    }
}


internal fun DrawScope.drawSacredMandala(
    center: Offset,
    radius: Float,
    alpha: Float
) {
    val stroke = Stroke(width = 0.9f)
    val color  = StarlightWhite
    val petalH    = radius * 0.72f
    val petalW    = radius * 0.28f
    val petalBase = radius * 0.74f

    for (i in 0 until 8) {
        val angle = i * PI.toFloat() / 4f
        val petalPath = buildPetal(center, petalW, petalH, angle)
        drawPath(petalPath, color = color.copy(alpha = alpha), style = stroke)
    }

    listOf(0.22f, 0.44f, 0.66f, 0.88f, 1.0f).forEachIndexed { i, ratio ->
        drawCircle(
            color  = color.copy(alpha = alpha * (1f - i * 0.08f)),
            radius = radius * ratio,
            center = center,
            style  = stroke
        )
    }

    val outerR  = radius * 0.66f
    val innerR  = radius * 0.28f
    val starPath = Path()
    for (i in 0 until 24) {
        val a = (i * PI / 12 - PI / 2).toFloat()
        val r = if (i % 2 == 0) outerR else innerR
        val x = center.x + r * cos(a)
        val y = center.y + r * sin(a)
        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
    }
    starPath.close()
    drawPath(starPath, color = color.copy(alpha = alpha * 1.1f), style = stroke)

    for (i in 0 until 8) {
        val a = (i * PI / 4).toFloat()
        drawLine(
            color       = color.copy(alpha = alpha * 0.6f),
            start       = center,
            end         = Offset(
                center.x + radius * cos(a),
                center.y + radius * sin(a)
            ),
            strokeWidth = 0.7f
        )
    }
    drawCircle(color = color.copy(alpha = alpha * 1.5f), radius = 3f, center = center)
}


internal fun buildPetal(
    center: Offset,
    halfWidth: Float,
    height: Float,
    anglRad: Float
): Path {
    val tip = Offset(
        center.x + height * sin(anglRad),
        center.y - height * cos(anglRad)
    )
    val leftAngle  = anglRad - 0.42f
    val rightAngle = anglRad + 0.42f
    val cpDist     = height * 0.55f

    return Path().apply {
        moveTo(center.x, center.y)
        cubicTo(
            center.x + cpDist * sin(leftAngle),
            center.y - cpDist * cos(leftAngle),
            tip.x - (height * 0.25f) * sin(leftAngle),
            tip.y + (height * 0.25f) * cos(leftAngle),
            tip.x, tip.y
        )
        cubicTo(
            tip.x - (height * 0.25f) * sin(rightAngle),
            tip.y + (height * 0.25f) * cos(rightAngle),
            center.x + cpDist * sin(rightAngle),
            center.y - cpDist * cos(rightAngle),
            center.x, center.y
        )
        close()
    }
}


internal fun DrawScope.drawKamonCorner(
    corner: Offset,
    size: Float,
    quadrant: Int,
    alpha: Float
) {
    val startAngle = when (quadrant) {
        0 -> 0f    // top-left → arc swings right-downward
        1 -> 90f   // top-right
        2 -> 180f  // bottom-right
        else -> 270f
    }

    val stroke = Stroke(width = 0.9f)
    val color  = StarlightWhite.copy(alpha = alpha)

    listOf(0.35f, 0.65f, 1.0f).forEach { ratio ->
        val r = size * ratio
        drawArc(
            color      = color,
            startAngle = startAngle,
            sweepAngle = 90f,
            useCenter  = false,
            topLeft    = Offset(corner.x - r, corner.y - r),
            size       = Size(r * 2, r * 2),
            style      = stroke
        )
    }
}

internal fun DrawScope.drawHorizonLines(w: Float, h: Float) {
    val horizonY = h * 0.76f
    listOf(
        Pair(horizonY,        0.07f),
        Pair(horizonY + 12f,  0.05f),
        Pair(horizonY + 22f,  0.03f),
    ).forEach { (y, opacity) ->
        drawLine(
            color       = StarlightWhite.copy(alpha = opacity),
            start       = Offset(0f, y),
            end         = Offset(w, y),
            strokeWidth = 0.5f
        )
    }
}