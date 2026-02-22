package com.itsfrz.tictactoe.common.state

data class EmojiState(
    val emojiResourceId : Int,
    val emojiIndex : Int,
    val isSelected : Boolean = false
)
