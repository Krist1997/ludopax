package com.pumpkinprod.ludopax.lifecounter.ui.component.grid

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pumpkinprod.ludopax.lifecounter.domain.PlayerState
import com.pumpkinprod.ludopax.lifecounter.ui.component.card.PlayerCard
import com.pumpkinprod.ludopax.lifecounter.viewmodel.LifeCounterViewModel

@Composable
fun PlayerGrid(
    players: List<PlayerState>,
    viewModel: LifeCounterViewModel
) {
    when (players.size) {
        2 -> PlayerColumn(players, viewModel)
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
                PlayerRow(players.subList(0, 2), viewModel, players.size, 0)
                PlayerRow(players.subList(2, 4), viewModel, players.size, 2)
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
                PlayerRow(players.subList(1, 3), viewModel, players.size, 1)
                PlayerRow(players.subList(3, 5), viewModel, players.size, 3)
            }
        }
        6 -> {
            Column(Modifier.fillMaxSize()) {
                var start = 0
                while (start < 6) {
                    PlayerRow(players.subList(start, start + 2), viewModel, players.size, start)
                    start += 2
                }
            }
        }
        else -> PlayerColumn(players, viewModel)
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
            .weight(1f)
            .fillMaxWidth()
    ) {
        players.forEachIndexed { i, player ->
            PlayerCard(
                player = player,
                viewModel = viewModel,
                rotationAngle = playerRotation(allCount, startIndex + i),
                modifier = Modifier
                    .weight(1f)
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
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

private fun playerRotation(playerCount: Int, index: Int): Float {
    val patterns = when (playerCount) {
        2 -> floatArrayOf(180f, 0f)
        3 -> floatArrayOf(180f, 90f, 270f)
        4 -> floatArrayOf(90f, 270f, 90f, 270f)
        5 -> floatArrayOf(180f, 90f, 270f, 90f, 270f)
        6 -> floatArrayOf(90f, 270f, 90f, 270f, 90f, 270f)
        else -> floatArrayOf(0f)
    }
    return patterns[index]
}

/* -------------------- PREVIEW (optional) -------------------- */

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PlayerGridPreview() {
    val vm = LifeCounterViewModel().apply { setPlayers(4) }
    PlayerGrid(players = vm.uiState.value.players, viewModel = vm)
}
