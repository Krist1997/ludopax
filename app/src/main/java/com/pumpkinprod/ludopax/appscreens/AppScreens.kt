package com.pumpkinprod.ludopax.appscreens

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pumpkinprod.ludopax.R
import com.pumpkinprod.ludopax.lifecounter.LifeCounterScreen
import com.pumpkinprod.ludopax.pod.PodScreen
import com.pumpkinprod.ludopax.randomizer.RandomizerScreen
import com.pumpkinprod.ludopax.undercity.UndercityScreen

object AppScreens {

    @Composable
    fun AppNavHost(
        navController: NavHostController,
        modifier: Modifier = Modifier
    ) {
        NavHost(
            navController = navController,
            startDestination = "randomizer",
            modifier = modifier
        ) {
            composable("lifecounter") { LifeCounterScreen() }
            composable("undercity") { UndercityScreen() }
            composable("pods") { PodScreen() }
            composable("randomizer") { RandomizerScreen() }
        }
    }

    @Composable
    fun BottomNavBar(navController: NavHostController) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        val navBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
        )

        NavigationBar {
            NavigationBarItem(
                selected = currentRoute == "lifecounter",
                onClick = { navController.navigate("lifecounter") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lifecounter),
                        contentDescription = "Life Counter",
                        modifier = Modifier.size(50.dp)
                    )
                },
                colors = navBarItemColors
            )
            NavigationBarItem(
                selected = currentRoute == "undercity",
                onClick = { navController.navigate("undercity") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_undercity),
                        contentDescription = "Undercity",
                        modifier = Modifier.size(50.dp)
                    )
                },
                colors = navBarItemColors
            )
            NavigationBarItem(
                selected = currentRoute == "pods",
                onClick = { navController.navigate("pods") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pods),
                        contentDescription = "Pods",
                        modifier = Modifier.size(50.dp)
                    )
                },
                colors = navBarItemColors
            )
            NavigationBarItem(
                selected = currentRoute == "randomizer",
                onClick = { navController.navigate("randomizer") },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_randomizer),
                        contentDescription = "Randomizer",
                        modifier = Modifier.size(50.dp)
                    )
                },
                colors = navBarItemColors
            )
        }
    }
}