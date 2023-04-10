package com.itsfrz.tictactoe.goonline.data.service

import com.itsfrz.tictactoe.goonline.data.models.BoardState
import com.itsfrz.tictactoe.goonline.data.models.Playground

interface GameSessionService {
    suspend fun createGameSession(sessionId : String,friendUserId : String)
    suspend fun updateGameBoard(sessionId : String,gameBoardState: BoardState)
    suspend fun fetchGameBoardInfoAndStore(sessionId : String)
    suspend fun removeGameBoard(sessionId: String)
}