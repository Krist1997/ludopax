package com.pumpkinprod.ludopax.lifecounter

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
private fun PlayerCard(
    player: PlayerState,
    onIncrement: (CounterType, Int) -> Unit,
    onSwitchCounter: (CounterType) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentCounter by remember { mutableStateOf(player.activeCounter) }

    val value = player.counters[currentCounter] ?: 0

    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -20) { // swipe up
                        currentCounter = nextCounter(currentCounter)
                        onSwitchCounter(currentCounter)
                    } else if (dragAmount > 20) { // swipe down
                        currentCounter = prevCounter(currentCounter)
                        onSwitchCounter(currentCounter)
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = player.color.copy(alpha = 0.28f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Player ${player.id + 1}", style = MaterialTheme.typography.titleMedium)
            Text("${value}", style = MaterialTheme.typography.headlineLarge)
            Text(currentCounter.label, style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onIncrement(currentCounter, -1) }) { Text("-1") }
                Button(onClick = { onIncrement(currentCounter, +1) }) { Text("+1") }
            }
        }
    }
}

private fun nextCounter(type: CounterType): CounterType {
    val values = CounterType.values()
    val i = (type.ordinal + 1) % values.size
    return values[i]
}

private fun prevCounter(type: CounterType): CounterType {
    val values = CounterType.values()
    val i = (type.ordinal - 1 + values.size) % values.size
    return values[i]
}
