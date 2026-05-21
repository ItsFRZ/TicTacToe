package com.itsfrz.tictactoe.support.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsfrz.tictactoe.common.functionality.ThemePicker
import com.itsfrz.tictactoe.ui.theme.NavyPrimary
import com.itsfrz.tictactoe.ui.theme.OrangePrimary

@Composable
fun AnimatedSupportButton(
    purchase : Boolean,
    amount: Int,
    onClick: () -> Unit
) {

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        )
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(62.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (purchase) NavyPrimary else OrangePrimary
        )
    ) {

        Text(
            text = "${if (purchase) "Buy" else "Support"} ₹$amount",
            fontSize = 18.sp
        )
    }
}