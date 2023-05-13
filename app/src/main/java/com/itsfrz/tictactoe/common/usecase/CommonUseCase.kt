package com.itsfrz.tictactoe.common.usecase

import com.itsfrz.tictactoe.common.enums.PlayerCount
import com.itsfrz.tictactoe.emoji.usecase.EmojiPickerUseCase

sealed class CommonUseCase{
    data class OnSelectedEmojiChange(val selectedIndex : Int) : CommonUseCase()
    data class OnRemovedEmojiChange(val removedIndex : Int) : CommonUseCase()
    data class OnPlayerCountUpdate(val playerCount: PlayerCount) : CommonUseCase()
    object ResetSelectEmojiData : CommonUseCase()
}
