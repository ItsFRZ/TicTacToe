package com.itsfrz.tictactoe.support.manager

import android.app.Activity
import android.content.Intent
import android.net.Uri

class UpiLauncher(
    private val upiId: String,
    private val receiverName: String
) : PaymentLauncher {

    override fun launch(
        activity: Activity,
        amount: Int
    ) {

        val uri = Uri.parse(
            "upi://pay" +
                    "?pa=$upiId" +
                    "&pn=$receiverName" +
                    "&am=$amount" +
                    "&cu=INR"
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
        }

        activity.startActivity(intent)
    }
}