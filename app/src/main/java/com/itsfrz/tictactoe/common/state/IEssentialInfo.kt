package com.itsfrz.tictactoe.common.state

import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameLevel
import com.itsfrz.tictactoe.common.enums.GameMode

data class IEssentialInfo(val gameMode : GameMode,
                     val gameLevel : GameLevel,
                     val boardType: BoardType) : EssentialInfo {
    override fun getEssentialInfo(): IEssentialInfo = this
}