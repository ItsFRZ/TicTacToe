package com.itsfrz.tictactoe.online

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.common.components.CustomOutlinedButton
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.headerTitle
import com.itsfrz.tictactoe.online.viewmodel.BoardViewModel
import com.itsfrz.tictactoe.online.viewmodel.BoardViewModelFactory
import com.itsfrz.tictactoe.ui.theme.headerSubTitle
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.functionality.NavOptions
import com.itsfrz.tictactoe.online.viewmodel.OnlineModeViewModel
import com.itsfrz.tictactoe.online.viewmodel.OnlineModeViewModelFactory

class OnlineModeFragment : Fragment() {
    private lateinit var viewmodel: OnlineModeViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = OnlineModeViewModelFactory()
        viewmodel = ViewModelProvider(viewModelStore,viewModelFactory)[OnlineModeViewModel::class.java]

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
                        buttonClick = {
                            findNavController()
                                .navigate(
                                    resId = R.id.friendFragment,
                                    args = null,
                                    navOptions = NavOptions.navOptionStack
                                )
                           },
                        buttonText = "Friend"
                    )
                }
            }
        }
    }

}