package com.pumpkinprod.ludopax.lifecounter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
            // Player grid
            PlayerGrid(
                players = players,
                onIncrement = { player, type, delta ->
                    vm.incrementCounter(player.id, type, delta)
                },
                onSwitchCounter = { player, type ->
                    vm.switchCounter(player.id, type)
                }
            )

            // Life counter menu
            LifeCounterMenu(vm = vm)
        }
    }
}
