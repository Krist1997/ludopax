package com.pumpkinprod.ludopax.lifecounter.ui.component.grid

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pumpkinprod.ludopax.lifecounter.domain.PlayerState
import com.pumpkinprod.ludopax.lifecounter.ui.component.card.PlayerCard
import com.pumpkinprod.ludopax.lifecounter.viewmodel.LifeCounterViewModel

@Composable
fun PlayerGrid(
    players: List<PlayerState>,
    viewModel: LifeCounterViewModel
) {
    when (players.size) {
        2 -> {
            // simple split column
            PlayerColumn(players, viewModel)
        }

        3 -> {
            Column(Modifier.fillMaxSize()) {
                PlayerCard(
                    player = players[0],
                    viewModel = viewModel,
                    rotationAngle = playerRotation(players.size, 0),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                PlayerRow(
                    players = players.subList(1, 3),
                    viewModel = viewModel,
                    allCount = players.size,
                    startIndex = 1
                )
            }
        }

        4 -> {
            Column(Modifier.fillMaxSize()) {
                PlayerRow(
                    players = players.subList(0, 2),
                    viewModel = viewModel,
                    allCount = players.size,
                    startIndex = 0
                )
                PlayerRow(
                    players = players.subList(2, 4),
                    viewModel = viewModel,
                    allCount = players.size,
                    startIndex = 2
                )
            }
        }

        5 -> {
            Column(Modifier.fillMaxSize()) {
                PlayerCard(
                    player = players[0],
                    viewModel = viewModel,
                    rotationAngle = playerRotation(players.size, 0),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                PlayerRow(
                    players = players.subList(1, 3),
                    viewModel = viewModel,
                    allCount = players.size,
                    startIndex = 1
                )
                PlayerRow(
                    players = players.subList(3, 5),
                    viewModel = viewModel,
                    allCount = players.size,
                    startIndex = 3
                )
            }
        }

        6 -> {
            Column(Modifier.fillMaxSize()) {
                var start = 0
                while (start < 6) {
                    PlayerRow(
                        players = players.subList(start, start + 2),
                        viewModel = viewModel,
                        allCount = players.size,
                        startIndex = start
                    )
                    start += 2
                }
            }
        }

        else -> {
            // fallback: stack everyone vertically
            PlayerColumn(players, viewModel)
        }
    }
}

@Composable
private fun ColumnScope.PlayerRow(
    players: List<PlayerState>,
    viewModel: LifeCounterViewModel,
    allCount: Int,
    startIndex: Int
) {
    Row(
        modifier = Modifier
            .weight(1f)            // row takes equal vertical space
            .fillMaxWidth()
    ) {
        players.forEachIndexed { i, player ->
            PlayerCard(
                player = player,
                viewModel = viewModel,
                rotationAngle = playerRotation(allCount, startIndex + i),
                modifier = Modifier
                    .weight(1f)      // split row horizontally
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun PlayerColumn(
    players: List<PlayerState>,
    viewModel: LifeCounterViewModel
) {
    Column(Modifier.fillMaxSize()) {
        players.forEachIndexed { i, player ->
            PlayerCard(
                player = player,
                viewModel = viewModel,
                rotationAngle = playerRotation(players.size, i),
                modifier = Modifier
                    .weight(1f)      // split column vertically
                    .fillMaxWidth()
            )
        }
    }
}

private fun playerRotation(playerCount: Int, index: Int): Float {
    return when (playerCount) {
        2 -> if (index == 0) 180f else 0f
        3 -> listOf(180f, 90f, 270f)[index]
        4 -> listOf(90f, 270f, 90f, 270f)[index]
        5 -> listOf(180f, 90f, 270f, 90f, 270f)[index]
        6 -> listOf(90f, 270f, 90f, 270f, 90f, 270f)[index]
        else -> 0f
    }
}
