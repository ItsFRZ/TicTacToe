package com.itsfrz.tictactoe.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingRepository

class SettingViewModelFactory(
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingViewModel() as T
    }
}