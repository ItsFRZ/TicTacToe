package com.itsfrz.tictactoe.common.usecase

import com.itsfrz.tictactoe.common.enums.PlayerCount

sealed class CommonUseCase{
    data class OnSelectedEmojiChange(val selectedIndex : Int) : CommonUseCase()
    data class OnRemovedEmojiChange(val removedIndex : Int) : CommonUseCase()
    data class OnPlayerCountUpdate(val playerCount: PlayerCount) : CommonUseCase()
    object ResetSelectEmojiData : CommonUseCase()
    object OnCreditWinningToken : CommonUseCase()
    object OnDebitLosingToken : CommonUseCase()
    data class OnSlotMasterTokenUpdate(val token : Int) : CommonUseCase()
    data class OnLevelPurchase(val token : Int,val levelId : Int) : CommonUseCase()
    data class OnPurchaseTokenUpdate(val token : Int) : CommonUseCase()
}
