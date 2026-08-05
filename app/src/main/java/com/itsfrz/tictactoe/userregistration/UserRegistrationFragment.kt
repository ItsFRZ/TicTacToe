package com.itsfrz.tictactoe.userregistration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale

class UserRegistrationFragment : Fragment() {
    private lateinit var viewModel: UserRegistrationViewModel
    private lateinit var cloudRepository: CloudRepository
    private lateinit var settingRepository: SettingRepository

    private lateinit var dataStoreRepository: GameStoreRepository


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

//    @Composable
//    private fun TrustItem(
//        text: String,
//        modifier: Modifier = Modifier
//    ) {
//        Row(
//            modifier = modifier
//                .fillMaxWidth()
//                .padding(vertical = 2.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//
//            Icon(
//                imageVector = Icons.Rounded.CheckCircle,
//                contentDescription = null,
//                tint = ThemePicker.secondaryColor.value.copy(alpha = 0.95f),
//                modifier = Modifier.size(15.dp)
//            )
//
//            Spacer(
//                modifier = Modifier.width(12.dp)
//            )
//
//            Text(
//                text = text,
//                color = Color.White.copy(alpha = 0.78f),
//                maxLines = 1
//            )
//        }
//    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val username = viewModel.usernameValue.value
                val isUserNameEmpty = viewModel.isUsernameEmpty.value
                val languageExpanded = viewModel.languageExpanded.value
                val selectedLanguage = viewModel.selectedLanguage.value

                // Same tone as before — never hard-coded
                val primary = ThemePicker.primaryColor.value
                val secondary = ThemePicker.secondaryColor.value

                @OptIn(ExperimentalMaterial3Api::class)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = primary)
                        .statusBarsPadding()
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            style = headerTitle.copy(color = Color.White),
                            text = buildAnnotatedString {
                                append(stringResource(R.string.register_title) + "\n")
                                withStyle(
                                    style = SpanStyle(
                                        color = secondary,
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
                        Spacer(modifier = Modifier.height(28.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.88f)) {
                            TextFieldWithValidation(
                                fieldValue = username,
                                onUsernameChange = { inputData ->
                                    viewModel.onEvent(UserRegistrationUseCase.OnUsernameChange(inputData))
                                },
                                isValidationTriggered = isUserNameEmpty
                            )
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.88f)) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onEvent(UserRegistrationUseCase.OnLangToggle(true))
                                    },
                                value = selectedLanguage.first,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Language,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color.White.copy(alpha = .25f),
                                    disabledTextColor = Color.White,
                                    disabledLeadingIconColor = Color.White,
                                    disabledTrailingIconColor = Color.White,
                                    disabledContainerColor = Color.Transparent,
                                    focusedBorderColor = secondary,
                                    unfocusedBorderColor = Color.White.copy(alpha = .25f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )

                            DropdownMenu(
                                expanded = languageExpanded,
                                onDismissRequest = {
                                    viewModel.onEvent(UserRegistrationUseCase.OnLangToggle(false))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                viewModel.supportedLanguages.forEach { language ->
                                    DropdownMenuItem(
                                        text = { Text("${language.first} (${language.second})") },
                                        onClick = {
                                            viewModel.onEvent(UserRegistrationUseCase.OnLanguageChange(language))
                                            viewModel.onEvent(UserRegistrationUseCase.OnLangToggle(false))
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.choose_language),
                            color = Color.White.copy(alpha = 0.72f),
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.07f))
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.14f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(vertical = 6.dp)
                        ) {
                            AssuranceRow(
                                glyph = TrustGlyph.EyeOff,
                                text = stringResource(R.string.no_tracking),
                                accent = secondary
                            )
                            AssuranceDivider()
                            AssuranceRow(
                                glyph = TrustGlyph.NoAds,
                                text = stringResource(R.string.no_ads),
                                accent = secondary
                            )
                            AssuranceDivider()
                            AssuranceRow(
                                glyph = TrustGlyph.ShieldLock,
                                text = stringResource(R.string.privacy_first),
                                accent = secondary
                            )
                            AssuranceDivider()
                            AssuranceRow(
                                glyph = TrustGlyph.CloudOff,
                                text = stringResource(R.string.offline_support),
                                accent = secondary
                            )
                            AssuranceDivider()
                            AssuranceRow(
                                glyph = TrustGlyph.KeyOff,
                                text = stringResource(R.string.no_permission),
                                accent = secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))
                    }
                    Box(modifier = Modifier.fillMaxWidth(0.88f)) {
                        CustomButton(
                            onButtonClick = {
                                viewModel.onEvent(UserRegistrationUseCase.OnSubmitButtonClick)
                                findNavController().popBackStack(R.id.userRegistration, true)
                                findNavController().navigate(R.id.homePage)
                            },
                            isButtonEnabled = !isUserNameEmpty
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp).fillMaxWidth())
                }
            }
        }
    }

    private enum class TrustGlyph { EyeOff, NoAds, ShieldLock, CloudOff, KeyOff }

    @Composable
    private fun AssuranceRow(
        glyph: TrustGlyph,
        text: String,
        accent: Color
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f))
                    .padding(9.dp),
                contentAlignment = Alignment.Center
            ) {
                GlyphIcon(glyph = glyph, tint = accent, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }

    @Composable
    private fun AssuranceDivider() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.10f))
        )
    }

    @Composable
    private fun GlyphIcon(glyph: TrustGlyph, tint: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier) {
            val u = size.width / 24f
            scale(u, u, pivot = Offset.Zero) {
                val stroke = Stroke(width = 1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)

                when (glyph) {
                    TrustGlyph.EyeOff -> {
                        val eye = Path().apply {
                            moveTo(2.5f, 12f)
                            quadraticTo(12f, 4.8f, 21.5f, 12f)
                            quadraticTo(12f, 19.2f, 2.5f, 12f)
                        }
                        drawPath(eye, tint, style = stroke)
                        drawCircle(tint, radius = 2.1f, center = Offset(12f, 12f), style = stroke)
                        drawLine(tint, Offset(4.5f, 19.5f), Offset(19.5f, 4.5f), stroke.width, stroke.cap)
                    }
                    TrustGlyph.NoAds -> {
                        drawCircle(tint, radius = 8.6f, center = Offset(12f, 12f), style = stroke)
                        drawLine(tint, Offset(6f, 18f), Offset(18f, 6f), stroke.width, stroke.cap)
                    }
                    TrustGlyph.ShieldLock -> {
                        val shield = Path().apply {
                            moveTo(12f, 2.8f)
                            lineTo(19f, 5.6f)
                            lineTo(19f, 10.8f)
                            quadraticTo(19f, 16.8f, 12f, 21.2f)
                            quadraticTo(5f, 16.8f, 5f, 10.8f)
                            lineTo(5f, 5.6f)
                            close()
                        }
                        drawPath(shield, tint, style = stroke)
                        drawRoundRect(
                            tint,
                            topLeft = Offset(9.7f, 11.4f),
                            size = Size(4.6f, 4.2f),
                            cornerRadius = CornerRadius(1.1f),
                            style = stroke
                        )
                        drawArc(
                            tint,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(9.9f, 8.6f),
                            size = Size(4.2f, 4.2f),
                            style = stroke
                        )
                    }
                    TrustGlyph.CloudOff -> {
                        val cloud = Path().apply {
                            moveTo(6.6f, 17f)
                            quadraticTo(3.2f, 17f, 3.2f, 14f)
                            quadraticTo(3.2f, 11.4f, 5.9f, 11f)
                            quadraticTo(6.6f, 7.2f, 10.8f, 7.2f)
                            quadraticTo(14f, 7.2f, 15.2f, 9.9f)
                            quadraticTo(20.8f, 9.7f, 20.8f, 13.4f)
                            quadraticTo(20.8f, 17f, 17.2f, 17f)
                            close()
                        }
                        drawPath(cloud, tint, style = stroke)
                        drawLine(tint, Offset(9.6f, 14.8f), Offset(14.6f, 9.8f), stroke.width, stroke.cap)
                    }
                    TrustGlyph.KeyOff -> {
                        drawCircle(tint, radius = 3f, center = Offset(7.5f, 12f), style = stroke)
                        drawLine(tint, Offset(10.5f, 12f), Offset(20f, 12f), stroke.width, stroke.cap)
                        drawLine(tint, Offset(16.5f, 12f), Offset(16.5f, 15f), stroke.width, stroke.cap)
                        drawLine(tint, Offset(20f, 12f), Offset(20f, 15f), stroke.width, stroke.cap)
                        drawLine(tint, Offset(4.5f, 19.5f), Offset(19.5f, 4.5f), stroke.width, stroke.cap)
                    }
                }
            }
        }
    }

    @Composable
    private fun TicTacToeGlyph(tint: Color, accent: Color, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier) {
            val u = size.width / 24f
            scale(u, u, pivot = Offset.Zero) {
            val gridW = 0.9f
                val symW = 1.7f
                val gridColor = tint.copy(alpha = 0.35f)

                drawLine(gridColor, Offset(9f, 3.5f), Offset(9f, 20.5f), gridW, StrokeCap.Round)
                drawLine(gridColor, Offset(15f, 3.5f), Offset(15f, 20.5f), gridW, StrokeCap.Round)
                drawLine(gridColor, Offset(3.5f, 9f), Offset(20.5f, 9f), gridW, StrokeCap.Round)
                drawLine(gridColor, Offset(3.5f, 15f), Offset(20.5f, 15f), gridW, StrokeCap.Round)

                for (i in 0 until 9) {
                    val cx = 6f + (i % 3) * 6f
                    val cy = 6f + (i / 3) * 6f
                    if (i % 2 == 0) {
                        drawLine(tint, Offset(cx - 2f, cy - 2f), Offset(cx + 2f, cy + 2f), symW, StrokeCap.Round)
                        drawLine(tint, Offset(cx + 2f, cy - 2f), Offset(cx - 2f, cy + 2f), symW, StrokeCap.Round)
                    } else {
                        drawCircle(accent, radius = 2.1f, center = Offset(cx, cy),
                            style = Stroke(width = symW, cap = StrokeCap.Round))
                    }
                }
            }
        }
    }
}