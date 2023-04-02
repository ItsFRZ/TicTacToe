package com.itsfrz.tictactoe.minimax

object IGameBrain : GameBrain {

    private var isAITurn : Boolean = false;

    override fun setHumanInput(gameState: ArrayList<ArrayList<Int>>,row : Int,col : Int) {
        gameState[row][col] = 1
    }

    override fun getOptimalMove(gameState: ArrayList<ArrayList<Int>>): Move {
        var bestValue = -1000
        var bestMove = Move()

        for (row in 0 until gameState.size){
            for (col in 0 until gameState[row].size){
                if (gameState[row][col] == 0){
                    if (isAITurn){
                        gameState[row][col] = 2;
                    }else{
                        gameState[row][col] = 1;
                    }
                    val moveValue = minimax(gameState,0,isAITurn)
                    gameState[row][col] = 0

                    if (moveValue > bestValue){
                        bestValue = moveValue
                        bestMove = bestMove.copy(row = row, col = col)
                    }
                }
            }
        }
        return bestMove
    }

    private fun minimax(gameState: ArrayList<ArrayList<Int>>, depth: Int, isAITurn: Boolean): Int {

        val score : Int = evaluateScore(gameState)

        if (score == 10 || score == -10)
            return score

        if (!isMoveLeft(gameState))
            return 0


        if (!isAITurn){ // Maximizer
            var bestValue = -1000

            for (row in 0 until gameState.size){
                for (col in 0 until gameState[row].size){
                    if (gameState[row][col] == 0){
                        gameState[row][col] = 2
                        bestValue = Math.max(bestValue,minimax(gameState, depth+1, !isAITurn))
                        gameState[row][col] = 0
                    }
                }
            }
            return bestValue
        }else{ // Minimizer
            var bestValue = 1000

            for (row in 0 until gameState.size){
                for (col in 0 until gameState[row].size){
                    if (gameState[row][col] == 0){
                        gameState[row][col] = 1
                        bestValue = Math.min(bestValue,minimax(gameState, depth+1, !isAITurn))
                        gameState[row][col] = 0
                    }
                }
            }
            return bestValue
        }
    }

    private fun isMoveLeft(gameState: ArrayList<ArrayList<Int>>): Boolean {
        for (row in 0 until gameState.size)
            for (col in 0 until gameState[row].size)
                if (gameState[row][col] == 0) return true
        return false
    }

    private fun evaluateScore(gameState: ArrayList<ArrayList<Int>>): Int {

        for (row in 0..2) {
            if (gameState[row][0] == gameState[row][1] &&
                gameState[row][1] == gameState[row][2]
            ) {
                if (gameState[row][0] == 2) return +10 else if (gameState[row][0] == 1) return -10
            }
        }

        for (col in 0..2) {
            if (gameState[0][col] == gameState[1][col] &&
                gameState[1][col] == gameState[2][col]
            ) {
                if (gameState[0][col] == 2) return +10 else if (gameState[0][col] == 1) return -10
            }
        }

        if (gameState[0][0] == gameState[1][1] && gameState[1][1] == gameState[2][2]) {
            if (gameState[0][0] == 2) return +10 else if (gameState[0][0] == 1) return -10
        }

        if (gameState[0][2] == gameState[1][1] && gameState[1][1] == gameState[2][0]) {
            if (gameState[0][2] == 2) return +10 else if (gameState[0][2] == 1) return -10
        }
        return 0


    }


    override fun setAITurn(value: Boolean) {
        this.isAITurn = value;
    }
}