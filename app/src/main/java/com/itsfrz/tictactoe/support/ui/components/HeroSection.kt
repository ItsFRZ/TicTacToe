package com.itsfrz.tictactoe.support.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.NavyPrimary
import com.itsfrz.tictactoe.ui.theme.NavySecondary
import com.itsfrz.tictactoe.ui.theme.OrangePrimary
import com.itsfrz.tictactoe.ui.theme.OrangeSecondary
import com.itsfrz.tictactoe.ui.theme.ThemeBlue


@Composable
fun HeroSection(purchase : Boolean) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    if (purchase){
                        listOf(
                            NavyPrimary,
                            NavySecondary
                        )
                    }else{
                        listOf(
                            OrangePrimary,
                            OrangeSecondary
                        )
                    }
                )
            )
            .padding(28.dp)
    ) {

        Text(
            text = "Orange Labs",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Open Source Games • Free Forever",
            color = Color.White.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (purchase) "Coins will credited, immediately as transaction proceed to success. \n\n Wishing you best of luck from Orange Labs" else "Support future builds, better multiplayer, accessibility, and indie innovation.",
            color = Color.White
        )
    }
}