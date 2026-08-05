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


    private val _selectedAmount =
        MutableStateFlow(49)
    val selectedAmount = _selectedAmount


    fun onAmountChanged(
        value: Int
    ) {
        _selectedAmount.value = value
    }


}
