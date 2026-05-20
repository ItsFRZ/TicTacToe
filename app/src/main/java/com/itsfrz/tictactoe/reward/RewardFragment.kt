package com.itsfrz.tictactoe.reward

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.functionality.GameSound
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.reward.audio.SlotSoundManager
import com.itsfrz.tictactoe.reward.components.pano.PanoMode
import com.itsfrz.tictactoe.reward.components.pano.PanoramaViewer
import com.itsfrz.tictactoe.reward.components.slot.SlotScreenRefined
import com.itsfrz.tictactoe.reward.viewmodel.SlotNewViewModel

class RewardFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    private var rewardUrl : String = ""
    private var gameLevel : Int = 0
    private var viewSlotMachine: Boolean = true
    private lateinit var viewmodel: SlotNewViewModel
    private lateinit var commonViewModel: CommonViewModel
    private lateinit var soundManager: SlotSoundManager
    private var isPanoView : Boolean = false
    private lateinit var gameSound: GameSound

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProvider(this)[SlotNewViewModel::class.java]
        commonViewModel = CommonViewModel.getInstance()
        soundManager = SlotSoundManager(requireContext())
        viewmodel.updateCoinInfo(commonViewModel.goldTokens)
        viewmodel.provideCVMInstance(commonViewModel)
        viewSlotMachine = requireArguments().getBoolean(BundleKey.SLOT_MACHINE,true)
        gameLevel = requireArguments().getInt(BundleKey.GAME_LEVEL,0)
        rewardUrl = requireArguments().getString(BundleKey.REWARD_URL,"")
        isPanoView = !viewSlotMachine && gameLevel >= 1 && rewardUrl.isNotEmpty()
        gameSound = commonViewModel.gameSound
        gameSound.updateRoomLockAttributes(requireContext(),true,isPanoView)
        Log.i("REWARD_FLOW", "onCreate: Game Level : ${gameLevel} :: URL : ${rewardUrl} :: Slot Master ${viewSlotMachine}")
    }


    @SuppressLint("ServiceCast")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val viewMode = remember { mutableStateOf(PanoMode.TOUCH) }
                if (isPanoView) {
                    PanoramaViewer(
                        imageUrl = rewardUrl,
                        modifier = Modifier.fillMaxSize(),
                        initialMode = viewMode.value,
                        onModeChange = {mode -> viewMode.value = mode }
                    )
                } else {
                    SlotScreenRefined(viewmodel, soundManager)
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }


    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }

}