package com.itsfrz.tictactoe.emoji.usecase

import com.itsfrz.tictactoe.common.enums.PlayerCount
import com.itsfrz.tictactoe.common.state.EmojiState
import com.itsfrz.tictactoe.game.domain.usecase.GameUsecase

sealed class EmojiPickerUseCase{
    data class FillEmojiData(val emojis : List<EmojiState>) : EmojiPickerUseCase()
    data class OnSelectedEmojiChange(val selectedIndex : Int) : EmojiPickerUseCase()
    data class OnRemovedEmojiChange(val removedIndex : Int) : EmojiPickerUseCase()
    data class UpdateUserId(val userId : String): EmojiPickerUseCase()
    data class UpdateFriendUserId(val userId : String): EmojiPickerUseCase()
    data class OnUpdateGameSessionId(val sessionId : String) : EmojiPickerUseCase()
    data class OnUpdateCurrentUserId(val currentUserId : String) : EmojiPickerUseCase()
}
