package com.itsfrz.tictactoe.support.manager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher

class UpiLauncher(
    private val upiId: String,
    private val receiverName: String,
    private val launcher: ActivityResultLauncher<Intent>
) : PaymentLauncher {
    override fun launch(
        activity: Activity,
        amount: Int
    ) {
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", receiverName)
            .appendQueryParameter("am", amount.toString())
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter(
                "tn",
                "Orange Labs"
            )
            .appendQueryParameter(
                "tr",
                System.currentTimeMillis().toString()
            )
            .build()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
        }
        val chooser = Intent.createChooser(
            intent,
            "Pay with"
        )
        if (intent.resolveActivity(activity.packageManager) != null) {
            launcher.launch(chooser)
        } else {
            Toast.makeText(activity, "No UPI app found", Toast.LENGTH_SHORT).show()
        }
    }
}