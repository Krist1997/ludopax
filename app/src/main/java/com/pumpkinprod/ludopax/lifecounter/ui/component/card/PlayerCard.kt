package com.pumpkinprod.ludopax.lifecounter.ui.component.card

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pumpkinprod.ludopax.lifecounter.domain.CounterType
import com.pumpkinprod.ludopax.lifecounter.domain.PlayerState
import com.pumpkinprod.ludopax.lifecounter.viewmodel.LifeCounterViewModel
import kotlin.math.abs

@Composable
fun PlayerCard(
    player: PlayerState,
    viewModel: LifeCounterViewModel,
    modifier: Modifier = Modifier,
    rotationAngle: Float = 0f
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = player.color.copy(alpha = 0.28f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PlayerCardContent(
                player = player,
                viewModel = viewModel,
                rotationAngle = rotationAngle
            )
        }
    }
}

@Composable
fun PlayerCardContent(
    player: PlayerState,
    viewModel: LifeCounterViewModel,
    rotationAngle: Float = 0f
) {
    val angle = normalizedAngle(rotationAngle)
    val isHorizontal = angle == 0f || angle == 180f
    val leftIsPlus = angle == 180f
    val topIsPlus = angle == 270f

    Box(modifier = Modifier.fillMaxSize()) {
        if (isHorizontal) {
            Row(Modifier.fillMaxSize()) {
                // Left tappable strip
                CounterTapStrip(
                    plus = leftIsPlus,
                    onIncrement = { delta ->
                        // always fetch the current active counter
                        val currentType = viewModel.uiState.value.players
                            .first { it.id == player.id }.activeCounter
                        viewModel.incrementCounter(player.id, currentType, delta)
                    },
                    modifier = Modifier
                        .width(72.dp)
                        .fillMaxHeight(),
                    alignment = Alignment.CenterStart
                )

                // Center swipe area
                CounterSwipeArea(player, viewModel, angle, Modifier.weight(1f))

                // Right tappable strip
                CounterTapStrip(
                    plus = !leftIsPlus,
                    onIncrement = { delta ->
                        val currentType = viewModel.uiState.value.players
                            .first { it.id == player.id }.activeCounter
                        viewModel.incrementCounter(player.id, currentType, delta)
                    },
                    modifier = Modifier
                        .width(72.dp)
                        .fillMaxHeight(),
                    alignment = Alignment.CenterEnd
                )
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                // Top tappable strip
                CounterTapStrip(
                    plus = topIsPlus,
                    onIncrement = { delta ->
                        val currentType = viewModel.uiState.value.players
                            .first { it.id == player.id }.activeCounter
                        viewModel.incrementCounter(player.id, currentType, delta)
                    },
                    modifier = Modifier
                        .height(72.dp)
                        .fillMaxWidth(),
                    alignment = Alignment.Center
                )

                // Center swipe area
                CounterSwipeArea(player, viewModel, angle, Modifier.weight(1f))

                // Bottom tappable strip
                CounterTapStrip(
                    plus = !topIsPlus,
                    onIncrement = { delta ->
                        val currentType = viewModel.uiState.value.players
                            .first { it.id == player.id }.activeCounter
                        viewModel.incrementCounter(player.id, currentType, delta)
                    },
                    modifier = Modifier
                        .height(72.dp)
                        .fillMaxWidth(),
                    alignment = Alignment.Center
                )
            }
        }
    }
}

// -------------------- HELPER COMPOSABLES --------------------

@Composable
private fun CounterTapStrip(
    plus: Boolean,
    onIncrement: (Int) -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center
) {
    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { onIncrement(if (plus) +1 else -1) }
        },
        contentAlignment = alignment
    ) {
        Text(
            if (plus) "+" else "–",
            fontSize = 56.sp,
            color = Color.Black.copy(alpha = 0.25f)
        )
    }
}

@Composable
private fun CounterSwipeArea(
    player: PlayerState,
    viewModel: LifeCounterViewModel,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(player.id, rotation) {
                detectCounterSwipe(player.id, viewModel, rotation)
            },
        contentAlignment = Alignment.Center
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val currentPlayer = uiState.players.firstOrNull { it.id == player.id }
        val value = currentPlayer?.counters?.get(currentPlayer.activeCounter) ?: 0
        val counterType = currentPlayer?.activeCounter ?: CounterType.LIFE

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .graphicsLayer { rotationZ = rotation }
                .padding(8.dp)
        ) {
            Text(text = "$value", fontSize = 42.sp)
            Spacer(Modifier.height(6.dp))
            Text(text = counterType.label, fontSize = 14.sp)
        }
    }
}


// -------------------- UTILITY --------------------

private fun normalizedAngle(rotationAngle: Float): Float {
    val a = ((rotationAngle % 360) + 360) % 360
    return when {
        abs(a - 0f) < 1e-3 -> 0f
        abs(a - 90f) < 1e-3 -> 90f
        abs(a - 180f) < 1e-3 -> 180f
        else -> 270f
    }
}



// #################### HELPER FUNCTIONS ####################

// --- Swipe detection ---
suspend fun PointerInputScope.detectCounterSwipe(
    playerId: Int,
    viewModel: LifeCounterViewModel,
    rotation: Float
) {
    awaitPointerEventScope {
        while (true) {
            awaitFirstDown()
            var acc = 0f
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull() ?: continue
                if (!change.pressed) break
                val dx = change.positionChange().x
                val dy = change.positionChange().y
                val adjusted = when (rotation) {
                    0f -> dy
                    180f -> -dy
                    90f -> -dx
                    270f -> dx
                    else -> dy
                }
                acc += adjusted
                change.consume()
            }

            // **Query the latest active counter from ViewModel**
            val currentType = viewModel.uiState.value.players
                .firstOrNull { it.id == playerId }?.activeCounter
                ?: CounterType.LIFE

            if (acc < -60f) viewModel.switchCounter(playerId, nextCounter(currentType))
            else if (acc > 60f) viewModel.switchCounter(playerId, prevCounter(currentType))
        }
    }
}



// --- Counter cycling helpers ---
fun nextCounter(t: CounterType): CounterType {
    val arr = CounterType.values()
    return arr[(t.ordinal + 1) % arr.size]
}

fun prevCounter(t: CounterType): CounterType {
    val arr = CounterType.values()
    return arr[(t.ordinal - 1 + arr.size) % arr.size]
}
