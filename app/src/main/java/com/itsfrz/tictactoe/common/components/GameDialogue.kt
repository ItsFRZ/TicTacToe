package com.itsfrz.tictactoe.common.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.enums.GameResult
import com.itsfrz.tictactoe.common.functionality.GameSound
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.ui.theme.*
import kotlinx.coroutines.delay

object GameDialogue{

    private lateinit var gameSound: GameSound
    fun setDialogSound(gameSound: GameSound){
        this.gameSound = gameSound
    }
    @Composable
    fun GameDrawLoseDialogue(
        gameResult : GameResult,
        commonViewModel: CommonViewModel,
        onCloseEvent : () -> Unit,
        onDialogueButtonClick : () -> Unit,
    ) {
        val view = LocalView.current
        LaunchedEffect(Unit){
            gameSound.triggerPopSound()
        }
        LaunchedEffect(Unit){
            delay(3000)
        }
        Card(
            modifier = Modifier
                .fillMaxWidth(7f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(8.dp),
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .background(ThemeDialogBackground),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp, end = 7.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(25.dp)
                            .border(
                                border = BorderStroke(width = 0.8.dp, color = ThemePicker.secondaryColor.value),
                                shape = RoundedCornerShape(100)
                            )
                            .padding(8.dp),
                        onClick = {
                            gameSound.clickSound()
                            commonViewModel.performHapticVibrate(view)
                            onCloseEvent()
                        }
                    ){
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross),
                            contentDescription = "Back Icon",
                            tint = ThemePicker.secondaryColor.value
                        )
                    }
                }
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(5.dp))
                Text(text = if (gameResult == GameResult.DRAW) "DRAW" else "You Lose!", style = headerTitle.copy(color = ThemePicker.secondaryColor.value, fontSize = 20.sp))
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(12.dp))
                when(gameResult){
                    GameResult.DRAW -> {
                        Text(text = "Better Luck Next Time!", style = headerSubTitle.copy(fontSize = 17.sp, fontStyle = FontStyle.Italic, color = ThemePicker.secondaryColor.value))
                    }
                    GameResult.LOSE -> {
                        Icon(modifier = Modifier.size(50.dp), painter = painterResource(id = R.drawable.ic_game_retry), contentDescription = "Game Retry", tint = ThemePicker.secondaryColor.value)
                    }
                    else ->{}
                }
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(5.dp))
                CustomButton(
                    onButtonClick = {
                        gameSound.clickSound()
                        commonViewModel.performHapticVibrate(view)
                        onDialogueButtonClick() },
                    buttonText = if (gameResult == GameResult.DRAW) "Play Again" else "Retry"
                )
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(5.dp))
            }
        }

    }

    @Composable
    fun GameWinDialogue(
        winnerUsername: String,
        dialogueButtonText : String,
        onCloseEvent : () -> Unit,
        commonViewModel: CommonViewModel,
        onDialogueButtonClick : () -> Unit,
    ){
        LaunchedEffect(Unit){
            gameSound.triggerPopSound()
        }
        val view = LocalView.current
        LaunchedEffect(Unit){
            delay(3000)
        }
        Card(
            modifier = Modifier
                .fillMaxWidth(7f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(2.dp),
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .background(ThemeDialogBackground),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp, end = 7.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(25.dp)
                            .border(
                                border = BorderStroke(width = 0.8.dp, color = ThemePicker.secondaryColor.value),
                                shape = RoundedCornerShape(100)
                            )
                            .padding(8.dp),
                        onClick = {
                            gameSound.clickSound()
                            commonViewModel.performHapticVibrate(view)
                            onCloseEvent()
                        }
                    ){
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross),
                            contentDescription = "Back Icon",
                            tint = ThemePicker.secondaryColor.value
                        )
                    }
                }
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(5.dp))
                Text(text = "WINNER", style = headerTitle.copy(color = ThemePicker.secondaryColor.value, fontSize = 20.sp))
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(12.dp))
                AnimatedStarLayout(starLayoutBackground = ThemeDialogBackground, starInactiveColor = PrimaryDark)
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(2.dp))
                Text(text = winnerUsername, style = headerSubTitle.copy(color = ThemePicker.secondaryColor.value, fontSize = 14.sp))
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(5.dp))
                CustomButton(
                    onButtonClick = {
                        gameSound.clickSound()
                        commonViewModel.performHapticVibrate(view)
                        onDialogueButtonClick()
                                    },
                    buttonText = dialogueButtonText
                )
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(5.dp))
            }
        }
    }

    private @Composable
    fun AnimatedStarLayout(
        starLayoutBackground : Color,
        starInactiveColor : Color
    ) {
        val starOneColor = remember {
            Animatable(starInactiveColor)
        }
        val starTwoColor = remember {
            Animatable(starInactiveColor)
        }
        val starThreeColor = remember {
            Animatable(starInactiveColor)
        }
        
        LaunchedEffect(Unit){
            starOneColor.animateTo(targetValue = ThemePicker.secondaryColor.value, animationSpec = tween(durationMillis = 200, delayMillis = 30, easing = EaseInBounce))
            delay(60)
            gameSound.activeStarSound()
            starTwoColor.animateTo(targetValue = ThemePicker.secondaryColor.value, animationSpec = tween(durationMillis = 200, delayMillis = 30, easing = EaseInBounce))
            delay(60)
            starThreeColor.animateTo(targetValue = ThemePicker.secondaryColor.value, animationSpec = tween(durationMillis = 200, delayMillis = 30, easing = EaseInBounce))
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(starLayoutBackground),
            horizontalArrangement = Arrangement.Center
        ) {

            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(starLayoutBackground)
                    .height(100.dp)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_star), contentDescription = "Winning Star", tint = starOneColor.value)
            }


            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(starLayoutBackground)
                    .height(100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_star), contentDescription = "Winning Star", tint = starTwoColor.value)
            }


            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(starLayoutBackground)
                    .height(100.dp)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_star), contentDescription = "Winning Star", tint = starThreeColor.value)
            }
        }
    }

    @Composable
    fun GameDialog(
        onExitEvent : () -> Unit,
        onContinueEvent : () -> Unit,
        headerText : String = "EXIT",
        titleText : String = "Do you really want to exit ?",
        buttonText : String = "Agree",
        commonViewModel: CommonViewModel
    ) {
        LaunchedEffect(Unit){
            gameSound.triggerPopSound()
        }
        val view = LocalView.current
        Card(
            modifier = Modifier
                .fillMaxWidth(7f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(2.dp),
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .background(ThemeDialogBackground),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 7.dp, end = 7.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(25.dp)
                            .border(
                                border = BorderStroke(width = 0.8.dp, color = ThemePicker.secondaryColor.value),
                                shape = RoundedCornerShape(100)
                            )
                            .padding(8.dp),
                        onClick = {
                            gameSound.clickSound()
                            commonViewModel.performHapticVibrate(view)
                            onContinueEvent()
                        }
                    ){
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross),
                            contentDescription = "Back Icon",
                            tint = ThemePicker.secondaryColor.value
                        )
                    }
                }
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(5.dp))
                Text(text =headerText, style = headerTitle.copy(color = ThemePicker.secondaryColor.value, fontSize = 20.sp))
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(12.dp))
                Text(text = titleText, style = headerSubTitle.copy(fontSize = 17.sp, fontStyle = FontStyle.Normal, color = ThemePicker.secondaryColor.value))
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(10.dp))
                CustomButton(
                    onButtonClick = {
                        gameSound.clickSound()
                        commonViewModel.performHapticVibrate(view)
                        onExitEvent() },
                    buttonText = buttonText
                )
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(5.dp))
            }
        }
    }


    @Composable
    fun CommonLoadingScreen() {
        LaunchedEffect(Unit){
            gameSound.triggerPopSound()
        }
        Card(
            modifier = Modifier
                .fillMaxWidth(.6f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(2.dp),
            elevation = 10.dp,
            backgroundColor = ThemeDialogBackground
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 25.dp)
                    .background(ThemeDialogBackground),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(5.dp))
                CircularProgressIndicator(modifier = Modifier.size(30.dp), color = ThemePicker.secondaryColor.value)
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(12.dp))
                Text(text = "Please wait ...", style = headerSubTitle.copy(fontSize = 17.sp, fontStyle = FontStyle.Normal, color = ThemePicker.secondaryColor.value))
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(10.dp))
            }
        }
    }

    @Composable
    fun PlayRequestBox(
        onCloseClick : () -> Unit,
        commonViewModel: CommonViewModel
    ) {
        LaunchedEffect(Unit){
            gameSound.triggerPopSound()
        }
        val view = LocalView.current
        Card(
            modifier = Modifier
                .fillMaxWidth(.7f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(8.dp),
            elevation = 10.dp,
            backgroundColor = ThemeDialogBackground
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 22.dp)
                    .background(ThemeDialogBackground),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp, end = 7.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(22.dp)
                            .border(
                                border = BorderStroke(width = 0.8.dp, color = ThemePicker.secondaryColor.value),
                                shape = RoundedCornerShape(100)
                            )
                            .padding(8.dp),
                        onClick = {
                            gameSound.clickSound()
                           commonViewModel.performHapticVibrate(view)
                            onCloseClick()
                        }
                    ){
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross),
                            contentDescription = "Back Icon",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(4.dp))
                Text(text = "WAIT", style = headerTitle.copy(color = Color.White, fontSize = 20.sp))
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(12.dp))
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(5.dp))
                CircularProgressIndicator(modifier = Modifier.size(40.dp), color = ThemePicker.secondaryColor.value)
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(12.dp))
                Text(text = "Please wait for your opponent\nto connect ...", style = headerSubTitle.copy(fontSize = 12.sp, fontStyle = FontStyle.Italic, color = ThemePicker.secondaryColor.value, fontWeight = FontWeight.Light))
                Spacer(modifier = Modifier
                    .wrapContentWidth()
                    .height(10.dp))
            }
        }
    }
}



