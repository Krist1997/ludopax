package com.pumpkinprod.ludopax.lifecounter.ui

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import com.pumpkinprod.ludopax.lifecounter.model.CounterType
import com.pumpkinprod.ludopax.lifecounter.model.PlayerState

@Composable
fun PlayerCard(
    player: PlayerState,
    onIncrement: (CounterType, Int) -> Unit,
    onSwitchCounter: (CounterType) -> Unit,
    modifier: Modifier = Modifier,
    rotationAngle: Float = 0f,      // card rotation
) {
    var dragAccum by remember { mutableFloatStateOf(0f) }
    val currentCounter = player.activeCounter

    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxSize()
            .pointerInput(currentCounter, rotationAngle) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        var accumulated = 0f

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: continue

                            // Only process if the pointer is pressed
                            if (!change.pressed) break

                            val dx = change.positionChange().x
                            val dy = change.positionChange().y

                            // Adjust according to rotation
                            val adjustedDrag = when (rotationAngle % 360) {
                                0f -> dy
                                180f -> -dy
                                90f -> -dx
                                270f -> dx
                                else -> dy
                            }

                            accumulated += adjustedDrag
                            change.consume()
                        }

                        // Trigger counter switch if drag threshold is reached
                        when {
                            accumulated < -60f -> onSwitchCounter(nextCounter(currentCounter))
                            accumulated > 60f -> onSwitchCounter(prevCounter(currentCounter))
                        }
                    }
                }
            }


,
        colors = CardDefaults.cardColors(containerColor = player.color.copy(alpha = 0.28f))
    ) {
        // Rotate the whole card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = rotationAngle
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                }
        ) {
            PlayerCardContent(
                player = player,
                onIncrement = onIncrement,
                onSwitchCounter = onSwitchCounter,
            )
        }
    }
}

private fun nextCounter(type: CounterType): CounterType {
    val v = CounterType.entries.toTypedArray()
    return v[(type.ordinal + 1) % v.size]
}

private fun prevCounter(type: CounterType): CounterType {
    val v = CounterType.entries.toTypedArray()
    return v[(type.ordinal - 1 + v.size) % v.size]
}
