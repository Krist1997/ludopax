package com.pumpkinprod.ludopax.appscreens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pumpkinprod.ludopax.Routes
import com.pumpkinprod.ludopax.lifecounter.ui.LifeCounterScreen
import com.pumpkinprod.ludopax.lifecounter.viewmodel.LifeCounterViewModel
import com.pumpkinprod.ludopax.pod.PodScreen
import com.pumpkinprod.ludopax.randomizer.RandomizerScreen
import com.pumpkinprod.ludopax.undercity.UndercityScreen

object AppScreens {

    @Composable
    fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
        val lifeCounterViewModel: LifeCounterViewModel = hiltViewModel()

        NavHost(
            navController = navController,
            startDestination = Routes.RANDOMIZER,
            modifier = modifier
        ) {
            composable(Routes.LIFE_COUNTER) {
                LifeCounterScreen(vm = lifeCounterViewModel)
            }

            composable(Routes.UNDERCITY) { UndercityScreen() }
            composable(Routes.PODS) { PodScreen() }
            composable(Routes.RANDOMIZER) { RandomizerScreen() }
        }
    }

}
