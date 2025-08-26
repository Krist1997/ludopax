package com.pumpkinprod.ludopax.lifecounter.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pumpkinprod.ludopax.lifecounter.viewmodel.LifeCounterViewModel

@Composable
fun LifeCounterMenu(vm: LifeCounterViewModel) {
    var menuOpen by remember { mutableStateOf(false) }
    var resetDialogOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        FloatingActionButton(onClick = { menuOpen = !menuOpen }) {
            Text("Menu")
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false }
        ) {
            DropdownMenuItem(
                text = { Text("Reset") },
                onClick = {
                    menuOpen = false
                    resetDialogOpen = true
                }
            )
            DropdownMenuItem(
                text = { Text("Back to Player Selection") },
                onClick = {
                    menuOpen = false
                    vm.clearPlayers()
                }
            )
        }

        if (resetDialogOpen) {
            ConfirmResetDialog(
                title = "Reset Game",
                message = "Are you sure you want to reset all player counters?",
                onConfirm = {
                    resetDialogOpen = false
                    vm.reset()
                },
                onDismiss = { resetDialogOpen = false }
            )
        }
    }
}
