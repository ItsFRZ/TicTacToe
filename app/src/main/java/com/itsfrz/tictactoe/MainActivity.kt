package com.itsfrz.tictactoe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.IdRes
import androidx.navigation.fragment.NavHostFragment
import com.itsfrz.tictactoe.goonline.data.firebase.FirebaseDB
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.IGameStoreRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn

class MainActivity : AppCompatActivity(){

    private var job : Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val storeRepo : GameStoreRepository = IGameStoreRepository(GameDataStore.getDataStore(this))
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.game_graph)
        job = CoroutineScope(Dispatchers.IO).launch {
            storeRepo.fetchPreference().collectLatest {
                Log.i("MAIN_ACTIVITY", "onCreate: DataFlow UserId ${it}")
                withContext(Dispatchers.Main){
                    graph.setStartDestination(if (it.userId.isEmpty()) R.id.userRegistration else R.id.homePage)
                    navHostFragment.navController.graph = graph
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}