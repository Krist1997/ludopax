package com.pumpkinprod.ludopax.pod

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pumpkinprod.ludopax.pod.model.Player

@Composable
fun PodScreen(viewModel: PodViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!uiState.isBracketStarted) {
            PlayerSetup(viewModel)
        } else {
            BracketView(
                matches = uiState.matches,
                onSelectWinner = { matchId, player -> viewModel.selectWinner(matchId, player) }
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.undo() }) { Text("Undo") }
                Button(onClick = { viewModel.reset() }) { Text("Reset") }
            }
        }
    }
}


@Composable
fun PlayerSetup(viewModel: PodViewModel) {
    var playerCount by remember { mutableStateOf(4) }
    val playerNames = remember { mutableStateListOf("", "", "", "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        // 👇 IME padding lives *inside* the scrollable content to avoid the blank bar
        contentPadding = WindowInsets.ime.asPaddingValues(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top spacing equal to your original 16.dp screen padding
        item { Spacer(Modifier.height(16.dp)) }

        item {
            Text("Select number of players:")
            val options = listOf(4, 6, 8, 10, 12)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { count ->
                    FilterChip(
                        selected = playerCount == count,
                        onClick = {
                            playerCount = count
                            while (playerNames.size < count) playerNames.add("")
                            while (playerNames.size > count) {
                                playerNames.removeAt(playerNames.lastIndex)
                            }
                        },
                        label = { Text("$count") }
                    )
                }
            }
        }

        items(playerNames.size) { i ->
            OutlinedTextField(
                value = playerNames[i],
                onValueChange = { playerNames[i] = it },
                label = { Text("Player ${i + 1}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // match screen padding visually
            )
        }

        item {
            Button(
                onClick = { viewModel.setPlayers(playerNames); viewModel.startBracket() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // match screen padding visually
            ) {
                Text("Start Bracket")
            }
        }

        // Bottom spacer so last field/button isn’t glued to IME edge
        item { Spacer(Modifier.height(16.dp)) }
    }
}


@Composable
fun BracketView(
    matches: List<com.pumpkinprod.ludopax.pod.model.Match>,
    onSelectWinner: (Int, Player) -> Unit
) {
    val grouped = matches.groupBy { it.round }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        grouped.toSortedMap().forEach { (round, roundMatches) ->
            Column {
                Text("Round $round", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                roundMatches.forEach { match ->
                    MatchItem(match = match, onSelectWinner = onSelectWinner)
                }
            }
        }
    }
}

@Composable
fun MatchItem(
    match: com.pumpkinprod.ludopax.pod.model.Match,
    onSelectWinner: (Int, Player) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        PlayerItem(match.player1, match.winner, onClick = {
            if (match.player1 != null && match.player1.id != -1) {
                onSelectWinner(match.id, match.player1)
            }
        })
        PlayerItem(match.player2, match.winner, onClick = {
            if (match.player2 != null && match.player2.id != -1) {
                onSelectWinner(match.id, match.player2)
            }
        })
    }
}

@Composable
fun PlayerItem(player: Player?, winner: Player?, onClick: () -> Unit) {
    val isWinner = player != null && winner?.id == player.id
    val bg = if (isWinner) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(enabled = player != null && player.id != -1, onClick = onClick),
        color = bg,
        tonalElevation = if (isWinner) 2.dp else 0.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = player?.name ?: "—",
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
        )
    }
}
