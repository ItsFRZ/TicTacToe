package com.itsfrz.tictactoe.game.brain

import com.itsfrz.tictactoe.minimax.GameBrain
import com.itsfrz.tictactoe.minimax.Move
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * ═══════════════════════════════════════════════════════════════
 *  IGAMEBRAIN v2.1  —  Sage-Level Game Engine (Corrected)
 * ═══════════════════════════════════════════════════════════════
 *
 *  Architecture (PRESERVED & FIXED):
 *  ┌─────────────────────────────────────────────────────────┐
 *  │  ✓ Bitboard representation  (O(1) win detection)       │
 *  │  ✓ Negamax + Alpha-Beta pruning (proper perspective)   │
 *  │  ✓ Iterative deepening (time-bounded)                  │
 *  │  ✓ Transposition table (Zobrist hashing + ply-aware)   │
 *  │  ✓ Move ordering  (killer + history + threat + sage)   │
 *  │  ✓ Threat-space search  (5×5, 6×6 endgame)            │
 *  │  ✓ Static evaluation  (pattern + positional + sage)    │
 *  └─────────────────────────────────────────────────────────┘
 *
 *  CRITICAL FIXES APPLIED:
 *  ┌─────────────────────────────────────────────────────────┐
 *  │  • Fork detection: properly blocks HUMAN fork creation │
 *  │  • Evaluation: negamax-compatible perspective handling │
 *  │  • Null-move pruning: disabled (invalid in no-pass games)│
 *  │  • PV table: ply-based indexing (not depth)            │
 *  │  • Quiescence: proper sign propagation                 │
 *  │  • Move gen: proximity-restricted for large boards     │
 *  │  • Threat scoring: accurate open-line counting         │
 *  │  • Sage strategy: center control, fork creation, defense│
 *  └─────────────────────────────────────────────────────────┘
 *
 *  Board values:  AI = 2  |  Human = 1  |  Empty = 0
 *  Search framing: Negamax (score always from current player POV)
 * ═══════════════════════════════════════════════════════════════
 */
object IGameBrain : GameBrain {

    // ─────────────────────────────────────────────────────────
    // CONSTANTS & CONFIGURATION
    // ─────────────────────────────────────────────────────────

    private const val AI    = 2
    private const val HUMAN = 1
    private const val EMPTY = 0

    // Score constants - scaled for deeper search stability
    private const val WIN_SCORE  =  10_000_000
    private const val LOSE_SCORE = -10_000_000
    private const val DRAW_SCORE = 0
    private const val INF        =  Int.MAX_VALUE / 4

    // Time budget per move (ms)
    private val timeBudgetMs = mapOf(
        3 to 3_000L,
        4 to 5_000L,
        5 to 12_000L,
        6 to 15_000L,
        7 to 15_000L
    )

    // Win-line length per board size (m,n,k-game rules)
    private val winLength = mapOf(
        3 to 3,  // Classic: 3-in-a-row
        4 to 4,  // 4x4: 4-in-a-row
        5 to 4,  // 5x5: 4-in-a-row (standard Gomoku variant)
        6 to 5,  // 6x6: 5-in-a-row
        7 to 5   // 7x7: 5-in-a-row
    )

    // Max search depth per difficulty & board size
    private val maxDepthTable = mapOf(
        3 to mapOf(0 to 3, 1 to 6, 2 to 9),
        4 to mapOf(0 to 4, 1 to 6, 2 to 9),
        5 to mapOf(0 to 5, 1 to 8, 2 to 12),
        6 to mapOf(0 to 5, 1 to 8, 2 to 12),
        7 to mapOf(0 to 3, 1 to 6, 2 to 9)
    )

    // Late Move Reduction parameters
    private const val LMR_BASE_REDUCTION = 1
    private const val LMR_FULL_DEPTH_THRESHOLD = 3
    private const val LMR_MIN_DEPTH_FOR_REDUCTION = 4

    // Futility pruning margin (shallow depth only)
    private const val FUTILITY_MARGIN = 1500

    // Proximity radius for move generation on large boards
    private const val PROXIMITY_RADIUS = 2

    // ─────────────────────────────────────────────────────────
    // STATE MANAGEMENT
    // ─────────────────────────────────────────────────────────

    @Volatile private var isAITurn = false
    @Volatile private var searchStartTime = 0L
    @Volatile private var nodesSearched = 0L

    // Principal Variation stack - indexed by PLY (distance from root)
    private val pvTable = Array(64) { Array(64) { Move(0, 0) } }
    private val pvLength = IntArray(64)

    // ─────────────────────────────────────────────────────────
    // TRANSPOSITION TABLE (Enhanced)
    // ─────────────────────────────────────────────────────────

    private data class TTEntry(
        val score: Int,
        val depth: Int,
        val flag: Byte,      // 0=EXACT, 1=LOWER_BOUND, 2=UPPER_BOUND
        val bestMove: Move?,
        val ply: Int         // Store ply for PV reconstruction
    )

    private val transpositionTable = ConcurrentHashMap<Long, TTEntry>(1 shl 22)

    // ─────────────────────────────────────────────────────────
    // ZOBRIST HASHING
    // ─────────────────────────────────────────────────────────

    private val zobristTable = run {
        val rng = Random(0xDEADBEEFCAFE)
        Array(64) { LongArray(3) { rng.nextLong() } }
    }
    private var zobristHash = 0L

    private fun zobristIndex(row: Int, col: Int, boardSize: Int) = row * boardSize + col

    // ─────────────────────────────────────────────────────────
    // MOVE ORDERING HEURISTICS
    // ─────────────────────────────────────────────────────────

    private val killerMoves = Array(64) { arrayOfNulls<Move>(2) }
    private val historyTable = HashMap<Int, Int>(1024)
    private val counterMoveTable = HashMap<Int, Move>(512)

    private fun historyKey(row: Int, col: Int, boardSize: Int) = row * boardSize + col

    private fun updateKiller(move: Move, depth: Int) {
        val km = killerMoves[depth.coerceAtMost(63)]
        if (km[0] != move) {
            km[1] = km[0]
            km[0] = move
        }
    }

    private fun updateHistory(row: Int, col: Int, boardSize: Int, depth: Int, bonus: Int) {
        val key = historyKey(row, col, boardSize)
        val current = historyTable[key] ?: 0
        historyTable[key] = (current + bonus * depth * depth).coerceAtMost(1_000_000)
    }

    private fun updateCounterMove(opponentMove: Move, response: Move, boardSize: Int) {
        val key = historyKey(opponentMove.row, opponentMove.col, boardSize)
        counterMoveTable[key] = response
    }

    // ─────────────────────────────────────────────────────────
    // BITBOARD ENGINE
    // ─────────────────────────────────────────────────────────

    private data class Bitboard(
        val ai: Long,
        val human: Long,
        val boardSize: Int
    ) {
        fun cellIndex(row: Int, col: Int) = row * boardSize + col

        fun isEmpty(row: Int, col: Int): Boolean =
            (ai or human) and (1L shl cellIndex(row, col)) == 0L

        fun isOccupied(row: Int, col: Int): Boolean =
            (ai or human) and (1L shl cellIndex(row, col)) != 0L

        fun isAI(row: Int, col: Int): Boolean =
            ai and (1L shl cellIndex(row, col)) != 0L

        fun isHuman(row: Int, col: Int): Boolean =
            human and (1L shl cellIndex(row, col)) != 0L

        fun isFull(): Boolean =
            (ai or human).countOneBits() == boardSize * boardSize

        fun place(row: Int, col: Int, player: Int): Bitboard {
            val bit = 1L shl cellIndex(row, col)
            return if (player == AI)
                copy(ai = ai or bit)
            else
                copy(human = human or bit)
        }

        fun remove(row: Int, col: Int): Bitboard {
            val bit = (1L shl cellIndex(row, col)).inv()
            return copy(ai = ai and bit, human = human and bit)
        }

        fun playerBits(player: Int) = if (player == AI) ai else human

        fun emptyCells(): List<Pair<Int, Int>> {
            val cells = mutableListOf<Pair<Int, Int>>()
            for (r in 0 until boardSize) {
                for (c in 0 until boardSize) {
                    if (isEmpty(r, c)) cells += Pair(r, c)
                }
            }
            return cells
        }

        fun cellsNearOccupied(radius: Int): List<Pair<Int, Int>> {
            val cells = mutableListOf<Pair<Int, Int>>()
            val occupied = ai or human
            for (r in 0 until boardSize) {
                for (c in 0 until boardSize) {
                    if (!isEmpty(r, c)) continue
                    // Check if within radius of any occupied cell
                    var near = false
                    for (dr in -radius..radius) {
                        for (dc in -radius..radius) {
                            val nr = r + dr; val nc = c + dc
                            if (nr in 0 until boardSize && nc in 0 until boardSize) {
                                if (occupied and (1L shl cellIndex(nr, nc)) != 0L) {
                                    near = true; break
                                }
                            }
                        }
                        if (near) break
                    }
                    if (near || (ai or human) == 0L) cells += Pair(r, c)
                }
            }
            return cells.ifEmpty { emptyCells() }
        }
    }

    // Win mask caching
    private data class BoardConfig(val size: Int, val winLen: Int)
    private val winMasksCache = ConcurrentHashMap<BoardConfig, List<Long>>()

    private fun getWinMasks(boardSize: Int, wl: Int): List<Long> {
        val cfg = BoardConfig(boardSize, wl)
        return winMasksCache.getOrPut(cfg) { buildWinMasks(boardSize, wl) }
    }

    private fun buildWinMasks(n: Int, wl: Int): List<Long> {
        val masks = mutableListOf<Long>()

        // Rows
        for (r in 0 until n) {
            for (c in 0..n - wl) {
                var m = 0L
                for (k in 0 until wl) m = m or (1L shl (r * n + c + k))
                masks += m
            }
        }

        // Cols
        for (c in 0 until n) {
            for (r in 0..n - wl) {
                var m = 0L
                for (k in 0 until wl) m = m or (1L shl ((r + k) * n + c))
                masks += m
            }
        }

        // Diag ↘
        for (r in 0..n - wl) {
            for (c in 0..n - wl) {
                var m = 0L
                for (k in 0 until wl) m = m or (1L shl ((r + k) * n + (c + k)))
                masks += m
            }
        }

        // Diag ↙
        for (r in 0..n - wl) {
            for (c in wl - 1 until n) {
                var m = 0L
                for (k in 0 until wl) m = m or (1L shl ((r + k) * n + (c - k)))
                masks += m
            }
        }

        return masks
    }

    private fun checkWin(bits: Long, masks: List<Long>): Boolean {
        for (mask in masks) if (bits and mask == mask) return true
        return false
    }

    // ─────────────────────────────────────────────────────────
    // INTERFACE IMPLEMENTATION
    // ─────────────────────────────────────────────────────────

    override fun setHumanInput(gameState: ArrayList<ArrayList<Int>>, row: Int, col: Int) {
        gameState[row][col] = HUMAN
    }

    override fun setAITurn(value: Boolean) {
        isAITurn = value
    }

    override fun reset() {
        isAITurn = false
        transpositionTable.clear()
        killerMoves.forEach { it.fill(null) }
        historyTable.clear()
        counterMoveTable.clear()
        zobristHash = 0L
        pvLength.fill(0)
        nodesSearched = 0
    }

    override fun getOptimalMove(
        gameState: ArrayList<ArrayList<Int>>,
        boardSize: Int,
        difficulty: Int
    ): Move {
        if (boardSize == 3 && difficulty <= 1){
            return getQuickMove(gameState)
        }
        val wl      = winLength[boardSize] ?: 3
        val masks   = getWinMasks(boardSize, wl)
        val maxD    = maxDepthTable[boardSize]?.get(difficulty) ?: 4
        val budget  = timeBudgetMs[boardSize] ?: 8_000L

        var board = buildBitboard(gameState, boardSize)
        zobristHash = computeZobrist(board, boardSize)

        // Immediate tactical checks
        immediateMove(board, boardSize, masks, wl)?.let { return it }

        // Iterative Deepening with Aspiration Windows
        var bestMove = Move(boardSize / 2, boardSize / 2)
        var bestScore = LOSE_SCORE
        var lastScore = 0

        searchStartTime = System.currentTimeMillis()
        val deadline = searchStartTime + budget
        var depth = 1

        while (depth <= maxD && System.currentTimeMillis() < deadline) {
            var alpha = if (depth == 1) -INF else lastScore - 500
            var beta  = if (depth == 1)  INF else lastScore + 500

            val (move, score) = iterationRoot(
                board = board, boardSize = boardSize, ply = 0, depth = depth,
                masks = masks, wl = wl, alpha = alpha, beta = beta,
                deadline = deadline, difficulty = difficulty
            )

            if (score <= alpha || score >= beta) {
                alpha = -INF; beta = INF
                val (retryMove, retryScore) = iterationRoot(
                    board = board, boardSize = boardSize, ply = 0, depth = depth,
                    masks = masks, wl = wl, alpha = alpha, beta = beta,
                    deadline = deadline, difficulty = difficulty
                )
                if (retryMove != null) {
                    bestMove = retryMove; bestScore = retryScore; lastScore = retryScore
                }
            } else if (move != null) {
                bestMove = move; bestScore = score; lastScore = score
            }

            if (bestScore >= WIN_SCORE - depth || bestScore <= LOSE_SCORE + depth) break
            if (System.currentTimeMillis() >= deadline - 200) break
            depth++
        }

        return bestMove
    }

    private fun getQuickMove(gameState: ArrayList<ArrayList<Int>>) : Move{
        println("BOARD3 ::getQuickMove :: Game State ${gameState} ")
        val moves =mutableListOf<Move>()
        for (row in 0 until gameState.size){
            for (col in 0 until gameState[row].size){
                if (gameState[row][col] == 0){
                    moves.add(Move(row,col))
                }
            }
        }
        return moves.random()
    }

    override fun getFirstMove(boardSize: Int): Move {
        return when(boardSize) {
            3 -> {Move(1,1)}
            4 -> {Move(2,2)}
            5 -> {Move(2,2)}
            6 -> {Move(3,2)}
            else -> {Move(2,3)}
        }
    }

    // ─────────────────────────────────────────────────────────
    // IMMEDIATE TACTICS: Win/Block + Fork Prevention (FIXED)
    // ─────────────────────────────────────────────────────────

    private fun immediateMove(
        board: Bitboard,
        boardSize: Int,
        masks: List<Long>,
        winLen: Int
    ): Move? {
        // 1. AI wins immediately?
        for (r in 0 until boardSize) for (c in 0 until boardSize) {
            if (!board.isEmpty(r, c)) continue
            if (checkWin(board.place(r, c, AI).ai, masks)) return Move(r, c)
        }

        // 2. Block human from winning immediately?
        for (r in 0 until boardSize) for (c in 0 until boardSize) {
            if (!board.isEmpty(r, c)) continue
            if (checkWin(board.place(r, c, HUMAN).human, masks)) return Move(r, c)
        }

        if (boardSize >= 5) {
            // 3. Create a fork for AI (proactive — attack takes priority over defense)
            val forkCreate = createAIFork(board, boardSize, masks, winLen)
            if (forkCreate != null) return forkCreate

            // 4. Block human from creating a fork
            val forkBlock = preventHumanFork(board, boardSize, masks, winLen)
            if (forkBlock != null) return forkBlock
        }

        return null
    }

    // FIXED: Prevent HUMAN from creating a fork (two+ winning threats)
    private fun preventHumanFork(
        board: Bitboard,
        boardSize: Int,
        masks: List<Long>,
        winLen: Int
    ): Move? {
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                if (!board.isEmpty(r, c)) continue
                // Simulate HUMAN playing here - would they create a fork?
                val afterHuman = board.place(r, c, HUMAN)
                if (countWinningThreats(afterHuman, boardSize, HUMAN, masks, winLen) >= 2) {
                    // Block this fork by playing here ourselves
                    return Move(r, c)
                }
            }
        }
        return null
    }

    // Sage strategy: Create a fork for AI (two+ winning threats)
    private fun createAIFork(
        board: Bitboard,
        boardSize: Int,
        masks: List<Long>,
        winLen: Int
    ): Move? {
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                if (!board.isEmpty(r, c)) continue
                val afterAI = board.place(r, c, AI)
                if (countWinningThreats(afterAI, boardSize, AI, masks, winLen) >= 2) {
                    return Move(r, c)
                }
            }
        }
        return null
    }

    // Count how many moves would let player win immediately
    private fun countWinningThreats(
        board: Bitboard,
        boardSize: Int,
        player: Int,
        masks: List<Long>,
        winLen: Int
    ): Int {
        var count = 0
        val bits = board.playerBits(player)
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                if (!board.isEmpty(r, c)) continue
                val testBits = bits or (1L shl board.cellIndex(r, c))
                if (checkWin(testBits, masks)) count++
            }
        }
        return count
    }

    // ─────────────────────────────────────────────────────────
    // ROOT ITERATION (PLY-aware PV tracking)
    // ─────────────────────────────────────────────────────────

    private fun iterationRoot(
        board: Bitboard,
        boardSize: Int,
        ply: Int,
        depth: Int,
        masks: List<Long>,
        wl: Int,
        alpha: Int,
        beta: Int,
        deadline: Long,
        difficulty: Int
    ): Pair<Move?, Int> {
        var alphaLocal = alpha
        var bestMove: Move? = null
        var bestScore = -INF

        val moves = generateMoves(board, boardSize, ply, AI, true)

        for ((idx, move) in moves.withIndex()) {
            val (r, c) = move
            val next = board.place(r, c, AI)
            zobristHash = zobristHash xor zobristTable[board.cellIndex(r, c)][AI]

            val score = if (idx == 0) {
                -negamax(
                    board = next, boardSize = boardSize, ply = ply + 1, depth = depth - 1,
                    alpha = -beta, beta = -alphaLocal, player = HUMAN,
                    masks = masks, wl = wl, deadline = deadline, difficulty = difficulty
                )
            } else {
                val nullScore = -negamax(
                    board = next, boardSize = boardSize, ply = ply + 1, depth = depth - 1,
                    alpha = -alphaLocal - 1, beta = -alphaLocal, player = HUMAN,
                    masks = masks, wl = wl, deadline = deadline, difficulty = difficulty
                )
                if (nullScore > alphaLocal && nullScore < beta) {
                    -negamax(
                        board = next, boardSize = boardSize, ply = ply + 1, depth = depth - 1,
                        alpha = -beta, beta = -alphaLocal, player = HUMAN,
                        masks = masks, wl = wl, deadline = deadline, difficulty = difficulty
                    )
                } else nullScore
            }

            zobristHash = zobristHash xor zobristTable[board.cellIndex(r, c)][AI]

            if (score > bestScore) {
                bestScore = score
                bestMove = move
                // PLY-based PV update (FIXED)
                pvTable[ply][0] = move
                pvLength[ply] = 1
                if (depth > 1 && pvLength[ply + 1] > 0) {
                    for (i in 1..pvLength[ply + 1]) {
                        pvTable[ply][i] = pvTable[ply + 1][i - 1]
                    }
                    pvLength[ply] = 1 + pvLength[ply + 1]
                }
            }

            alphaLocal = max(alphaLocal, score)
            if (alphaLocal >= beta) break
        }

        return Pair(bestMove, bestScore)
    }

    // ─────────────────────────────────────────────────────────
    // NEGAMAX + ALPHA-BETA + TT (FIXED PERSPECTIVE & PRUNING)
    // ─────────────────────────────────────────────────────────

    private fun negamax(
        board: Bitboard,
        boardSize: Int,
        ply: Int,
        depth: Int,
        alpha: Int,
        beta: Int,
        player: Int,
        masks: List<Long>,
        wl: Int,
        deadline: Long,
        difficulty: Int
    ): Int {
        nodesSearched++

        if (nodesSearched % 1024 == 0L && System.currentTimeMillis() > deadline) {
            return evaluate(board, boardSize, player, masks, wl) // Return static eval on timeout
        }

        val opponent = if (player == AI) HUMAN else AI

        // Terminal checks: did the PREVIOUS player (opponent) just win?
        if (checkWin(board.playerBits(opponent), masks)) {
            return -WIN_SCORE - ply  // Prefer faster wins, slower losses
        }
        if (board.isFull()) return DRAW_SCORE

        // TT lookup (PLY-aware)
        val ttKey = zobristHash xor (player.toLong() shl 62)
        val ttEntry = transpositionTable[ttKey]

        var alphaLocal = alpha
        if (ttEntry != null && ttEntry.depth >= depth) {
            when (ttEntry.flag.toInt()) {
                0 -> return ttEntry.score
                1 -> alphaLocal = max(alphaLocal, ttEntry.score)
                2 -> if (ttEntry.score <= alpha) return ttEntry.score
            }
            if (alphaLocal >= beta) return ttEntry.score
        }

        // NULL-MOVE PRUNING: DISABLED for tic-tac-toe variants
        // Passing is not a legal move - null-move can cause serious errors

        // Quiescence at leaf
        if (depth == 0) {
            return quiesce(board, boardSize, alphaLocal, beta, player, masks, wl, ply)
        }

        // Move generation with ordering
        val moves = generateMoves(board, boardSize, ply, player, false)
        var best = -INF
        val alphaOrig = alphaLocal
        var searched = 0

        for ((idx, move) in moves.withIndex()) {
            val (r, c) = move
            val next = board.place(r, c, player)
            val zIdx = board.cellIndex(r, c)

            zobristHash = zobristHash xor zobristTable[zIdx][player]

            // LMR
            var reduction = 0
            if (depth >= LMR_MIN_DEPTH_FOR_REDUCTION &&
                searched >= LMR_FULL_DEPTH_THRESHOLD &&
                !isCaptureOrThreat(board, r, c, player, masks, wl)) {
                reduction = LMR_BASE_REDUCTION +
                        (depth / 4).coerceAtMost(2) +
                        (searched / 8).coerceAtMost(2)
            }
            val newDepth = (depth - 1 - reduction).coerceAtLeast(0)

            // PVS
            val score = if (searched == 0) {
                -negamax(
                    board = next, boardSize = boardSize, ply = ply + 1, depth = newDepth,
                    alpha = -beta, beta = -alphaLocal, player = opponent,
                    masks = masks, wl = wl, deadline = deadline, difficulty = difficulty
                )
            } else {
                val nullScore = -negamax(
                    board = next, boardSize = boardSize, ply = ply + 1, depth = newDepth,
                    alpha = -alphaLocal - 1, beta = -alphaLocal, player = opponent,
                    masks = masks, wl = wl, deadline = deadline, difficulty = difficulty
                )
                if (nullScore > alphaLocal && nullScore < beta) {
                    -negamax(
                        board = next, boardSize = boardSize, ply = ply + 1, depth = newDepth,
                        alpha = -beta, beta = -alphaLocal, player = opponent,
                        masks = masks, wl = wl, deadline = deadline, difficulty = difficulty
                    )
                } else nullScore
            }

            zobristHash = zobristHash xor zobristTable[zIdx][player]

            if (score > best) {
                best = score
                // PLY-based PV update (FIXED)
                pvTable[ply][0] = move
                for (i in 1 until pvLength[ply + 1]) {
                    pvTable[ply][i] = pvTable[ply + 1][i - 1]
                }
                pvLength[ply] = 1 + pvLength[ply + 1]
            }

            alphaLocal = max(alphaLocal, score)
            searched++

            if (alphaLocal >= beta) {
                updateKiller(move, depth)
                updateHistory(r, c, boardSize, depth, bonus = depth * depth)
                if (ply > 0 && pvLength[ply - 1] > 0) {
                    val oppMove = pvTable[ply - 1][0]
                    updateCounterMove(oppMove, move, boardSize)
                }
                break
            }

            // Futility pruning
            if (depth <= 3 && best > -WIN_SCORE + ply) {
                val futilityBound = best + FUTILITY_MARGIN + depth * 100
                if (futilityBound <= alphaLocal) break
            }
        }

        // Store in TT (PLY-aware)
        val flag: Byte = when {
            best <= alphaOrig -> 2
            best >= beta -> 1
            else -> 0
        }
        transpositionTable[ttKey] = TTEntry(
            best, depth, flag,
            if (best > alphaOrig) pvTable[ply][0] else null,
            ply
        )

        return best
    }

    // ─────────────────────────────────────────────────────────
    // QUIESCENCE SEARCH (FIXED SIGN PROPAGATION)
    // ─────────────────────────────────────────────────────────

    private fun quiesce(
        board: Bitboard,
        boardSize: Int,
        alpha: Int,
        beta: Int,
        player: Int,
        masks: List<Long>,
        wl: Int,
        ply: Int
    ): Int {
        // Evaluation is from CURRENT player's perspective (negamax-compatible)
        val standPat = evaluate(board, boardSize, player, masks, wl)

        if (standPat >= beta) return beta
        val alphaLocal = max(alpha, standPat)

        if (boardSize <= 3) return standPat

        val threatMoves = generateThreatMoves(board, boardSize, masks, wl, player)
        var best = standPat

        for (move in threatMoves) {
            val (r, c) = move
            val next = board.place(r, c, player)
            // Recurse with flipped player - score returned is from opponent's POV, so negate
            val score = -quiesce(
                next, boardSize, -beta, -alphaLocal,
                flipPlayer(player), masks, wl, ply + 1
            )
            if (score > best) best = score
            if (best >= beta) return beta
        }

        return best
    }

    // ─────────────────────────────────────────────────────────
    // MOVE GENERATION — Sage-Level Ordering
    // ─────────────────────────────────────────────────────────

    private fun generateMoves(
        board: Bitboard,
        boardSize: Int,
        ply: Int,
        player: Int,
        isRoot: Boolean = false
    ): List<Move> {
        val center = boardSize / 2.0
        val candidates = mutableListOf<Triple<Move, Int, Boolean>>()

        // TT move for ordering — use correct player key
        val ttKey = zobristHash xor (player.toLong() shl 62)
        val ttMove = transpositionTable[ttKey]?.bestMove

        // PROXIMITY RESTRICTION for large boards (FIXED)
        val emptyCells = if (boardSize >= 5 && (board.ai or board.human).countOneBits() > 4) {
            board.cellsNearOccupied(PROXIMITY_RADIUS)
        } else {
            board.emptyCells()
        }

        for ((r, c) in emptyCells) {
            var score = 0
            val move = Move(r, c)
            var isThreat = false

            // TT bonus
            if (ttMove != null && ttMove.row == r && ttMove.col == c) score += 50_000

            // Killer moves
            val km = killerMoves[ply.coerceAtMost(63)]
            if (km[0] == move) score += 20_000
            else if (km[1] == move) score += 15_000

            // Counter-move
            if (ply > 0 && pvLength[ply - 1] > 0) {
                val oppMove = pvTable[ply - 1][0]
                val counterKey = historyKey(oppMove.row, oppMove.col, boardSize)
                if (counterMoveTable[counterKey] == move) score += 12_000
            }

            // History
            score += historyTable[historyKey(r, c, boardSize)] ?: 0

            // Threat detection — score from current player's perspective
            val threatBonus = threatScore(board, r, c, boardSize, player)
            val oppThreatBonus = threatScore(board, r, c, boardSize, flipPlayer(player))
            if (threatBonus > 0 || oppThreatBonus > 0) {
                score += (threatBonus + oppThreatBonus) * 100
                isThreat = true
            }

            // SAGE STRATEGY: Center control (stronger weight)
            val dist = abs(r - center) + abs(c - center)
            val centerBonus = (boardSize * 5 - dist * 5).toInt().coerceAtLeast(0)
            score += centerBonus * (if (boardSize >= 5) 3 else 1)

            // SAGE STRATEGY: Corner preference for larger boards (strategic positioning)
            if (boardSize >= 5 && ((r == 0 || r == boardSize - 1) && (c == 0 || c == boardSize - 1))) {
                score += 300
            }

            // Fork potential
            if (boardSize >= 5) {
                val forkBonus = forkPotential(board, r, c, boardSize, player)
                score += forkBonus * 150
            }

            // Opening book preference: first move -> center
            if ((board.ai or board.human) == 0L && r == center.toInt() && c == center.toInt()) {
                score += 100_000
            }

            candidates += Triple(move, score, isThreat)
        }

        return candidates.sortedWith(
            compareByDescending<Triple<Move, Int, Boolean>> { it.second }
                .thenByDescending { it.third }
        ).map { it.first }
    }

    private fun generateThreatMoves(
        board: Bitboard,
        boardSize: Int,
        masks: List<Long>,
        wl: Int,
        player: Int
    ): List<Move> {
        val moves = mutableListOf<Move>()
        val opponent = flipPlayer(player)

        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                if (!board.isEmpty(r, c)) continue
                val createsThreat = threatScore(board, r, c, boardSize, player) > 0
                val blocksThreat = threatScore(board, r, c, boardSize, opponent) > 0
                if (createsThreat || blocksThreat) moves += Move(r, c)
            }
        }

        return moves.ifEmpty {
            if (boardSize >= 5) board.cellsNearOccupied(PROXIMITY_RADIUS).map { Move(it.first, it.second) }
            else board.emptyCells().map { Move(it.first, it.second) }
        }
    }

    // IMPROVED threat scoring with open-line counting
    private fun threatScore(
        board: Bitboard,
        row: Int, col: Int,
        boardSize: Int,
        player: Int
    ): Int {
        var score = 0
        val n = boardSize
        val bits = board.playerBits(player)
        val oppBits = board.playerBits(flipPlayer(player))
        val directions = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(1, -1))

        for ((dr, dc) in directions) {
            var count = 1
            var openEnds = 0
            var blocked = false

            // Forward
            var r = row + dr; var c = col + dc
            while (r in 0 until n && c in 0 until n && !blocked) {
                val bit = 1L shl (r * n + c)
                when {
                    bits and bit != 0L -> { count++; r += dr; c += dc }
                    oppBits and bit != 0L -> blocked = true
                    else -> { openEnds++; break }
                }
            }
            if (blocked) continue

            // Backward
            r = row - dr; c = col - dc; blocked = false
            while (r in 0 until n && c in 0 until n && !blocked) {
                val bit = 1L shl (r * n + c)
                when {
                    bits and bit != 0L -> { count++; r -= dr; c -= dc }
                    oppBits and bit != 0L -> blocked = true
                    else -> { openEnds++; break }
                }
            }

            if (!blocked && count >= 2) {
                score += when {
                    count >= 4 && openEnds >= 1 -> 2000  // Immediate win threat
                    count >= 3 && openEnds >= 2 -> 800   // Strong open threat
                    count >= 3 && openEnds >= 1 -> 400   // One-sided threat
                    count >= 2 && openEnds >= 2 -> 150   // Developing line
                    else -> 0
                }
            }
        }
        return score
    }

    // Fork potential: count independent directions where placing here creates a near-win line
    private fun forkPotential(
        board: Bitboard,
        row: Int, col: Int,
        boardSize: Int,
        player: Int
    ): Int {
        if (boardSize < 5) return 0
        val wl = winLength[boardSize] ?: 4
        val bits = board.playerBits(player)
        val oppBits = board.playerBits(flipPlayer(player))
        val n = boardSize
        val directions = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(1, -1))
        var forkLines = 0

        for ((dr, dc) in directions) {
            var count = 1  // count the cell being placed
            var openEnds = 0

            // Forward
            var r = row + dr; var c = col + dc
            var blocked = false
            while (r in 0 until n && c in 0 until n) {
                val bit = 1L shl (r * n + c)
                when {
                    bits and bit != 0L -> { count++; r += dr; c += dc }
                    oppBits and bit != 0L -> { blocked = true; break }
                    else -> { openEnds++; break }
                }
            }
            if (!blocked && r !in 0 until n || c !in 0 until n) { /* wall — no open end */ }

            // Backward
            r = row - dr; c = col - dc; blocked = false
            while (r in 0 until n && c in 0 until n) {
                val bit = 1L shl (r * n + c)
                when {
                    bits and bit != 0L -> { count++; r -= dr; c -= dc }
                    oppBits and bit != 0L -> { blocked = true; break }
                    else -> { openEnds++; break }
                }
            }

            // A near-win line: count >= wl-1 and at least one open end
            if (count >= wl - 1 && openEnds >= 1) forkLines++
        }

        return when {
            forkLines >= 2 -> 12  // True fork: two simultaneous near-win lines
            forkLines == 1 -> 3
            else -> 0
        }
    }

    private fun isCaptureOrThreat(
        board: Bitboard,
        row: Int, col: Int,
        player: Int,
        masks: List<Long>,
        wl: Int
    ): Boolean {
        val n = board.boardSize
        var adjacentOpponent = 0
        for (dr in -1..1) for (dc in -1..1) {
            if (dr == 0 && dc == 0) continue
            val nr = row + dr; val nc = col + dc
            if (nr in 0 until n && nc in 0 until n) {
                if (board.playerBits(flipPlayer(player)) and (1L shl (nr * n + nc)) != 0L) {
                    adjacentOpponent++
                }
            }
        }
        val threat = threatScore(board, row, col, n, player) > 0
        return adjacentOpponent >= 2 || threat
    }

    // ─────────────────────────────────────────────────────────
    // STATIC EVALUATION (FIXED: Negamax-Compatible Perspective)
    // Returns score from CURRENT player's perspective
    // ─────────────────────────────────────────────────────────

    private val patternScore = intArrayOf(0, 10, 100, 1000, 10000, WIN_SCORE)

    private fun evaluate(
        board: Bitboard,
        boardSize: Int,
        player: Int,  // CURRENT player (perspective for negamax)
        masks: List<Long>,
        wl: Int
    ): Int {
        val opponent = flipPlayer(player)
        var score = 0

        // Pattern scoring per win-line mask
        for (mask in masks) {
            val pBits = board.playerBits(player) and mask
            val oBits = board.playerBits(opponent) and mask

            if (oBits != 0L && pBits != 0L) continue  // Mixed = neutral

            if (pBits != 0L) {
                val rawCount = pBits.countOneBits()
                val count = rawCount.coerceAtMost(patternScore.size - 1)
                val base = patternScore[count]
                score += if (rawCount >= wl - 1) base * 2 else base
            }

            if (oBits != 0L) {
                val rawCount = oBits.countOneBits()
                val count = rawCount.coerceAtMost(patternScore.size - 1)
                val base = patternScore[count]
                score -= if (rawCount >= wl - 1) base * 2 else base
            }
        }

        // Positional: Center control (Sage strategy)
        val center = boardSize / 2
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val dist = abs(r - center) + abs(c - center)
                val weight = (boardSize - dist).coerceAtLeast(1)
                if (board.isOccupied(r, c)) {
                    val isPlayer = if (board.isAI(r, c)) AI else HUMAN
                    score += if (isPlayer == player) weight * 8 else -weight * 8
                }
            }
        }

        // Mobility: both players share the same empty cells — no asymmetric term needed.
        // Instead, reward having more near-neighbor open cells adjacent to own pieces (local mobility).
        if (boardSize >= 4) {
            val n = boardSize
            val playerBits = board.playerBits(player)
            val oppBits = board.playerBits(opponent)
            var localMobility = 0
            for (r in 0 until n) for (c in 0 until n) {
                if (!board.isEmpty(r, c)) continue
                var adjPlayer = 0; var adjOpp = 0
                for (dr in -1..1) for (dc in -1..1) {
                    if (dr == 0 && dc == 0) continue
                    val nr = r + dr; val nc = c + dc
                    if (nr in 0 until n && nc in 0 until n) {
                        val bit = 1L shl (nr * n + nc)
                        if (playerBits and bit != 0L) adjPlayer++
                        if (oppBits and bit != 0L) adjOpp++
                    }
                }
                localMobility += adjPlayer - adjOpp
            }
            score += localMobility * 3
        }

        // Connected pieces bonus (encourages building lines)
        if (boardSize >= 5) {
            score += connectedPiecesBonus(board, boardSize, player) * 5
        }

        // Threat balance (dynamic) — weighted heavier on large boards where threats dominate
        val threatWeight = if (boardSize >= 5) 600 else 300
        val playerThreats = countActiveThreats(board, boardSize, player, wl)
        val oppThreats = countActiveThreats(board, boardSize, opponent, wl)
        score += (playerThreats - oppThreats) * threatWeight

        // Return score from CURRENT player's perspective (FIXED - no final sign flip)
        return score
    }

    private fun connectedPiecesBonus(board: Bitboard, boardSize: Int, player: Int): Int {
        var bonus = 0
        val bits = board.playerBits(player)
        val n = boardSize
        for (r in 0 until n) {
            for (c in 0 until n) {
                if (bits and (1L shl (r * n + c)) == 0L) continue
                var adjacent = 0
                for (dr in -1..1) for (dc in -1..1) {
                    if (dr == 0 && dc == 0) continue
                    val nr = r + dr; val nc = c + dc
                    if (nr in 0 until n && nc in 0 until n) {
                        if (bits and (1L shl (nr * n + nc)) != 0L) adjacent++
                    }
                }
                bonus += adjacent
            }
        }
        return bonus
    }

    // Count active threats: lines of exactly wl cells with (wl-1) player pieces + 1 empty, not blocked by opponent
    private fun countActiveThreats(board: Bitboard, boardSize: Int, player: Int, wl: Int): Int {
        var threats = 0
        val bits = board.playerBits(player)
        val oppBits = board.playerBits(flipPlayer(player))
        val n = boardSize
        // Only canonical directions to avoid double-counting: right, down, diag-↘, diag-↙
        val directions = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(1, -1))

        for (r in 0 until n) {
            for (c in 0 until n) {
                for ((dr, dc) in directions) {
                    // Check if a window of size `wl` starting at (r,c) is a threat
                    val endR = r + dr * (wl - 1)
                    val endC = c + dc * (wl - 1)
                    if (endR !in 0 until n || endC !in 0 until n) continue

                    var playerCount = 0
                    var emptyCount = 0
                    var blocked = false
                    for (k in 0 until wl) {
                        val nr = r + dr * k; val nc = c + dc * k
                        val bit = 1L shl (nr * n + nc)
                        when {
                            bits and bit != 0L -> playerCount++
                            oppBits and bit != 0L -> { blocked = true; break }
                            else -> emptyCount++
                        }
                    }
                    if (!blocked && playerCount == wl - 1 && emptyCount == 1) threats++
                }
            }
        }
        return threats
    }

    // ─────────────────────────────────────────────────────────
    // UTILITIES
    // ─────────────────────────────────────────────────────────

    private fun flipPlayer(p: Int) = if (p == AI) HUMAN else AI

    private fun buildBitboard(gameState: ArrayList<ArrayList<Int>>, n: Int): Bitboard {
        var ai = 0L; var human = 0L
        for (r in 0 until n) {
            for (c in 0 until n) {
                val bit = 1L shl (r * n + c)
                when (gameState[r][c]) {
                    AI -> ai = ai or bit
                    HUMAN -> human = human or bit
                }
            }
        }
        return Bitboard(ai, human, n)
    }

    private fun computeZobrist(board: Bitboard, n: Int): Long {
        var hash = 0L
        for (r in 0 until n) {
            for (c in 0 until n) {
                val idx = r * n + c
                when {
                    board.isAI(r, c) -> hash = hash xor zobristTable[idx][AI]
                    board.isHuman(r, c) -> hash = hash xor zobristTable[idx][HUMAN]
                }
            }
        }
        return hash
    }

    // Legacy compatibility
    private operator fun Move.component1() = row
    private operator fun Move.component2() = col
}