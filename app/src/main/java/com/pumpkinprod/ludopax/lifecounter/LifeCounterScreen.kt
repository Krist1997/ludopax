package com.pumpkinprod.ludopax.lifecounter

import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LifeCounterScreen(
    vm: LifeCounterViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val players = uiState.players

    if (players.isEmpty()) {
        PlayerSelection(vm)
    } else {
        LifeCounterContent(players, vm)
    }
}

@Composable
private fun PlayerSelection(vm: LifeCounterViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select number of players")
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 3, 4, 5, 6).forEach { n ->
                Button(onClick = { vm.setPlayers(n) }) { Text("$n") }
            }
        }
    }
}

@Composable
private fun LifeCounterContent(players: List<PlayerState>, vm: LifeCounterViewModel) {
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    val layout = rowSpec(players.size, isLandscape)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            var index = 0
            layout.forEach { colsInRow ->
                Row(Modifier.weight(1f)) {
                    repeat(colsInRow) {
                        val p = players[index]
                        PlayerCard(
                            player = p,
                            onIncrement = { type, delta -> vm.incrementCounter(p.id, type, delta) },
                            onSwitchCounter = { newType -> vm.switchCounter(p.id, newType) },
                            modifier = Modifier.weight(1f)
                        )
                        index++
                    }
                }
            }
        }

        // Center FAB menu
        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.align(Alignment.Center)) {
            FloatingActionButton(
                onClick = { expanded = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) { Text("≡") }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Reset Life & Counters") },
                    onClick = {
                        vm.reset()
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Change Player Count") },
                    onClick = {
                        vm.clearPlayers()
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun rowSpec(count: Int, landscape: Boolean): List<Int> {
    val portrait = when (count) {
        2 -> listOf(1, 1)
        3 -> listOf(1, 2)
        4 -> listOf(2, 2)
        5 -> listOf(2, 3)
        6 -> listOf(3, 3)
        else -> listOf(count)
    }

    val landscapeSpec = when (count) {
        3 -> listOf(3)
        4 -> listOf(4)
        5 -> listOf(3, 2)
        6 -> listOf(3, 3)
        else -> portrait
    }

    return if (landscape && count > 2) landscapeSpec else portrait
}

@Composable
private fun PlayerCard(
    player: PlayerState,
    onIncrement: (CounterType, Int) -> Unit,
    onSwitchCounter: (CounterType) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCounter = player.activeCounter
    val value = player.counters[currentCounter] ?: 0
    var dragAccum by remember { mutableStateOf(0f) }

    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxSize()
            .pointerInput(currentCounter) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccum = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        dragAccum += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        if (dragAccum < -60f) onSwitchCounter(nextCounter(currentCounter))
                        else if (dragAccum > 60f) onSwitchCounter(prevCounter(currentCounter))
                        dragAccum = 0f
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = player.color.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Player ${player.id + 1}", style = MaterialTheme.typography.titleMedium)
            Text("$value", style = MaterialTheme.typography.headlineLarge)
            Text(currentCounter.label, style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onIncrement(currentCounter, -1) }) { Text("-1") }
                Button(onClick = { onIncrement(currentCounter, +1) }) { Text("+1") }
            }
        }
    }
}

private fun nextCounter(type: CounterType): CounterType {
    val v = CounterType.values()
    return v[(type.ordinal + 1) % v.size]
}

private fun prevCounter(type: CounterType): CounterType {
    val v = CounterType.values()
    return v[(type.ordinal - 1 + v.size) % v.size]
}
