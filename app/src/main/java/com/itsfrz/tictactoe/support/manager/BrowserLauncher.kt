package com.itsfrz.tictactoe.support.manager

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

class BrowserLauncher(
    private val url: String
) : PaymentLauncher {

    override fun launch(
        activity: Activity,
        amount: Int
    ) {

        val customTabsIntent =
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()

        customTabsIntent.launchUrl(
            activity,
            Uri.parse(url)
        )
    }
}