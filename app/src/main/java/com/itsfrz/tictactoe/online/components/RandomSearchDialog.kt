package com.itsfrz.tictactoe.online.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.snap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.ThemeDialogBackground
import com.itsfrz.tictactoe.ui.theme.headerSubTitle

@Composable
fun RandomSearchDialog(
    @DrawableRes emojiDataList : List<Int>,
    @DrawableRes userAvatar : Int,
    isDialogueEnabled : Boolean,
    onCancelPlayRequest : () -> Unit,
    listState : LazyListState
) {
    if (isDialogueEnabled){
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        painter = painterResource(id = userAvatar),
                        contentDescription = "Emoji Icon",
                        contentScale = ContentScale.Fit
                    )
                    Text(text = "VS", style = headerSubTitle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ThemePicker.secondaryColor.value))
//                    PlayerSearchScroller(emojiDataList, listState)
                }
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
}

//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun PlayerSearchScroller(emojiDataList: List<Int>,listState : LazyListState) {
//    LazyColumn(
//        modifier = Modifier.size(80.dp),
//        state = listState,
//        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
//    ){
//        items(emojiDataList.size){
//            Image(
//                modifier = Modifier
//                    .size(60.dp)
//                    .padding(horizontal = 4.dp, vertical = 6.dp),
//                painter = painterResource(id = it),
//                contentDescription = "Emoji Icon",
//                contentScale = ContentScale.Fit
//            )
//        }
//    }
//}
