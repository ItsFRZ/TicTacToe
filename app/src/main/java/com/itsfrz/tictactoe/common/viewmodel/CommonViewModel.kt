package com.itsfrz.tictactoe.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CommonViewModel private constructor(): ViewModel() {

    private lateinit var gameStoreRepository: GameStoreRepository
    private lateinit var cloudRepository: CloudRepository
    private var userId: String = ""

    companion object {
        private var instance : CommonViewModel? = null
        private lateinit var gameStoreRepository: GameStoreRepository
        private lateinit var cloudRepository: CloudRepository

        private lateinit var dataStoreRepository : GameDataStore
        fun getInstance() : CommonViewModel{
            if (instance == null){
                synchronized(this){
                    if (instance == null){
                       instance = CommonViewModel()
                    }
                }
            }
            return instance!!
        }
    }

    fun registerViewModel(gameStoreRepository: GameStoreRepository,cloudRepository: CloudRepository,userId : String){
        instance?.let {
            it.gameStoreRepository = gameStoreRepository
            it.cloudRepository = cloudRepository
            it.userId = userId
        }
    }

    fun updateOnlineStatus(isOnline : Boolean){
        instance?.let {
            viewModelScope.launch(Dispatchers.IO) {
                if (userId.isNotEmpty()){
                    cloudRepository.updateOnlineStatus(isOnline,userId)
                }
            }
        } ?: println("Common ViewModel instance is not initialized")
    }

}