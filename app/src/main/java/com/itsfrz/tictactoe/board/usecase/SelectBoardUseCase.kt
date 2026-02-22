package com.itsfrz.tictactoe.board.usecase

import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.PlayerCount

sealed class SelectBoardUseCase{
    data class OnBoardInfoEvent(val boardInfo : Pair<BoardType,Int>) : SelectBoardUseCase()
    data class OnGameModeEvent(val gameMode : GameMode) : SelectBoardUseCase()
    data class OnPlayerCountEvent(val playerCount : PlayerCount) : SelectBoardUseCase()
    data class OnBoardTypeChange(val index : Int) : SelectBoardUseCase()
}
