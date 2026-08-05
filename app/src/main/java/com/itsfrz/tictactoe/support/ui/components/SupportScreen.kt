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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.support.data.SupportTier
import com.itsfrz.tictactoe.support.manager.UpiLauncher
import com.itsfrz.tictactoe.ui.theme.SurfaceDark

@Composable
fun SupportScreen(
    purchase : Boolean,
    onPayment : (activity : Activity, amount : Int) -> Unit
) {

    val activity = LocalContext.current as Activity
    val best = stringResource(R.string.sbest_text)
    val legend = stringResource(R.string.legend_text)
    val insane = stringResource(R.string.insane_text)
    val goldCoin =  stringResource(R.string.gold_coins_text)
    val bagCoin =  stringResource(R.string.bag_coin_text)
    val manyBagCoin =  stringResource(R.string.many_bag_coin_text)


    val coffeeDrop = stringResource(R.string.coffee_drop_text)
    val support = stringResource(R.string.support_text)
    val pizza = stringResource(R.string.pizza_text)
    val fuel = stringResource(R.string.fuel_text)
    val legendary = stringResource(R.string.legendary_text)
    val fame = stringResource(R.string.fame_text)

    val tiers = remember {
        if (purchase){
            listOf(
                SupportTier(
                    best,
                    11,
                    "🪙",
                    "${1*1100} \n"+goldCoin
                ),
                SupportTier(
                    legend,
                    25,
                    "💰",
                    bagCoin+"\n ${1*2500}"
                ),
                SupportTier(
                    insane,
                    49,
                    "🌟",
                    manyBagCoin+"\n ${1*4900}"
                )
            )
        }else{
            listOf(
                SupportTier(
                    coffeeDrop,
                    100,
                    "☕",
                    support
                ),
                SupportTier(
                    pizza,
                    200,
                    "🍕",
                    fuel
                ),
                SupportTier(
                    legendary,
                    500,
                    "🏆",
                    fame
                )
            )
        }


    }

    var selectedAmount by remember {
        mutableIntStateOf(11)
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