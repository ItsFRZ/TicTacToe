package com.itsfrz.tictactoe.support.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.support.data.SupportTier
import com.itsfrz.tictactoe.support.manager.UpiLauncher
import com.itsfrz.tictactoe.ui.theme.SurfaceDark

@Composable
fun SupportScreen(
    purchase : Boolean,
    onPayment : (activity : Activity, amount : Int) -> Unit
) {

    val activity = LocalContext.current as Activity

    val tiers = remember {
        if (purchase){
            listOf(
                SupportTier(
                    "Best",
                    49,
                    "🪙",
                    "${1*100000} gold coins"
                ),
                SupportTier(
                    "Legend",
                    149,
                    "💰",
                    "1 Bag of coins worth ${3*100000}"
                ),
                SupportTier(
                    "Insane",
                    499,
                    "🌟",
                    "10 Bag of coins worth ${10*100000}"
                )
            )
        }else{
            listOf(
                SupportTier(
                    "Coffee Drop",
                    49,
                    "☕",
                    "Support nightly builds"
                ),
                SupportTier(
                    "Pizza Patch",
                    149,
                    "🍕",
                    "Fuel new features"
                ),
                SupportTier(
                    "Legendary",
                    499,
                    "🏆",
                    "Become hall of fame"
                )
            )
        }


    }

    var selectedAmount by remember {
        mutableIntStateOf(49)
    }

    Scaffold(
        containerColor = SurfaceDark
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            item {
                HeroSection(purchase)
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))
            }

            items(tiers) { tier ->
                SupportTierCard(
                    purchase = purchase,
                    tier = tier,
                    selected = tier.amount == selectedAmount,
                    onClick = {
                        selectedAmount = tier.amount
                    }
                )
            }

            item {

                Spacer(modifier = Modifier.height(28.dp))

                AnimatedSupportButton(
                    purchase = purchase,
                    amount = selectedAmount
                ) {
                    onPayment(activity,selectedAmount)
                }

                SecurityFooter()
            }
        }
    }
}