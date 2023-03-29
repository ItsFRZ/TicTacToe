package com.itsfrz.tictactoe.online

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.itsfrz.tictactoe.MainActivity
import com.itsfrz.tictactoe.common.components.CustomOutlinedButton
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.headerTitle
import com.itsfrz.tictactoe.online.viewmodel.OnlineModeViewModel
import com.itsfrz.tictactoe.online.viewmodel.OnlineModeViewModelFactory
import com.itsfrz.tictactoe.ui.theme.headerSubTitle

class OnlineModeFragment : Fragment() {
    private lateinit var viewModel: OnlineModeViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = OnlineModeViewModelFactory()
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[OnlineModeViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(color = PrimaryLight),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth())
                    Text(
                        style = headerTitle.copy(color = ThemeBlue),
                        text = "Online Mode",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth())
                    Text(
                        style = headerSubTitle.copy(color = ThemeBlue),
                        text = "Play with random person\n--- OR ---\nEnter your friend’s username to play.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier
                        .height(78.dp)
                        .fillMaxWidth())
                    CustomOutlinedButton(
                        buttonClick = {},
                        buttonText = "Random"
                    )
                    Spacer(modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth())
                    CustomOutlinedButton(
                        buttonClick = {},
                        buttonText = "Friend"
                    )
                }
            }
        }
    }

}