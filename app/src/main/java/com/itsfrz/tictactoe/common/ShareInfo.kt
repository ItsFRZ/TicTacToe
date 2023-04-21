package com.itsfrz.tictactoe.common

import android.util.Log

object ShareInfo {
    const val SHARE_HEADER = "✅ Classic Criss Cross ❌"
    const val SHARE_TITLE = "Friend is inviting you to 🔥 compete in a game"
    const val SHARE_SUBTITLE = "Paste this message or userId in 🔎 search window of friend page and enjoy the game!"

    fun getUserIdFromTemplate(template : String) : String{
        val TAG = "UID_SBOX"
        Log.i(TAG, "getUserIdFromTemplate: ${template}")
        if (template.contains("✄-x"))
        {
            val index = template.indexOf("x")
            val rawUserId = template.substring(index)
            Log.i(TAG, "getUserIdFromTemplate: Raw UserId ${rawUserId}")
            val userId = rawUserId.substring(1,rawUserId.length-3)
            Log.i(TAG, "getUserIdFromTemplate: Filtered Id ${userId}")
            return userId
        }
        return template
    }
}