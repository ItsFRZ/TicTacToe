package com.itsfrz.tictactoe.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.itsfrz.tictactoe.common.components.Separator
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.goonline.data.models.Stats
import com.itsfrz.tictactoe.stats.components.PlayerStatsCompact
import com.itsfrz.tictactoe.stats.components.PlayerStatsComponent
import com.itsfrz.tictactoe.stats.components.StatsAvatar
import com.itsfrz.tictactoe.ui.theme.headerTitle

class StatsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = ThemePicker.secondaryColor.value)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    PlayerStatsComponent(
                        username = "Faraz Sheikh",
                        rank = 181,
                        level = 32,
                        playTime = 200,
                        wins = 50,
                        lose = 20
                    )
                    StatsAvatar(username = "Faraz Sheikh")
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = ThemePicker.primaryColor.value),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = ThemePicker.secondaryColor.value)) {
                                    append("Leader")
                                }
                                append(" Board")
                            },
                            style = headerTitle.copy(color = Color.White)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Separator(ThemePicker.secondaryColor.value)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(10) {
                                PlayerStatsCompact(
                                    playerStats = Stats(
                                        userId = "12343",
                                        username = "Faisal Sheikh",
                                        rank = 1,
                                        level = 999,
                                        playTime = 1203,
                                        wins = 500,
                                        lose = 150
                                    )
                                )
                                Separator(color = ThemePicker.themeStatsTextHeading.value)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }

}