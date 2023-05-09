package com.itsfrz.tictactoe.minimax

interface GameBrain {
    public fun setHumanInput(gameState : ArrayList<ArrayList<Int>>,row : Int,col : Int) : Unit

    // Returns best optimal move for 3,4,5 matrix boards
    // For 3x3 matrix user can define difficulty
    public fun getOptimalMove(gameState : ArrayList<ArrayList<Int>>,boardSize : Int,difficulty : Int) : Move
    public fun setAITurn(value : Boolean) : Unit
}