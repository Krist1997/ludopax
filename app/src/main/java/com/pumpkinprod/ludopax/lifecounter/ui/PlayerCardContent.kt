package com.pumpkinprod.ludopax.lifecounter.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pumpkinprod.ludopax.lifecounter.model.CounterType
import com.pumpkinprod.ludopax.lifecounter.model.PlayerState

@Composable
fun PlayerCardContent(
    player: PlayerState,
    onIncrement: (CounterType, Int) -> Unit,
    onSwitchCounter: (CounterType) -> Unit
) {
    val currentCounter = player.activeCounter
    val value = player.counters[currentCounter] ?: 0

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Decide layout density depending on available height (proxy for player count)
        val density = when {
            maxHeight < 150.dp -> "compact"   // ~6 players
            maxHeight < 250.dp -> "medium"    // ~3–4 players
            else -> "large"                   // 1–2 players
        }

        val (titleSize, valueSize, buttonPadding) = when (density) {
            "compact" -> Triple(12.sp, 28.sp, 2.dp)
            "medium"  -> Triple(14.sp, 36.sp, 4.dp)
            else      -> Triple(16.sp, 48.sp, 6.dp)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer {
                // Prevent weird scaling issues, just fit to card
                scaleX = 0.95f
                scaleY = 0.95f
            }
        ) {
            // Counter value
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = valueSize)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Active counter type (Life, Poison, Energy, Exp)
            Text(
                text = currentCounter.label,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = titleSize)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Increment/decrement buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    contentPadding = PaddingValues(horizontal = buttonPadding, vertical = 2.dp),
                    onClick = { onIncrement(currentCounter, -1) }
                ) { Text("-1") }

                Button(
                    contentPadding = PaddingValues(horizontal = buttonPadding, vertical = 2.dp),
                    onClick = { onIncrement(currentCounter, +1) }
                ) { Text("+1") }
            }

            // ⚠ Removed the row with CounterType switch buttons
        }
    }
}
