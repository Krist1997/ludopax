package com.pumpkinprod.ludopax.lifecounter.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pumpkinprod.ludopax.lifecounter.model.CounterType
import com.pumpkinprod.ludopax.lifecounter.model.PlayerState

@Composable
fun PlayerGrid(
    players: List<PlayerState>,
    onIncrement: (player: PlayerState, type: CounterType, delta: Int) -> Unit,
    onSwitchCounter: (player: PlayerState, type: CounterType) -> Unit
) {
    when (players.size) {

        2 -> {
            Column(modifier = Modifier.fillMaxSize()) {
                PlayerCard(
                    player = players[0],
                    rotationAngle = 180f, // top
                    onIncrement = { t, d -> onIncrement(players[0], t, d) },
                    onSwitchCounter = { t -> onSwitchCounter(players[0], t) },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                PlayerCard(
                    player = players[1],
                    rotationAngle = 0f, // bottom
                    onIncrement = { t, d -> onIncrement(players[1], t, d) },
                    onSwitchCounter = { t -> onSwitchCounter(players[1], t) },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
        }

        3 -> {
            Column(modifier = Modifier.fillMaxSize()) {
                PlayerCard(
                    player = players[0],
                    rotationAngle = 180f, // bottom center
                    onIncrement = { t, d -> onIncrement(players[0], t, d) },
                    onSwitchCounter = { t -> onSwitchCounter(players[0], t) },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PlayerCard(
                        player = players[1],
                        rotationAngle = 90f, // top left
                        onIncrement = { t, d -> onIncrement(players[1], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[1], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    PlayerCard(
                        player = players[2],
                        rotationAngle = 270f, // top right
                        onIncrement = { t, d -> onIncrement(players[2], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[2], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }

        4 -> {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PlayerCard(
                        player = players[0],
                        rotationAngle = 90f, // bottom left
                        onIncrement = { t, d -> onIncrement(players[0], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[0], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    PlayerCard(
                        player = players[1],
                        rotationAngle = 270f, // top left
                        onIncrement = { t, d -> onIncrement(players[1], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[1], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PlayerCard(
                        player = players[2],
                        rotationAngle = 90f, // top right
                        onIncrement = { t, d -> onIncrement(players[2], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[2], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    PlayerCard(
                        player = players[3],
                        rotationAngle = 270f, // bottom right
                        onIncrement = { t, d -> onIncrement(players[3], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[3], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }

        5 -> {
            Column(modifier = Modifier.fillMaxSize()) {
                PlayerCard(
                    player = players[0],
                    rotationAngle = 180f, // bottom center
                    onIncrement = { t, d -> onIncrement(players[0], t, d) },
                    onSwitchCounter = { t -> onSwitchCounter(players[0], t) },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PlayerCard(
                        player = players[1],
                        rotationAngle = 90f, // center left
                        onIncrement = { t, d -> onIncrement(players[1], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[1], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    PlayerCard(
                        player = players[2],
                        rotationAngle = 270f, // top left
                        onIncrement = { t, d -> onIncrement(players[2], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[2], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PlayerCard(
                        player = players[3],
                        rotationAngle = 90f, // top right
                        onIncrement = { t, d -> onIncrement(players[3], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[3], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    PlayerCard(
                        player = players[4],
                        rotationAngle = 270f, // center right
                        onIncrement = { t, d -> onIncrement(players[4], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[4], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }

        6 -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Row 1
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PlayerCard(
                        player = players[0],
                        rotationAngle = 90f, // bottom left
                        onIncrement = { t, d -> onIncrement(players[0], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[0], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    PlayerCard(
                        player = players[1],
                        rotationAngle = 270f, // bottom right
                        onIncrement = { t, d -> onIncrement(players[1], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[1], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                // Row 2
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PlayerCard(
                        player = players[2],
                        rotationAngle = 90f, // left side
                        onIncrement = { t, d -> onIncrement(players[2], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[2], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    PlayerCard(
                        player = players[3],
                        rotationAngle = 270f, // right side
                        onIncrement = { t, d -> onIncrement(players[3], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[3], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                // Row 3
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    PlayerCard(
                        player = players[4],
                        rotationAngle = 90f, // top left
                        onIncrement = { t, d -> onIncrement(players[4], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[4], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    PlayerCard(
                        player = players[5],
                        rotationAngle = 270f, // top right
                        onIncrement = { t, d -> onIncrement(players[5], t, d) },
                        onSwitchCounter = { t -> onSwitchCounter(players[5], t) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }


        else -> {
            Column(modifier = Modifier.fillMaxSize()) {
                players.forEach { player ->
                    PlayerCard(
                        player = player,
                        rotationAngle = 0f,
                        onIncrement = { type, delta -> onIncrement(player, type, delta) },
                        onSwitchCounter = { type -> onSwitchCounter(player, type) },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
            }
        }
    }
}
