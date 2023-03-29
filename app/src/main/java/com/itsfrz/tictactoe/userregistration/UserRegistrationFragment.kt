package com.itsfrz.tictactoe.userregistration

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.MainActivity
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.CustomButton
import com.itsfrz.tictactoe.common.components.TextFieldWithValidation
import com.itsfrz.tictactoe.ui.theme.*
import com.itsfrz.tictactoe.userregistration.usecase.UserRegistrationUseCase
import com.itsfrz.tictactoe.userregistration.viewmodel.UserRegistrationViewModel
import com.itsfrz.tictactoe.userregistration.viewmodel.UserRegistrationViewModelFactory

class UserRegistrationFragment : Fragment() {
    private lateinit var viewModel : UserRegistrationViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelFactory = UserRegistrationViewModelFactory()
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)[UserRegistrationViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val username = viewModel.usernameValue.value
                val isUserExists = viewModel.isUsernameExists.value

                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(color = PrimaryLight),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth())
                    Text(
                        style = headerTitle,
                        text = buildAnnotatedString {
                            append("You must register\n")
                            withStyle(
                                style = SpanStyle(
                                    color = ThemeBlue,
                                    fontFamily = headerTitle.fontFamily,
                                    fontSize = headerTitle.fontSize,
                                    fontWeight = headerTitle.fontWeight
                                )){
                                append("Username")
                            }
                            append(" to play online")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier
                        .height(78.dp)
                        .fillMaxWidth())
                    TextFieldWithValidation(
                        fieldValue = username,
                        onUsernameChange = { inputData ->
                            viewModel.onEvent(UserRegistrationUseCase.OnUsernameChange(inputData))
                        },
                        isUsernameExist = isUserExists
                    )
                    Spacer(modifier = Modifier
                        .height(78.dp)
                        .fillMaxWidth())
                    CustomButton(
                        onButtonClick = {
                            findNavController().navigate(R.id.homePage)
                        },
                        isButtonEnabled = !isUserExists
                    )
                }
            }
        }
    }
}