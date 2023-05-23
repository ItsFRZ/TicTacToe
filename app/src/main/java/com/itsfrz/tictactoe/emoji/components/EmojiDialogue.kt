package com.itsfrz.tictactoe.emoji.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.ui.theme.ThemeButtonBackground
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.common.state.EmojiState

@Composable
fun EmojiDialogue(
    emojiList : List<EmojiState>,
    onSelectedEmojiChange : (selectedEmoji : Int) -> Unit,
    onRemoveEmojiChange : (removedEmoji : Int) -> Unit,
    playerCount : Int,
    playerCountReachedPopUp : () -> Unit,
    selectedEmojiListCount: Int
) {
    var selectedPlayer by remember {
        mutableStateOf(selectedEmojiListCount)
    }
    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(400.dp)
            .padding(4.dp),
        elevation = 8.dp,
        backgroundColor = ThemePicker.themeBoardBackground.value,
        border = BorderStroke(width = 0.4.dp, color = ThemePicker.themeButtonBorder.value),
        shape = RoundedCornerShape(8.dp)
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(3),
        ){
            itemsIndexed(
                items = emojiList,
                key = {index: Int, item: EmojiState ->  item.emojiResourceId }
            ){ index: Int, item: EmojiState ->
                Box(modifier = Modifier.clickable {
                    if (item.isSelected){
                        onRemoveEmojiChange(index)
                        selectedPlayer-=1
                    }else{
                        if (selectedPlayer <= playerCount){
                            onSelectedEmojiChange(index)
                            selectedPlayer+=1
                        }else{
                            playerCountReachedPopUp()
                        }
                    }
                },contentAlignment = Alignment.Center) {
                    Image(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        painter = painterResource(id = item.emojiResourceId),
                        contentDescription = "Emoji Icon",
                        contentScale = ContentScale.Fit
                    )
                    if (item.isSelected){
                        Image(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            painter = painterResource(id = R.drawable.ic_right),
                            contentDescription = "Selected Emoji Icon"
                        )
                    }
                }
            }
        }
    }
}