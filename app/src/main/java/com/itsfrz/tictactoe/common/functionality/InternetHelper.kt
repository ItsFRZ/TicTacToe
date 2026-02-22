package com.itsfrz.tictactoe.common.functionality

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object InternetHelper {

    fun isOnline(context : Context) : Boolean {
        val connectivityManager : ConnectivityManager? = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager?.let {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            capabilities?.let {
                if (it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    return true
                if (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                    return true
                if (it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    return true
            }
        }
        return false
    }

}