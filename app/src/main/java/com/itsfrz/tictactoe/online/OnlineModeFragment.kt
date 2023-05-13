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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.common.components.CustomOutlinedButton
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.functionality.InternetHelper
import com.itsfrz.tictactoe.common.functionality.NavOptions
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.online.viewmodel.OnlineModeViewModel
import com.itsfrz.tictactoe.online.viewmodel.OnlineModeViewModelFactory
import com.itsfrz.tictactoe.ui.theme.*

class OnlineModeFragment : Fragment() {
    private lateinit var viewmodel: OnlineModeViewModel
    private lateinit var commonViewModel: CommonViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = OnlineModeViewModelFactory()
        viewmodel = ViewModelProvider(viewModelStore,viewModelFactory)[OnlineModeViewModel::class.java]
        commonViewModel = CommonViewModel.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(color = PrimaryMain),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth())
                    Text(
                        style = headerTitle.copy(color = Color.White),
                        text = buildAnnotatedString {
                            append("Online")
                            withStyle(style = SpanStyle(color = ThemeBlue)){
                                append(" Mode")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth())
                    Text(
                        style = headerSubTitle.copy(color = LightWhite),
                        text = "Play with random person\n--- OR ---\nEnter your friend’s id to play.",
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

    override fun onResume() {
        super.onResume()
        commonViewModel.updateOnlineStatus(isOnline = InternetHelper.isOnline(requireContext()))
    }

}