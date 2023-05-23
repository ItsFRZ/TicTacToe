package com.itsfrz.tictactoe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.goonline.common.GameTheme
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.IGameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.ISettingRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingDataStore
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull

class MainActivity : AppCompatActivity(){

    private val TAG = "TAG"
    private var job : Job? = null
    private var commonViewModel: CommonViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val storeRepo : GameStoreRepository = IGameStoreRepository(GameDataStore.getDataStore(this))
        val settingRepository = ISettingRepository(SettingDataStore.getDataStore(this))
        runBlocking {
            setupTheme(settingRepository)
        }

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

    private fun setupTheme(settingRepository : ISettingRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            val data = settingRepository.getGameSetting().firstOrNull()
            data?.let {
                when(it.theme){
                    GameTheme.THEME_BLUE-> ThemePicker.colorPicker(com.itsfrz.tictactoe.common.enums.GameTheme.THEME_BLUE)
                    GameTheme.DARK_RED-> ThemePicker.colorPicker(com.itsfrz.tictactoe.common.enums.GameTheme.DARK_RED)
                    GameTheme.DRACULA_GREEN-> ThemePicker.colorPicker(com.itsfrz.tictactoe.common.enums.GameTheme.DRACULA_GREEN)
                    GameTheme.POPPY_ORANGE-> ThemePicker.colorPicker(com.itsfrz.tictactoe.common.enums.GameTheme.POPPY_ORANGE)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        try {
            commonViewModel = CommonViewModel.getInstance()
            commonViewModel?.let {
                it.gameSound.resumeBackgroundMusic()
            }
        }catch (e : UninitializedPropertyAccessException){
            Log.e(TAG, "onResume: $e" )
        }
    }

    override fun onStop() {
        super.onStop()
        commonViewModel = CommonViewModel.getInstance()
        Log.i(TAG, "onStop: MainActivity")
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}