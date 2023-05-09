package com.itsfrz.tictactoe.minimax

/*
* For 3x3 board difficulty level can be defined as easy, medium, hard corresponding to 0,1,2 values
* */

object IGameBrain : GameBrain {

    private var isAITurn : Boolean = false;
    override fun setHumanInput(gameState: ArrayList<ArrayList<Int>>,row : Int,col : Int) {
        gameState[row][col] = 1
    }

    override fun getOptimalMove(gameState: ArrayList<ArrayList<Int>>,boardSize : Int,difficulty : Int): Move {
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
                    val moveValue = minimax(gameState,0,isAITurn,boardSize,difficulty)
                    gameState[row][col] = 0

                    if (moveValue > bestValue){
                        bestValue = moveValue
                        bestMove = bestMove.copy(row = row, col = col)
                    }
                }
            }
        }
        println("BEST_MOVE ${bestMove}")
        return bestMove
    }

    private fun minimax(gameState: ArrayList<ArrayList<Int>>, depth: Int, isAITurn: Boolean,boardSize : Int,difficulty : Int): Int {

        val score : Int = evaluateScore(gameState,boardSize,difficulty)
        println("BEST_MOVE score ${score}")

        if (boardSize == 3 && difficulty == 0 && depth == 3){
            return score;
        }

        if (boardSize == 3 && difficulty == 1 && depth == 4){
            return score;
        }

        if (score == 10 || score == -10) {
            println("BEST_MOVE depth ${depth}")
            return score
        }

        if (!isMoveLeft(gameState))
            return 0



        if (!isAITurn){ // Maximizer
            var bestValue = -1000

            for (row in 0 until gameState.size){
                for (col in 0 until gameState[row].size){
                    if (gameState[row][col] == 0){
                        gameState[row][col] = 2
                        bestValue = Math.max(bestValue,minimax(gameState, depth+1, !isAITurn,boardSize,difficulty))
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
                        bestValue = Math.min(bestValue,minimax(gameState, depth+1, !isAITurn,boardSize,difficulty))
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

    private fun evaluateScore(gameState: ArrayList<ArrayList<Int>>,boardSize: Int,difficulty: Int): Int {
        when(boardSize){
            3 -> {
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
            }
            4 -> {
                val data = checkAllPositionsByBoard(gameState,4,difficulty)
                println("BEST_MOVE position data ${data}")
                return data
            }
            5 -> {
                return checkAllPositionsByBoard(gameState,5,difficulty)
            }
        }
        return 0
    }

    private fun checkAllPositionsByBoard(gameState: List<List<Int>>,boardSize : Int,difficulty: Int): Int {
        for (row in 0 until gameState[0].size){
            for (col in 0 until gameState.size){
                println("row $row, col $col")
                if (validateAllPositions(gameState,row,col,2,boardSize,difficulty))
                    return +10
                if (validateAllPositions(gameState,row,col,1,boardSize,difficulty))
                    return -10
            }
        }
        val points = (0..100).random()
        return if (points >= 80) +10 else -10
    }

    private fun validateAllPositions(gameState: List<List<Int>>, row: Int, col: Int, playerValue : Int, boardSize : Int,difficulty: Int): Boolean {
        var count = 0
        var tempRow = row
        var tempCol = col
        // Vertical Top
        if (boardSize - tempRow in 0..1) {
            println("Inside Vertical Top Condition")
            while (boardSize - tempRow != boardSize+1){
                println(gameState[tempRow][col])
                if (gameState[tempRow][col] == playerValue)
                    count++
                else count--
                if(count == boardSize) {
                    println("Pass From Vertical Top")
                    return true;
                }
                tempRow--
            }
        }

        if (difficulty != 1 && boardSize == 3){

            count = 0
            tempRow = row
            tempCol = col
            // Right Upper Diagonal
            if (boardSize - tempCol >= 3 && boardSize - tempRow in 0..1){
                println("Inside Right Upper Diagonal Condition")
                while (tempRow != -1 && tempCol != boardSize+1){
                    println(gameState[tempRow][tempCol])
                    if (gameState[tempRow][tempCol] == playerValue)
                        count++
                    else count--
                    if(count == boardSize) {
                        println("Pass Right Upper Diagonal")
                        return true;
                    }
                    tempRow--
                    tempCol++
                }
            }

            count = 0
            tempRow = row
            tempCol = col
            // Horizontal Right
            if (boardSize - tempCol > 2){
                println("Inside Horizontal Right Condition")
                while (tempCol != boardSize+1){
                    println(gameState[tempRow][tempCol])
                    if (gameState[tempRow][tempCol] == playerValue)
                        count++
                    else count--
                    if(count == boardSize) {
                        println("Pass Horizontal Right")
                        return true;
                    }
                    tempCol++
                }
            }

            if (difficulty != 2 && boardSize == 3){

                count = 0
                tempRow = row
                tempCol = col
                // Right Lower Diagonal
                if (boardSize - tempRow >= 3 && boardSize - tempCol >= 3){
                    println("Inside Right Lower Diagonal Condition")
                    while (tempRow != boardSize+1 && tempCol != boardSize+1){
                        println(gameState[tempRow][tempCol])
                        if (gameState[tempRow][tempCol] == playerValue)
                            count++
                        else count--
                        if(count == boardSize) {
                            println("Pass Right Lower Diagonal")
                            return true;
                        }
                        tempRow++
                        tempCol++
                    }
                }

                count = 0
                tempRow = row
                tempCol = col
                // Vertical Down
                if (boardSize - tempRow >= 3){
                    println("Inside Vertical Down Condition")
                    while (tempRow != boardSize+1){
                        println(gameState[tempRow][tempCol])
                        if (gameState[tempRow][tempCol] == playerValue)
                            count++
                        else count--
                        if(count == boardSize) {
                            println("Pass Vertical Down")
                            return true;
                        }
                        tempRow++
                    }
                }

            }


        }
        count = 0
        tempRow = row
        tempCol = col
        // Left Lower Diagonal
        if (boardSize - tempCol in 0..1){
            println("Inside Left Lower Diagonal Condition")
            while (tempRow != boardSize+1 && tempCol != -1){
                println(gameState[tempRow][tempCol])
                if (gameState[tempRow][tempCol] == playerValue)
                    count++
                else count--
                if(count == boardSize) {
                    println("Pass Left Lower Diagonal")
                    return true;
                }
                tempRow++
                tempCol--
            }
        }

        if (difficulty != 2 && boardSize == 3){
            count = 0
            tempRow = row
            tempCol = col
            // Horizontal Left
            if (boardSize - tempCol in 0..1){
                println("Inside Horizontal Left Condition")
                while (tempCol != -1){
                    println(gameState[tempRow][tempCol])
                    if (gameState[tempRow][tempCol] == playerValue)
                        count++
                    else count--
                    if(count == boardSize) {
                        println("Pass Horizontal Left")
                        return true;
                    }
                    tempCol--
                }
            }
        }

        if (difficulty != 1 && boardSize == 3){
            count = 0
            tempRow = row
            tempCol = col
            // Left Upper Diagonal
            if (boardSize - tempCol in 0..1 && boardSize - tempRow in 0..1){
                println("Inside Left Upper Diagonal Condition")
                while (tempRow != -1 && tempCol != -1){
                    println(gameState[tempRow][tempCol])
                    if (gameState[tempRow][tempCol] == playerValue)
                        count++
                    else count--
                    if(count == boardSize) {
                        println("Pass Left Upper Diagonal")
                        return true;
                    }
                    tempRow--
                    tempCol--
                }
            }
        }

        return false
    }

    override fun setAITurn(value: Boolean) {
        this.isAITurn = value;
    }
}