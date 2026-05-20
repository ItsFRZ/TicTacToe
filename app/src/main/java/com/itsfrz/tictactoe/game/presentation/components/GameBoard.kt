package com.itsfrz.tictactoe.game.presentation.components

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import kotlinx.coroutines.launch
import kotlin.math.*

// ─────────────────────────────────────────────────────────
//  CONSTANTS & DESIGN TOKENS
// ─────────────────────────────────────────────────────────
private object DesignTokens {
    // Elevation & depth
    val BOARD_ELEVATION_DP = 28.dp
    val CELL_PRESS_DEPTH = 6.dp
    val PIECE_FLOAT_OFFSET = (-4).dp

    // Durations (ms)
    const val SPRING_PRESS_MS      = 120
    const val SPRING_RELEASE_MS    = 380
    const val WINNER_SWEEP_MS      = 900
    const val BOARD_BREATHE_MS     = 3800
    const val AMBIENT_SHIMMER_MS   = 6000
    const val PIECE_SPAWN_MS       = 420
    const val AI_PULSE_MS          = 1200

    // Colors — glass & metal palette
    val GLASS_WHITE      = Color(0x18FFFFFF)
    val GLASS_EDGE       = Color(0x40FFFFFF)
    val DEEP_SHADOW      = Color(0xBB000000)
    val INNER_SHADOW     = Color(0x55000000)
    val METALLIC_HIGH    = Color(0xCCFFFFFF)
    val WINNER_GOLD      = Color(0xFFFFD700)
    val WINNER_GLOW      = Color(0x88FFD700)
    val AI_COLD_GLOW     = Color(0xFF00E5FF)
    val AI_COLD_SHADOW   = Color(0x6600B8D4)
    val DANGER_RED       = Color(0xFFFF1744)
    val ENERGY_ACCENT    = Color(0xFF7C4DFF)
}

// ─────────────────────────────────────────────────────────
//  MAIN COMPOSABLE
// ─────────────────────────────────────────────────────────
@Composable
fun GameBoard(
    crossList: List<Int>,
    rightList: List<Int>,
    player3List: List<Int>,
    player4List: List<Int>,
    gameMode: GameMode,
    isWinner: Boolean,
    gameCellList: List<Int>,
    columnCount: Int,
    boardHeight: Dp,
    winnerIndexList: List<Int>,
    isPlayerMoved: Boolean,
    onMove: (index: Int) -> Unit,
    onAIMove: () -> Unit,
    userId: String,
    currentUserId: String,
    @DrawableRes playerIcons: List<Int>
) {
    // ── Icon resolution (unchanged logic) ──
    val playerOneIcon: Int
    val playerTwoIcon: Int
    val playerThreeIcon: Int
    val playerFourIcon: Int
    when (gameMode) {
        GameMode.AI -> {
            playerOneIcon   = playerIcons.getOrElse(0) { R.drawable.ic_tick_i }
            playerTwoIcon   = R.drawable.ic_ai_emoji
            playerThreeIcon = 0
            playerFourIcon  = 0
        }
        GameMode.TWO_PLAYER -> {
            playerOneIcon   = playerIcons.getOrElse(0) { R.drawable.ic_tick_i }
            playerTwoIcon   = playerIcons.getOrElse(1) { R.drawable.ic_cross_i }
            playerThreeIcon = 0
            playerFourIcon  = 0
        }
        GameMode.FOUR_PLAYER -> {
            playerOneIcon   = playerIcons.getOrElse(0) { R.drawable.ic_tick_i }
            playerTwoIcon   = playerIcons.getOrElse(1) { R.drawable.ic_cross_i }
            playerThreeIcon = playerIcons.getOrElse(2) { R.drawable.ic_tick_i }
            playerFourIcon  = playerIcons.getOrElse(3) { R.drawable.ic_cross_i }
        }
        else -> {
            playerOneIcon   = R.drawable.ic_tick_i
            playerTwoIcon   = R.drawable.ic_cross_i
            playerThreeIcon = 0
            playerFourIcon  = 0
        }
    }

    if (isWinner) Log.i("CHECK_WINNER", "GameBoard: Winner Index $winnerIndexList")

    // ── Board-level ambient animations ──
    val infiniteTransition = rememberInfiniteTransition(label = "board_ambient")

    // Breathing scale — the board lives and breathes
    val boardBreathScale by infiniteTransition.animateFloat(
        initialValue = 1.000f,
        targetValue  = 1.004f,
        animationSpec = infiniteRepeatable(
            animation = tween(DesignTokens.BOARD_BREATHE_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Ambient light sweep angle
    val ambientAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(DesignTokens.AMBIENT_SHIMMER_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_angle"
    )

    // Winner sweep progress
    val winnerSweep by animateFloatAsState(
        targetValue = if (isWinner) 1f else 0f,
        animationSpec = tween(DesignTokens.WINNER_SWEEP_MS, easing = FastOutSlowInEasing),
        label = "winner_sweep"
    )

    // Board border glow alpha
    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue  = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_glow"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = boardBreathScale
                scaleY = boardBreathScale
                // Perspective depth tilt — VisionOS inspired
                rotationX = 3f
                cameraDistance = 16f * density
                shadowElevation = with(this) { DesignTokens.BOARD_ELEVATION_DP.toPx() }
                shape = RoundedCornerShape(16.dp)
                clip = true
            }
            // Board outer shell — glass + metallic border
            .drawBehind {
                drawBoardShell(ambientAngle, borderGlowAlpha, winnerSweep)
            }
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                // Gradient border: Modifier.border() has no brush+shape overload.
                // drawBehind lets us stroke a RoundRect with a linearGradient paint directly.
                .drawBehind {
                    val strokePx   = 1.5.dp.toPx()
                    val half       = strokePx / 2f
                    val cornerPx   = 16.dp.toPx()
                    val gradBrush  = Brush.linearGradient(
                        colors = listOf(
                            DesignTokens.GLASS_EDGE,
                            DesignTokens.METALLIC_HIGH,
                            DesignTokens.GLASS_EDGE
                        ),
                        start = Offset(0f, 0f),
                        end   = Offset(size.width, size.height)
                    )
                    drawRoundRect(
                        brush       = gradBrush,
                        topLeft     = Offset(half, half),
                        size        = Size(size.width - strokePx, size.height - strokePx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx),
                        style        = Stroke(width = strokePx)
                    )
                }
                .clip(RoundedCornerShape(16.dp))
                .background(ThemePicker.primaryColor.value.copy(alpha = 0.92f)),
            columns = GridCells.Fixed(columnCount),
            userScrollEnabled = false,
            content = {
                itemsIndexed(items = gameCellList, key = { idx, _ -> idx }) { index, _ ->
                    val isWinnerCell = isWinner && index in winnerIndexList

                    // Determine which player owns this cell.
                    // Plain val is correct here: the lists are stable snapshot state read
                    // in the same composition scope, so no extra State wrapper is needed.
                    val cellOwner: CellOwner = when {
                        index in crossList   -> CellOwner.PLAYER_TWO
                        index in rightList   -> CellOwner.PLAYER_ONE
                        index in player3List -> CellOwner.PLAYER_THREE
                        index in player4List -> CellOwner.PLAYER_FOUR
                        else                 -> CellOwner.EMPTY
                    }

                    val icon: Int = when (cellOwner) {
                        CellOwner.PLAYER_TWO   -> playerTwoIcon
                        CellOwner.PLAYER_ONE   -> playerOneIcon
                        CellOwner.PLAYER_THREE -> playerThreeIcon
                        CellOwner.PLAYER_FOUR  -> playerFourIcon
                        CellOwner.EMPTY        -> 0
                    }

                    val isAiCell = gameMode == GameMode.AI && cellOwner == CellOwner.PLAYER_TWO

                    ImmersiveGameCell(
                        index          = index,
                        cellOwner      = cellOwner,
                        icon           = icon,
                        isWinnerCell   = isWinnerCell,
                        isAiCell       = isAiCell,
                        winnerSweep    = winnerSweep,
                        ambientAngle   = ambientAngle,
                        boardHeight    = boardHeight,
                        gameMode       = gameMode,
                        isWinner       = isWinner,
                        isPlayerMoved  = isPlayerMoved,
                        crossList      = crossList,
                        rightList      = rightList,
                        player3List    = player3List,
                        player4List    = player4List,
                        userId         = userId,
                        currentUserId  = currentUserId,
                        onMove         = onMove,
                        onAIMove       = onAIMove
                    )
                }
            }
        )

        // ── Frosted glass top sheen overlay ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DesignTokens.GLASS_WHITE,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

// ─────────────────────────────────────────────────────────
//  CELL COMPOSABLE — isolated recomposition boundary
// ─────────────────────────────────────────────────────────
@Composable
private fun ImmersiveGameCell(
    index: Int,
    cellOwner: CellOwner,
    icon: Int,
    isWinnerCell: Boolean,
    isAiCell: Boolean,
    winnerSweep: Float,
    ambientAngle: Float,
    boardHeight: Dp,
    gameMode: GameMode,
    isWinner: Boolean,
    isPlayerMoved: Boolean,
    crossList: List<Int>,
    rightList: List<Int>,
    player3List: List<Int>,
    player4List: List<Int>,
    userId: String,
    currentUserId: String,
    onMove: (Int) -> Unit,
    onAIMove: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Per-cell press physics
    val pressDepth by animateFloatAsState(
        targetValue = if (isPressed && cellOwner == CellOwner.EMPTY) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "press_$index"
    )

    // Piece spawn — scale from 0 to 1 with spring bounce
    var spawned by remember(cellOwner) { mutableStateOf(cellOwner == CellOwner.EMPTY) }
    val spawnScale by animateFloatAsState(
        targetValue = if (spawned || cellOwner == CellOwner.EMPTY) 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "spawn_$index"
    )
    LaunchedEffect(cellOwner) {
        if (cellOwner != CellOwner.EMPTY) spawned = false
    }

    // Winner cell pulse
    val infiniteTransition = rememberInfiniteTransition(label = "cell_$index")
    val winnerPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = if (isWinnerCell) 1.07f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "winner_pulse_$index"
    )

    // AI cell cold glow alpha
    val aiGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue  = if (isAiCell) 0.75f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(DesignTokens.AI_PULSE_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_glow_$index"
    )

    val cellScale = when {
        isWinnerCell -> winnerPulseScale
        else         -> 1f - (pressDepth * 0.04f)
    }

    val cellTranslationY = pressDepth * with(LocalDensity.current) {
        DesignTokens.CELL_PRESS_DEPTH.toPx()
    }

    // Tap handler — preserves original move logic exactly
    val handleTap: () -> Unit = {
        if (!isWinner) {
            when (gameMode) {
                GameMode.TWO_PLAYER -> {
                    if (index !in crossList && index !in rightList) onMove(index)
                }
                GameMode.AI -> {
                    if (index !in crossList && index !in rightList && !isPlayerMoved) {
                        onMove(index)
                        onAIMove()
                    }
                }
                GameMode.FRIEND -> {
                    if (index !in crossList && index !in rightList && userId == currentUserId) {
                        onMove(index)
                    }
                }
                GameMode.FOUR_PLAYER -> {
                    if (index !in crossList && index !in rightList &&
                        index !in player3List && index !in player4List) {
                        onMove(index)
                    }
                }
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .height(boardHeight)
            .graphicsLayer {
                scaleX = cellScale
                scaleY = cellScale
                translationY = cellTranslationY
                shadowElevation = if (isWinnerCell) 24f else if (isPressed) 2f else 8f
                shape = RoundedCornerShape(4.dp)
                clip = false
            }
            // Cell surface — glass + neumorphic inner shadow system
            .drawBehind {
                drawCellSurface(
                    isWinnerCell  = isWinnerCell,
                    isAiCell      = isAiCell,
                    isEmpty       = cellOwner == CellOwner.EMPTY,
                    pressDepth    = pressDepth,
                    winnerSweep   = winnerSweep,
                    ambientAngle  = ambientAngle,
                    aiGlowAlpha   = aiGlowAlpha
                )
            }
            // Cell edge gradient — drawn manually; Modifier.border has no brush overload.
            // The gradient runs top-left (highlight) → bottom-right (shadow) for the
            // directional-light illusion without any extra composable overhead.
            .drawBehind {
                val strokePx = 0.5.dp.toPx()
                val half     = strokePx / 2f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            DesignTokens.GLASS_EDGE.copy(alpha = 0.6f),
                            Color.Transparent,
                            DesignTokens.INNER_SHADOW.copy(alpha = 0.4f)
                        ),
                        start = Offset(0f, 0f),
                        end   = Offset(size.width, size.height)
                    ),
                    topLeft = Offset(half, half),
                    size    = Size(size.width - strokePx, size.height - strokePx),
                    style   = Stroke(width = strokePx)
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication        = null,   // we render our own physics
                onClick           = handleTap
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Empty cell: ambient grid energy ──
        if (cellOwner == CellOwner.EMPTY) {
            EmptyCellGlow(ambientAngle = ambientAngle, pressDepth = pressDepth)
        }

        // ── Occupied cell: piece with spawn physics ──
        if (cellOwner != CellOwner.EMPTY && icon != 0) {
            PieceRenderer(
                icon       = icon,
                spawnScale = spawnScale,
                isAiCell   = isAiCell,
                isWinner   = isWinnerCell,
                aiGlowAlpha = aiGlowAlpha
            )
        }

        // ── Winner gold sweep overlay ──
        if (isWinnerCell) {
            WinnerCellOverlay(winnerSweep = winnerSweep)
        }

        // ── Top-edge highlight — physical depth illusion ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DesignTokens.METALLIC_HIGH.copy(alpha = 0.5f - pressDepth * 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

// ─────────────────────────────────────────────────────────
//  PIECE RENDERER — premium icon with depth + glow
// ─────────────────────────────────────────────────────────
@Composable
private fun PieceRenderer(
    icon: Int,
    spawnScale: Float,
    isAiCell: Boolean,
    isWinner: Boolean,
    aiGlowAlpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = spawnScale
                scaleY = spawnScale
                // Piece floats slightly above surface
                translationY = -4f * spawnScale
                // Depth rotation on spawn
                rotationY = (1f - spawnScale) * 45f
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        // AI cold glow halo
        if (isAiCell) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.75f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DesignTokens.AI_COLD_GLOW.copy(alpha = aiGlowAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Winner gold halo
        if (isWinner) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DesignTokens.WINNER_GLOW,
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Piece drop-shadow
        Box(
            modifier = Modifier
                .fillMaxSize(0.65f)
                .offset(y = 4.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            DesignTokens.DEEP_SHADOW.copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // The actual piece icon
        androidx.compose.material.Icon(
            painter  = painterResource(id = icon),
            contentDescription = null,
            tint     = Color.Unspecified,
            modifier = Modifier
                .fillMaxSize(0.58f)
                .graphicsLayer {
                    // Soft metallic rim via colorFilter trick
                    alpha = 1f
                }
        )

        // Piece specular highlight — ceramic/glass feel
        Box(
            modifier = Modifier
                .fillMaxSize(0.3f)
                .offset(x = (-4).dp, y = (-6).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            DesignTokens.METALLIC_HIGH.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

// ─────────────────────────────────────────────────────────
//  EMPTY CELL GLOW — ambient energy on unoccupied cells
// ─────────────────────────────────────────────────────────
@Composable
private fun EmptyCellGlow(ambientAngle: Float, pressDepth: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r  = size.minDimension * 0.4f
                // Subtle rotating energy dot on press anticipation
                if (pressDepth > 0.01f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DesignTokens.ENERGY_ACCENT.copy(alpha = pressDepth * 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = r
                        ),
                        center = Offset(cx, cy),
                        radius = r
                    )
                }
            }
    )
}

// ─────────────────────────────────────────────────────────
//  WINNER CELL OVERLAY — cinematic gold sweep
// ─────────────────────────────────────────────────────────
@Composable
private fun WinnerCellOverlay(winnerSweep: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Gold sweep from left to right, cinematic
                val sweepWidth = size.width * winnerSweep
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            DesignTokens.WINNER_GLOW,
                            DesignTokens.WINNER_GLOW.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX   = sweepWidth.coerceAtLeast(1f)
                    ),
                    size = Size(sweepWidth, size.height)
                )
                // Gold border pulse
                drawRect(
                    color  = DesignTokens.WINNER_GOLD.copy(alpha = 0.6f * winnerSweep),
                    size   = size,
                    style  = Stroke(width = 2.dp.toPx())
                )
            }
    )
}

// ─────────────────────────────────────────────────────────
//  DRAW EXTENSIONS — GPU-friendly canvas work
// ─────────────────────────────────────────────────────────

private fun DrawScope.drawBoardShell(
    ambientAngle: Float,
    borderGlowAlpha: Float,
    winnerSweep: Float
) {
    val w = size.width
    val h = size.height
    val r = 16.dp.toPx()

    // Outer glow — dynamic ambient light sweep
    val glowAngleRad = Math.toRadians(ambientAngle.toDouble()).toFloat()
    val glowX = w / 2f + cos(glowAngleRad) * w * 0.6f
    val glowY = h / 2f + sin(glowAngleRad) * h * 0.6f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                ThemePicker.secondaryColor.value.copy(alpha = borderGlowAlpha * 0.3f),
                Color.Transparent
            ),
            center = Offset(glowX, glowY),
            radius = w * 0.7f
        ),
        center = Offset(glowX, glowY),
        radius = w * 0.7f
    )

    // Winner board-level golden bloom
    if (winnerSweep > 0f) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    DesignTokens.WINNER_GLOW.copy(alpha = 0.15f * winnerSweep),
                    Color.Transparent
                ),
                center = Offset(w / 2f, h / 2f),
                radius = w * 0.9f
            )
        )
    }

    // Deep bottom shadow — elevation illusion
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                DesignTokens.DEEP_SHADOW.copy(alpha = 0.7f)
            ),
            startY = h * 0.7f,
            endY   = h + 24.dp.toPx()
        )
    )
}

private fun DrawScope.drawCellSurface(
    isWinnerCell: Boolean,
    isAiCell: Boolean,
    isEmpty: Boolean,
    pressDepth: Float,
    winnerSweep: Float,
    ambientAngle: Float,
    aiGlowAlpha: Float
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f

    // Base cell fill
    val baseFill = when {
        isWinnerCell -> Brush.radialGradient(
            colors = listOf(
                DesignTokens.WINNER_GOLD.copy(alpha = 0.25f),
                ThemePicker.primaryColor.value
            ),
            center = Offset(cx, cy),
            radius = w * 0.8f
        )
        isAiCell -> Brush.radialGradient(
            colors = listOf(
                DesignTokens.AI_COLD_SHADOW.copy(alpha = aiGlowAlpha * 0.6f),
                ThemePicker.primaryColor.value
            ),
            center = Offset(cx, cy),
            radius = w * 0.7f
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                ThemePicker.primaryColor.value.copy(alpha = 0.85f),
                ThemePicker.primaryColor.value
            )
        )
    }
    drawRect(brush = baseFill)

    // Inner shadow — neumorphic depth on press
    val innerShadowAlpha = 0.15f + pressDepth * 0.3f
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                DesignTokens.INNER_SHADOW.copy(alpha = innerShadowAlpha)
            ),
            center = Offset(cx, cy),
            radius = w * 0.9f
        )
    )

    // Top-left highlight — directional light source
    val highlightAlpha = (0.2f - pressDepth * 0.15f).coerceAtLeast(0f)
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                DesignTokens.METALLIC_HIGH.copy(alpha = highlightAlpha),
                Color.Transparent
            ),
            start = Offset(0f, 0f),
            end   = Offset(w * 0.5f, h * 0.5f)
        )
    )

    // Pressed depression vignette
    if (pressDepth > 0.01f) {
        drawRect(
            color = DesignTokens.INNER_SHADOW.copy(alpha = pressDepth * 0.35f)
        )
    }
}

// ─────────────────────────────────────────────────────────
//  CELL OWNER ENUM — cleaner than raw list membership
// ─────────────────────────────────────────────────────────
private enum class CellOwner {
    EMPTY, PLAYER_ONE, PLAYER_TWO, PLAYER_THREE, PLAYER_FOUR
}