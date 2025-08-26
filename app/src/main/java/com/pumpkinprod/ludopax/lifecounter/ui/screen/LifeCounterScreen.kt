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
        PlayerSelection(vm = vm)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            PlayerGrid(players = players, viewModel = vm)
            LifeCounterMenu(vm = vm)
        }
    }
}
