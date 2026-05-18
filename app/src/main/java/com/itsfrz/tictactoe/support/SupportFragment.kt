package com.itsfrz.tictactoe.support

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.itsfrz.tictactoe.support.ui.components.SupportScreen
import com.itsfrz.tictactoe.support.viewmodel.SupportViewModel



class SupportFragment : Fragment() {

    private lateinit var viewModel: SupportViewModel
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SupportViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle()
                SupportScreen()
            }
        }
    }
}