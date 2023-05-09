package com.itsfrz.tictactoe.common.functionality

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

object GameWinner {


    private var _winnerIndexList : MutableState <ArrayList<Int>> = mutableStateOf(ArrayList()) 
    val winnerIndexList : State<List<Int>> = _winnerIndexList


    public fun checkAllPositionsByBoard(gameState: List<List<Int>>,boardSize : Int): Boolean {
        for (row in 0 until gameState[0].size){
            for (col in 0 until gameState.size){
                println("row $row, col $col")
                if (validateAllPositions(gameState,row,col,2,boardSize))
                    return true
                if (validateAllPositions(gameState,row,col,1,boardSize))
                    return true
            }
        }
        return false
    }

    private fun validateAllPositions(gameState: List<List<Int>>, row: Int, col: Int, playerValue : Int, boardSize : Int): Boolean {
        _winnerIndexList.value.clear()
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
                  _winnerIndexList.value.add(getBoardIndex(gameState,tempRow,tempCol))
                if(count == boardSize) {
                    println("Pass From Vertical Top")
                    return true;
                }
                tempRow--
            }
        }
        _winnerIndexList.value.clear()
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
                  _winnerIndexList.value.add(getBoardIndex(gameState,tempRow,tempCol))
                if(count == boardSize) {
                    println("Pass Right Upper Diagonal")
                    return true;
                }
                tempRow--
                tempCol++
            }
        }

        _winnerIndexList.value.clear()
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
                  _winnerIndexList.value.add(getBoardIndex(gameState,tempRow,tempCol))
                if(count == boardSize) {
                    println("Pass Horizontal Right")
                    return true;
                }
                tempCol++
            }
        }

        _winnerIndexList.value.clear()
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
                  _winnerIndexList.value.add(getBoardIndex(gameState,tempRow,tempCol))
                if(count == boardSize) {
                    println("Pass Right Lower Diagonal")
                    return true;
                }
                tempRow++
                tempCol++
            }
        }

        _winnerIndexList.value.clear()
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
                  _winnerIndexList.value.add(getBoardIndex(gameState,tempRow,tempCol))
                if(count == boardSize) {
                    println("Pass Vertical Down")
                    return true;
                }
                tempRow++
            }
        }

        _winnerIndexList.value.clear()
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
                  _winnerIndexList.value.add(getBoardIndex(gameState,tempRow,tempCol))
                if(count == boardSize) {
                    println("Pass Left Lower Diagonal")
                    return true;
                }
                tempRow++
                tempCol--
            }
        }

        _winnerIndexList.value.clear()
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
                  _winnerIndexList.value.add(getBoardIndex(gameState,tempRow,tempCol))
                if(count == boardSize) {
                    println("Pass Horizontal Left")
                    return true;
                }
                tempCol--
            }
        }

        _winnerIndexList.value.clear()
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
                  _winnerIndexList.value.add(getBoardIndex(gameState,tempRow,tempCol))
                if(count == boardSize) {
                    println("Pass Left Upper Diagonal")
                    return true;
                }
                tempRow--
                tempCol--
            }
        }

        return false
    }

    private fun getBoardIndex(gameState: List<List<Int>>, row: Int, col: Int) : Int{
        return gameState[0].size * row + col 
    }
    
}