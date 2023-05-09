package com.itsfrz.tictactoe.common.functionality

import com.itsfrz.tictactoe.R

object NavOptions{

    val navOptionStack = androidx.navigation.NavOptions
        .Builder()
        .setEnterAnim(R.anim.screen_enter)
        .setPopEnterAnim(R.anim.pop_screen_enter)
        .build()

}