package com.itsfrz.tictactoe.friend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository

class FriendPageViewModelFactory(
    private val cloudRepository: CloudRepository,
    private val gameStoreRepository: GameStoreRepository
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FriendPageViewModel(cloudRepository, gameStoreRepository) as T
    }
}