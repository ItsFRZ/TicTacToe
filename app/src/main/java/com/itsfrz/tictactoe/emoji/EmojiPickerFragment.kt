package com.itsfrz.tictactoe.emoji

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.components.CustomButton
import com.itsfrz.tictactoe.common.components.CustomOutlinedButton
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.BoardType
import com.itsfrz.tictactoe.common.enums.GameLevel
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.enums.PlayerCount
import com.itsfrz.tictactoe.common.functionality.GameSound
import com.itsfrz.tictactoe.common.functionality.NavOptions
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.common.usecase.CommonUseCase
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.emoji.components.EmojiDialogue
import com.itsfrz.tictactoe.emoji.usecase.EmojiPickerUseCase
import com.itsfrz.tictactoe.emoji.viewmodel.EmojiPickerViewModel
import com.itsfrz.tictactoe.emoji.viewmodel.EmojiPickerViewModelFactory
import com.itsfrz.tictactoe.game.domain.usecase.GameUsecase
import com.itsfrz.tictactoe.online.viewmodel.BoardViewModel
import com.itsfrz.tictactoe.online.viewmodel.BoardViewModelFactory
import com.itsfrz.tictactoe.ui.theme.headerTitle
import com.itsfrz.tictactoe.userregistration.usecase.UserRegistrationUseCase

class EmojiPickerFragment : Fragment() {
    private lateinit var viewmodel : EmojiPickerViewModel
    private lateinit var commonViewModel: CommonViewModel
    private lateinit var gameMode : GameMode
    private lateinit var playerCount: PlayerCount
    private lateinit var gameSound: GameSound
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelFactory = EmojiPickerViewModelFactory()
        viewmodel = ViewModelProvider(this,viewModelFactory)[EmojiPickerViewModel::class.java]
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
                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(color = ThemePicker.primaryColor.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth())
                    Text(
                        text = buildAnnotatedString {
                            append("Choose")
                            withStyle(style = SpanStyle(color = ThemePicker.secondaryColor.value)){
                                append(" Emoji")
                            }
                        },
                        style = headerTitle.copy(
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth())
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
                            Toast.makeText(requireContext(), "All player's selected!", Toast.LENGTH_SHORT).show()
                        },
                        selectedEmojiListCount = selectedEmojiList.size+1
                    )
                    Spacer(modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth())
                    CustomButton(
                        onButtonClick = {
                            commonViewModel.performHapticVibrate(requireView())
                            gameSound.clickSound()
                            gameBundle.putSerializable(BundleKey.GAME_MODE, gameMode)
                            gameBundle.putSerializable(BundleKey.PLAYER_COUNT,playerCount)
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