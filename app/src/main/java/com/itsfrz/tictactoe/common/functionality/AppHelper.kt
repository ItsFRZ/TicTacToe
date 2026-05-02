package com.itsfrz.tictactoe.common.functionality

import android.content.Context
import android.content.pm.PackageManager

fun isScreenTV(context: Context): Boolean {
    val pm = context.packageManager

    val isLeanback = pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    val hasTouch = pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)

    val widthDp = context.resources.displayMetrics.widthPixels /
            context.resources.displayMetrics.density

    val largeScreen = widthDp >= 720

    val uiMode = context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_TYPE_MASK

    val isTvUiMode = uiMode == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION

    return isLeanback || isTvUiMode || (largeScreen && !hasTouch)
}