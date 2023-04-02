package com.itsfrz.tictactoe.minimax

interface GameBrain {
    public fun setHumanInput(gameState : ArrayList<ArrayList<Int>>,row : Int,col : Int) : Unit
    public fun getOptimalMove(gameState : ArrayList<ArrayList<Int>>) : Move
    public fun setAITurn(value : Boolean) : Unit
}