package com.itsfrz.tictactoe.support.manager

import android.app.Activity

interface PaymentLauncher {

    fun launch(
        activity: Activity,
        amount: Int
    )
}