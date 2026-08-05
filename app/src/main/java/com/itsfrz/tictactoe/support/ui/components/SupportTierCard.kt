package com.itsfrz.tictactoe.support.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.support.data.SupportTier
import com.itsfrz.tictactoe.ui.theme.NavyPrimary
import com.itsfrz.tictactoe.ui.theme.OrangePrimary
import com.itsfrz.tictactoe.ui.theme.SurfaceCard
import com.itsfrz.tictactoe.ui.theme.TextPrimary

@Composable
fun SupportTierCard(
    purchase : Boolean,
    tier: SupportTier,
    selected: Boolean,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                if (purchase){
                    if (selected)
                        NavyPrimary.copy(alpha = 0.15f)
                    else
                        SurfaceCard
                }else {
                    if (selected)
                        OrangePrimary.copy(alpha = 0.15f)
                    else
                        SurfaceCard
                }

        ),
        border = BorderStroke(
            width = 1.dp,
            color = if(purchase){
                if (selected)
                    NavyPrimary
                else
                    Color.Transparent
            }else{
                if (selected)
                    OrangePrimary
                else
                    Color.Transparent
            }
        ),
        shape = RoundedCornerShape(28.dp)
    ) {

        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = tier.emoji,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column {

                Text(
                    text = tier.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = tier.subtitle,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "$${tier.amount}",
                color = if(purchase) Color.White else OrangePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
    }
}