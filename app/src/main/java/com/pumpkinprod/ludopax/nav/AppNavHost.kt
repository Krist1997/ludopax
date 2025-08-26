package com.pumpkinprod.ludopax.appscreens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pumpkinprod.ludopax.Routes
import com.pumpkinprod.ludopax.dungeon.DungeonScreen
import com.pumpkinprod.ludopax.dungeon.DungeonViewModel
import com.pumpkinprod.ludopax.lifecounter.ui.screen.LifeCounterScreen
import com.pumpkinprod.ludopax.lifecounter.viewmodel.LifeCounterViewModel
import com.pumpkinprod.ludopax.pod.PodScreen
import com.pumpkinprod.ludopax.pod.PodViewModel
import com.pumpkinprod.ludopax.randomizer.RandomizerScreen
import com.pumpkinprod.ludopax.randomizer.RandomizerViewModel

object AppScreens {

    @Composable
    fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
        val lifeCounterViewModel: LifeCounterViewModel = hiltViewModel()
        val dungeonViewModel: DungeonViewModel = hiltViewModel()
        val podViewModel: PodViewModel = hiltViewModel()
        val randomizerViewModel: RandomizerViewModel = hiltViewModel()

        NavHost(
            navController = navController,
            startDestination = Routes.LIFE_COUNTER,
            modifier = modifier
        ) {
            composable(Routes.LIFE_COUNTER) {
                LifeCounterScreen(vm = lifeCounterViewModel)
            }

            composable(Routes.UNDERCITY) {
                DungeonScreen(viewModel = dungeonViewModel)
            }

            composable(Routes.PODS) {
                PodScreen(viewModel = podViewModel)
            }

            composable(Routes.RANDOMIZER) {
                RandomizerScreen(viewModel = randomizerViewModel)
            }
        }
    }
}
