package com.itsfrz.tictactoe.emoji

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.CustomButton
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.PlayerCount
import com.itsfrz.tictactoe.common.functionality.GameSound
import com.itsfrz.tictactoe.common.functionality.NavOptions
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.common.functionality.isScreenTV
import com.itsfrz.tictactoe.common.usecase.CommonUseCase
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.emoji.components.EmojiDialogue
import com.itsfrz.tictactoe.emoji.usecase.EmojiPickerUseCase
import com.itsfrz.tictactoe.emoji.viewmodel.EmojiPickerViewModel
import com.itsfrz.tictactoe.emoji.viewmodel.EmojiPickerViewModelFactory
import com.itsfrz.tictactoe.ui.theme.headerTitle

class EmojiPickerFragment : Fragment() {
    private lateinit var viewmodel: EmojiPickerViewModel
    private lateinit var commonViewModel: CommonViewModel
    private lateinit var gameMode: GameMode
    private lateinit var playerCount: PlayerCount
    private lateinit var gameSound: GameSound
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelFactory = EmojiPickerViewModelFactory()
        viewmodel = ViewModelProvider(this, viewModelFactory)[EmojiPickerViewModel::class.java]
        commonViewModel = CommonViewModel.getInstance()
        gameSound = commonViewModel.gameSound
        viewmodel.onEvent(EmojiPickerUseCase.FillEmojiData(commonViewModel.emojiDataList))
        commonViewModel.onEvent(CommonUseCase.ResetSelectEmojiData)
        setUpNavArgs()
    }

    private fun setUpNavArgs() {
        val friendUserId = requireArguments().getString(BundleKey.FRIEND_ID)
        friendUserId?.let {
            if (it.isNotEmpty())
                viewmodel.onEvent(EmojiPickerUseCase.UpdateFriendUserId(it))
        }
        val userId = requireArguments().getString(BundleKey.USER_ID)
        userId?.let {
            if (it.isNotEmpty())
                viewmodel.onEvent(EmojiPickerUseCase.UpdateUserId(it))
        }
        val gameSessionId = requireArguments().getString(BundleKey.SESSION_ID)
        gameSessionId?.let {
            if (it.isNotEmpty()) {
                viewmodel.onEvent(EmojiPickerUseCase.OnUpdateGameSessionId(it))
                viewmodel.onEvent(EmojiPickerUseCase.OnUpdateCurrentUserId(it))
            }
        }
        gameMode = requireArguments().getSerializable(BundleKey.GAME_MODE) as GameMode
        playerCount = requireArguments().getSerializable(BundleKey.PLAYER_COUNT) as PlayerCount
        commonViewModel.onEvent(CommonUseCase.OnPlayerCountUpdate(playerCount))
        Log.i("PLAYER_COUNT", "setUpNavArgs: ${commonViewModel.playerCount.value}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val allEmojiList = viewmodel.emojiList.value
                val selectedEmojiList = commonViewModel.selectedEmojiList.value
                val gameBundle = bundleOf()
                val playerCountValue = commonViewModel.playerCount.value
                val userId = viewmodel.userId.value
                val currentUserId = viewmodel.currentUserId.value
                val friendUserId = viewmodel.friendUserId.value
                val sessionid = viewmodel.gameSessionId.value
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = ThemePicker.primaryColor.value)
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (gameMode == GameMode.AI){
                        Row(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${commonViewModel.goldTokens}", style = TextStyle(color = Color.White, textAlign = TextAlign.Start, fontSize = 18.sp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Image(modifier = Modifier.size(32.dp), painter = painterResource(R.drawable.ic_gold_coin), contentDescription = "Gold Tokens")
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .height(80.dp)
                            .fillMaxWidth()
                    )
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.choose_text))
                            withStyle(style = SpanStyle(color = ThemePicker.secondaryColor.value)) {
                                append(stringResource(R.string.emoji_text))
                            }
                        },
                        style = headerTitle.copy(
                            color = Color.White
                        )
                    )
                    Spacer(
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                    )
                    EmojiDialogue(
                        emojiList = allEmojiList,
                        onSelectedEmojiChange = {
                            gameSound.selectSound()
                            viewmodel.onEvent(EmojiPickerUseCase.OnSelectedEmojiChange(it))
                            commonViewModel.onEvent(CommonUseCase.OnSelectedEmojiChange(it))
                        },
                        onRemoveEmojiChange = {
                            gameSound.selectSound()
                            viewmodel.onEvent(EmojiPickerUseCase.OnRemovedEmojiChange(it))
                            commonViewModel.onEvent(CommonUseCase.OnRemovedEmojiChange(it))
                        },
                        playerCount = playerCountValue,
                        playerCountReachedPopUp = {
                            Toast.makeText(
                                requireContext(),
                                "All player's selected!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        selectedEmojiListCount = selectedEmojiList.size + 1
                    )

                    if (isScreenTV(requireContext())){
                        Spacer(
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth()
                        )
                    }else{
                        Spacer(
                            modifier = Modifier
                                .height(20.dp)
                                .fillMaxWidth()
                        )
                    }


                    CustomButton(
                        onButtonClick = {
                            commonViewModel.performHapticVibrate(requireView())
                            gameSound.clickSound()
                            gameBundle.putSerializable(BundleKey.GAME_MODE, gameMode)
                            gameBundle.putSerializable(BundleKey.PLAYER_COUNT, playerCount)
                            findNavController().navigate(
                                resId = R.id.selectBoard,
                                args = gameBundle,
                                navOptions = NavOptions.navOptionStack
                            )
                        },
                        isButtonEnabled = selectedEmojiList.size == playerCountValue
                    )

                }
            }
        }
    }
}