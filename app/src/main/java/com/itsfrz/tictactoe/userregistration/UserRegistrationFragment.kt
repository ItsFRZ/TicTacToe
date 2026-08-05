package com.itsfrz.tictactoe.userregistration

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.CustomButton
import com.itsfrz.tictactoe.common.components.TextFieldWithValidation
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.goonline.data.repositories.CloudRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameDataStore
import com.itsfrz.tictactoe.goonline.datastore.gamestore.GameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.gamestore.IGameStoreRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.ISettingRepository
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingDataStore
import com.itsfrz.tictactoe.goonline.datastore.setting.SettingRepository
import com.itsfrz.tictactoe.ui.theme.headerTitle
import com.itsfrz.tictactoe.userregistration.usecase.UserRegistrationUseCase
import com.itsfrz.tictactoe.userregistration.viewmodel.UserRegistrationViewModel
import com.itsfrz.tictactoe.userregistration.viewmodel.UserRegistrationViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class UserRegistrationFragment : Fragment() {
    private lateinit var viewModel: UserRegistrationViewModel
    private lateinit var cloudRepository: CloudRepository
    private lateinit var settingRepository: SettingRepository

    private lateinit var dataStoreRepository: GameStoreRepository
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpOnlineConfig()
        val viewModelFactory =
            UserRegistrationViewModelFactory(cloudRepository, dataStoreRepository, settingRepository)
        viewModel = ViewModelProvider(
            viewModelStore,
            viewModelFactory
        )[UserRegistrationViewModel::class.java]
    }

    private fun setUpOnlineConfig() {
        val gameStore = GameDataStore.getDataStore(requireContext())
        dataStoreRepository = IGameStoreRepository(gameStore)
        cloudRepository = CloudRepository(
            dataStoreRepository = dataStoreRepository,
            scope = CoroutineScope(Dispatchers.IO)
        )
        settingRepository = ISettingRepository(SettingDataStore.getDataStore(requireContext()))
    }

    @Composable
    private fun TrustItem(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = ThemePicker.secondaryColor.value.copy(alpha = 0.95f),
                modifier = Modifier.size(15.dp)
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = text,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 1
            )
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val username = viewModel.usernameValue.value
                val isUserNameEmpty = viewModel.isUsernameEmpty.value
                var languageExpanded = viewModel.languageExpanded.value
                var selectedLanguage = viewModel.selectedLanguage.value

                @OptIn(ExperimentalMaterial3Api::class)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = ThemePicker.primaryColor.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp).fillMaxWidth())
                    Text(
                        style = headerTitle.copy(color = Color.White),
                        text = buildAnnotatedString {
                            append(stringResource(R.string.register_title)+"\n")
                            withStyle(
                                style = SpanStyle(
                                    color = ThemePicker.secondaryColor.value,
                                    fontFamily = headerTitle.fontFamily,
                                    fontSize = headerTitle.fontSize,
                                    fontWeight = headerTitle.fontWeight
                                )
                            ) { append(stringResource(R.string.username)) }
                            append(stringResource(R.string.play_online))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.choose_language),
                        color = Color.White.copy(alpha = 0.72f),
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(0.86f)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { viewModel.onEvent(UserRegistrationUseCase.OnLangToggle(true)) },
                            value = selectedLanguage.first,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = {
                                Icon(imageVector = Icons.Rounded.Language, contentDescription = null, tint = Color.White)
                            },
                            trailingIcon = {
                                Icon(imageVector = Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = Color.White)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = Color.White.copy(alpha = .25f),
                                disabledTextColor = Color.White,
                                disabledLeadingIconColor = Color.White,
                                disabledTrailingIconColor = Color.White,
                                disabledContainerColor = Color.Transparent,
                                focusedBorderColor = ThemePicker.secondaryColor.value,
                                unfocusedBorderColor = Color.White.copy(alpha = .25f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        DropdownMenu (
                            expanded = languageExpanded,
                            onDismissRequest = {
                                viewModel.onEvent(UserRegistrationUseCase.OnLangToggle(false))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            viewModel.supportedLanguages.forEach { language ->
                                DropdownMenuItem(
                                    text = {  Text("${language.first} (${language.second})") },
                                    onClick = {
                                        viewModel.onEvent(UserRegistrationUseCase.OnLanguageChange(language))
                                        viewModel.onEvent(UserRegistrationUseCase.OnLangToggle(false))
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp).fillMaxWidth())
                    TextFieldWithValidation(
                        fieldValue = username,
                        onUsernameChange = { inputData ->
                            viewModel.onEvent(UserRegistrationUseCase.OnUsernameChange(inputData))
                        },
                        isValidationTriggered = isUserNameEmpty
                    )
                    Spacer(modifier = Modifier.height(30.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(0.86f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrustItem(stringResource(R.string.no_tracking))
                        TrustItem(stringResource(R.string.no_ads))
                        TrustItem(stringResource(R.string.privacy_first))
                        TrustItem(stringResource(R.string.offline_support))
                        TrustItem(stringResource(R.string.no_permission))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    CustomButton(
                        onButtonClick = {
                            viewModel.onEvent(UserRegistrationUseCase.OnSubmitButtonClick)
                            findNavController().popBackStack(R.id.userRegistration, true)
                            findNavController().navigate(R.id.homePage)
                        },
                        isButtonEnabled = !isUserNameEmpty
                    )
                    Spacer(modifier = Modifier.height(32.dp).fillMaxWidth())
                }
            }
        }
    }
}