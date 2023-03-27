package com.itsfrz.tictactoe.homepage

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
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.common.components.CustomCircleIconButton
import com.itsfrz.tictactoe.common.components.CustomOutlinedButton
import com.itsfrz.tictactoe.homepage.viewmodel.HomePageViewModel
import com.itsfrz.tictactoe.homepage.viewmodel.HomePageViewModelFactory
import com.itsfrz.tictactoe.ui.theme.PrimaryLight
import com.itsfrz.tictactoe.ui.theme.ThemeBlue
import com.itsfrz.tictactoe.ui.theme.headerTitle
import com.itsfrz.tictactoe.R

class HomePageFragment : Fragment() {
    private lateinit var viewModel: HomePageViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = HomePageViewModelFactory()
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[HomePageViewModel::class.java]

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
                        text = "Choose Your Play Mode",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier
                        .height(78.dp)
                        .fillMaxWidth())
                    CustomOutlinedButton(
                        buttonClick = {

                        },
                        buttonText = "AI"
                    )
                    Spacer(modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth())
                    CustomOutlinedButton(
                        buttonClick = {
                            findNavController().navigate(R.id.onlineModeFragment)
                        },
                        buttonText = "Online"
                    )
                    Spacer(modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth())
                    CustomOutlinedButton(
                        buttonClick = {
                            findNavController().navigate(R.id.gameFragment)
                        },
                        buttonText = "2 Player"
                    )
                    Spacer(modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth())
                    CustomCircleIconButton(iconButtonClick = { /*TODO*/ }, buttonIcon = R.drawable.ic_stats)
                    Spacer(modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 80.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CustomCircleIconButton(iconButtonClick = { /*TODO*/ }, buttonIcon = R.drawable.ic_share)
                        CustomCircleIconButton(iconButtonClick = { /*TODO*/ }, buttonIcon = R.drawable.ic_settings)
                    }

                }
            }
        }
    }

}