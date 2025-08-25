package com.pumpkinprod.ludopax.nav

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pumpkinprod.ludopax.R
import com.pumpkinprod.ludopax.Routes

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
    )

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.LIFE_COUNTER,
            onClick = {
                navController.navigate(Routes.LIFE_COUNTER) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
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
            selected = currentRoute == Routes.UNDERCITY,
            onClick = {
                navController.navigate(Routes.UNDERCITY) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
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
            selected = currentRoute == Routes.PODS,
            onClick = {
                navController.navigate(Routes.PODS) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
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
            selected = currentRoute == Routes.RANDOMIZER,
            onClick = {
                navController.navigate(Routes.RANDOMIZER) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
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
