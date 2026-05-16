package com.itsfrz.tictactoe.reward

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.background.refined.AnimatedGameBackground
import com.itsfrz.tictactoe.common.components.CustomCircleIconButton
import com.itsfrz.tictactoe.common.components.CustomCircleTextButton
import com.itsfrz.tictactoe.common.components.CustomOutlinedButton
import com.itsfrz.tictactoe.common.components.GameDialogue
import com.itsfrz.tictactoe.common.components.TitleTextComponent
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.PlayerCount
import com.itsfrz.tictactoe.common.functionality.GameSound
import com.itsfrz.tictactoe.common.functionality.InternetHelper
import com.itsfrz.tictactoe.common.functionality.NavOptions
import com.itsfrz.tictactoe.common.functionality.ShareInfo
import com.itsfrz.tictactoe.common.functionality.isScreenTV
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.IGameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.ISettingRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingDataStore
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingRepository
import com.itsfrz.tictactoe.home.usecase.HomePageUseCase
import com.itsfrz.tictactoe.home.viewmodel.HomePageViewModel
import com.itsfrz.tictactoe.home.viewmodel.HomePageViewModelFactory
import com.itsfrz.tictactoe.reward.components.PanoramaViewMode
import com.itsfrz.tictactoe.reward.components.PanoramaViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.abs

class RewardFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    @SuppressLint("ServiceCast")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PanoramaViewer(
                    imageUrl = "https://dl.polyhaven.org/file/ph-assets/HDRIs/extra/Tonemapped%20JPG/citrus_orchard_road_puresky.jpg",
                    modifier = Modifier.fillMaxSize(),
                    initialMode = PanoramaViewMode.CINEMA
                )
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
    }

}