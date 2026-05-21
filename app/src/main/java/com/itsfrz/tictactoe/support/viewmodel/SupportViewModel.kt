package com.itsfrz.tictactoe.support.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.support.data.PaymentMethod
import com.itsfrz.tictactoe.support.data.SupportTier
import com.itsfrz.tictactoe.support.manager.BrowserLauncher
import com.itsfrz.tictactoe.support.manager.PaymentLauncher
import com.itsfrz.tictactoe.support.manager.UpiLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SupportViewModel : ViewModel() {

    fun onPayment(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>
    ) {
        onSupportClicked(activity,launcher)
    }

    /**
     * Main payment action
     */
    fun onSupportClicked(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>
    ) {
        viewModelScope.launch {
            if (_selectedAmount.value < 49) {
                return@launch
            }
            /**
             * Prevent double-click racing
             */
            if (_isProcessing.value) {
                return@launch
            }
            _isProcessing.value = true
            try {
                val chooser =
                    createPaymentLauncher(
                        method = _paymentMethod.value,
                        launcher = launcher
                    )
                chooser.launch(
                    activity,
                    _selectedAmount.value
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isProcessing.value = false
            }
        }
    }


    private val _selectedAmount =
        MutableStateFlow(49)
    val selectedAmount = _selectedAmount

    private val _paymentMethod =
        MutableStateFlow(PaymentMethod.UPI)


    private val _isProcessing =
        MutableStateFlow(false)

    val isProcessing =
        _isProcessing.asStateFlow()

    fun onTierSelected(
        tier: SupportTier
    ) {
        _selectedAmount.value = tier.amount
    }

    /**
     * Custom amount changed
     */
    fun onAmountChanged(
        value: Int
    ) {
        _selectedAmount.value = value
    }

    /**
     * Payment method changed
     */
    fun onPaymentMethodChanged(
        method: PaymentMethod
    ) {
        _paymentMethod.value = method
    }


    /**
     * Future-proof launcher abstraction
     */
    private fun createPaymentLauncher(
        method: PaymentMethod,
        launcher: ActivityResultLauncher<Intent>
    ): PaymentLauncher {
        return when (method) {

            PaymentMethod.UPI -> {
                UpiLauncher(
                    upiId = "7796224997@ybl",
                    receiverName = "Orange Labs",
                    launcher = launcher
                )
            }

            PaymentMethod.RAZORPAY -> {

                BrowserLauncher(
                    url =
                        "https://razorpay.me/@orangelabsinc"
                )
            }

            PaymentMethod.GITHUB -> {

                BrowserLauncher(
                    url =
                        "https://github.com/sponsors/itsfaraz"
                )
            }

            PaymentMethod.KOFI -> {

                BrowserLauncher(
                    url =
                        "https://ko-fi.com/orangelabs"
                )
            }
            PaymentMethod.PAYPAL -> {

                BrowserLauncher(
                    url =
                        "https://www.paypal.com/paypalme/orangelabs"
                )
            }
        }
    }

    /**
     * Optional analytics hook
     */
    fun onScreenViewed() {
        viewModelScope.launch {
            /**
             * Firebase / PostHog / Sentry hook
             *
             * Intentionally empty
             * for OSS friendliness
             */
        }
    }

    /**
     * Reset all
     */
    fun reset() {
        _selectedAmount.value = 49
        _paymentMethod.value = PaymentMethod.UPI
    }
}
