package com.itsfrz.tictactoe.support.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.support.data.PaymentMethod
import com.itsfrz.tictactoe.support.data.SupportTier
import com.itsfrz.tictactoe.support.manager.BrowserLauncher
import com.itsfrz.tictactoe.support.manager.PaymentLauncher
import com.itsfrz.tictactoe.support.manager.UpiLauncher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Orange Labs
 * Production-ready SupportViewModel
 *
 * Goals:
 * - No payment logic in composables
 * - Stateless UI
 * - Future-proof payment abstraction
 * - OSS friendly
 * - UPI + Browser payment safe handling
 * - Rotation safe
 * - Event driven
 * - Minimal legal/PCI risk
 */

class SupportViewModel : ViewModel() {

    companion object {

        private const val MIN_SUPPORT_AMOUNT = 10
        private const val MAX_SUPPORT_AMOUNT = 100000
    }

    /**
     * Immutable support tiers
     */
    val supportTiers = listOf(

        SupportTier(
            title = "Coffee Drop",
            amount = 49,
            emoji = "☕",
            subtitle = "Support nightly builds"
        ),

        SupportTier(
            title = "Pizza Patch",
            amount = 149,
            emoji = "🍕",
            subtitle = "Fuel multiplayer mode"
        ),

        SupportTier(
            title = "Legendary",
            amount = 499,
            emoji = "🏆",
            subtitle = "Become hall of fame"
        ),

        SupportTier(
            title = "Galaxy Builder",
            amount = 999,
            emoji = "🚀",
            subtitle = "Help shape the future"
        )
    )

    /**
     * Selected support amount
     */
    private val _selectedAmount =
        MutableStateFlow(49)

    val selectedAmount =
        _selectedAmount.asStateFlow()

    /**
     * Custom amount input
     */
    private val _customAmount =
        MutableStateFlow("")

    val customAmount =
        _customAmount.asStateFlow()

    /**
     * Optional supporter message
     */
    private val _supportMessage =
        MutableStateFlow("")

    val supportMessage =
        _supportMessage.asStateFlow()

    /**
     * Selected payment method
     */
    private val _paymentMethod =
        MutableStateFlow(PaymentMethod.UPI)

    val paymentMethod =
        _paymentMethod.asStateFlow()

    /**
     * Payment loading state
     */
    private val _isProcessing =
        MutableStateFlow(false)

    val isProcessing =
        _isProcessing.asStateFlow()

    /**
     * Snackbar / toast / navigation events
     */
    private val _events =
        MutableSharedFlow<SupportUiEvent>()

    val events =
        _events.asSharedFlow()

    /**
     * Entire screen state
     */
    val uiState: StateFlow<SupportUiState> =
        combine(
            _selectedAmount,
            _customAmount,
            _supportMessage,
            _paymentMethod,
            _isProcessing
        ) { amount,
            custom,
            message,
            method,
            loading ->

            val effectiveAmount =
                custom.toIntOrNull()
                    ?: amount

            SupportUiState(
                selectedAmount = effectiveAmount,
                customAmount = custom,
                supportMessage = message,
                paymentMethod = method,
                isProcessing = loading,
                isAmountValid =
                effectiveAmount in
                        MIN_SUPPORT_AMOUNT..MAX_SUPPORT_AMOUNT
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SupportUiState()
        )

    /**
     * Tier clicked
     */
    fun onTierSelected(
        tier: SupportTier
    ) {

        _selectedAmount.value =
            tier.amount

        _customAmount.value = ""
    }

    /**
     * Custom amount changed
     */
    fun onCustomAmountChanged(
        value: String
    ) {

        /**
         * Allow only digits
         */
        val filtered =
            value.filter { it.isDigit() }

        /**
         * Prevent absurd amount lengths
         */
        if (filtered.length > 6) {
            return
        }

        _customAmount.value = filtered
    }

    /**
     * Support message changed
     */
    fun onSupportMessageChanged(
        value: String
    ) {

        /**
         * Prevent huge payloads
         */
        _supportMessage.value =
            value.take(200)
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
     * Main payment action
     */
    fun onSupportClicked(
        activity: Activity
    ) {

        viewModelScope.launch {

            val state = uiState.value

            /**
             * Anti-invalid amount protection
             */
            if (!state.isAmountValid) {

                _events.emit(
                    SupportUiEvent.ShowError(
                        "Invalid support amount"
                    )
                )

                return@launch
            }

            /**
             * Prevent double-click racing
             */
            if (state.isProcessing) {
                return@launch
            }

            _isProcessing.value = true

            try {

                val launcher =
                    createPaymentLauncher(
                        state.paymentMethod,
                        state.selectedAmount
                    )

                launcher.launch(
                    activity,
                    state.selectedAmount
                )

                _events.emit(
                    SupportUiEvent.PaymentStarted
                )

            } catch (e: Exception) {

                _events.emit(
                    SupportUiEvent.ShowError(
                        e.message
                            ?: "Unable to start payment"
                    )
                )

            } finally {

                _isProcessing.value = false
            }
        }
    }

    /**
     * Future-proof launcher abstraction
     */
    private fun createPaymentLauncher(
        method: PaymentMethod,
        amount: Int
    ): PaymentLauncher {

        return when (method) {

            PaymentMethod.UPI -> {

                UpiLauncher(
                    upiId = "orangelabs@upi",
                    receiverName = "Orange Labs"
                )
            }

            PaymentMethod.STRIPE -> {

                BrowserLauncher(
                    url =
                        "https://buy.stripe.com/your_payment_link"
                )
            }

            PaymentMethod.GITHUB -> {

                BrowserLauncher(
                    url =
                    "https://github.com/sponsors/your_username"
                )
            }

            PaymentMethod.KOFI -> {

                BrowserLauncher(
                    url =
                    "https://ko-fi.com/your_page"
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
     * Retry handler
     */
    fun retry(
        activity: Activity
    ) {

        onSupportClicked(activity)
    }

    /**
     * Clear transient input
     */
    fun clearMessage() {

        _supportMessage.value = ""
    }

    /**
     * Reset all
     */
    fun reset() {

        _selectedAmount.value = 49
        _customAmount.value = ""
        _supportMessage.value = ""
        _paymentMethod.value =
            PaymentMethod.UPI
    }
}

/**
 * Immutable screen state
 */
data class SupportUiState(

    val selectedAmount: Int = 49,

    val customAmount: String = "",

    val supportMessage: String = "",

    val paymentMethod: PaymentMethod =
        PaymentMethod.UPI,

    val isProcessing: Boolean = false,

    val isAmountValid: Boolean = true
)

/**
 * One-shot events
 */
sealed interface SupportUiEvent {

    data object PaymentStarted :
        SupportUiEvent

    data class ShowError(
        val message: String
    ) : SupportUiEvent
}