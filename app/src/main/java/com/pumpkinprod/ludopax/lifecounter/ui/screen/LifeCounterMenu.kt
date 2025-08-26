package com.pumpkinprod.ludopax.lifecounter.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pumpkinprod.ludopax.lifecounter.viewmodel.LifeCounterViewModel

@Composable
fun LifeCounterMenu(vm: LifeCounterViewModel) {
    var showMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Centered floating menu button
        FloatingActionButton(onClick = { showMenu = !showMenu }) {
            Text("Menu")
        }

        // Dropdown menu options
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Reset") },
                onClick = {
                    showMenu = false
                    showResetDialog = true
                }
            )
            DropdownMenuItem(
                text = { Text("Back to Player Selection") },
                onClick = {
                    showMenu = false
                    vm.clearPlayers()
                }
            )
        }

        // Confirmation dialog for reset
        if (showResetDialog) {
            ConfirmResetDialog(
                title = "Reset Game",
                message = "Are you sure you want to reset all player counters?",
                onConfirm = {
                    showResetDialog = false
                    vm.reset()
                },
                onDismiss = { showResetDialog = false }
            )
        }
    }
}
