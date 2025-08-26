package com.pumpkinprod.ludopax.lifecounter.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pumpkinprod.ludopax.lifecounter.ui.component.grid.PlayerGrid
import com.pumpkinprod.ludopax.lifecounter.viewmodel.LifeCounterViewModel

@Composable
fun LifeCounterScreen(vm: LifeCounterViewModel) {
    val uiState by vm.uiState.collectAsState()
    val players = uiState.players

    if (players.isEmpty()) {
        // Show the player selection menu
        PlayerSelection(vm = vm)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Player grid (now only needs players + viewModel)
            PlayerGrid(
                players = players,
                viewModel = vm
            )

            // Life counter menu
            LifeCounterMenu(vm = vm)
        }
    }
}
