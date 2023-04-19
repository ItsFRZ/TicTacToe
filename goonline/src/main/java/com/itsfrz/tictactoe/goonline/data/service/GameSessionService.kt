package com.itsfrz.tictactoe.goonline.data.service

import com.itsfrz.tictactoe.goonline.data.models.BoardState
import com.itsfrz.tictactoe.goonline.data.models.Playground
import com.itsfrz.tictactoe.goonline.data.models.UserProfile

interface GameSessionService {
    suspend fun createGameSession(sessionId : String,friendUserId : String)
    suspend fun updateGameBoard(sessionId : String,gameBoardState: BoardState)
    suspend fun fetchGameBoardInfoAndStore(sessionId : String)
    suspend fun removeGameBoard(sessionId: String)
    suspend fun playAgainRequest(gameSessionId : String,boardState : BoardState)
    suspend fun acceptPlayAgainRequest(gameSessionId: String,boardState: BoardState)
    suspend fun cancelPlayRequest(userId : String,friendUserId: String)
}