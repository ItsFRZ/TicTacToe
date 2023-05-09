package com.itsfrz.tictactoe.board.usecase

import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameMode

sealed class SelectBoardUseCase{
    data class OnBoardInfoEvent(val boardInfo : Pair<BoardType,Int>) : SelectBoardUseCase()
    data class OnGameModeEvent(val gameMode : GameMode) : SelectBoardUseCase()
    data class OnBoardTypeChange(val index : Int) : SelectBoardUseCase()
}
