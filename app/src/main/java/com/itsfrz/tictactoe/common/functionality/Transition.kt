package com.itsfrz.tictactoe.common.functionality

import androidx.annotation.IdRes
import com.itsfrz.tictactoe.R

object NavOptions{

    val navOptionStack = androidx.navigation.NavOptions
        .Builder()
        .setEnterAnim(R.anim.screen_enter)
        .setPopEnterAnim(R.anim.pop_screen_enter)
        .build()

    fun navigatePop(@IdRes resId : Int) : androidx.navigation.NavOptions{
        return androidx.navigation.NavOptions
            .Builder()
            .setEnterAnim(R.anim.screen_enter)
            .setPopEnterAnim(R.anim.pop_screen_enter)
            .setPopUpTo(destinationId = resId,inclusive = true)
            .build()
    }



}