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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pumpkinprod.ludopax.pod.model.Player
import com.pumpkinprod.ludopax.pod.model.SlotRef

@Composable
fun PodScreen(viewModel: PodViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Root container unchanged
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!uiState.isBracketStarted) {
            PlayerSetup(viewModel)
        } else {
            var swapMode by remember { mutableStateOf(false) }
            var firstPick by remember { mutableStateOf<SlotRef?>(null) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                BracketView(
                    matches = uiState.matches,
                    swapMode = swapMode,
                    selectedSlot = firstPick,
                    onPickSlot = { slot ->
                        if (!swapMode) return@BracketView
                        if (firstPick == null) {
                            firstPick = slot
                        } else {
                            val ok = viewModel.swapPlayers(firstPick!!, slot)
                            firstPick = null
                            // Optionally: show a snackbar if !ok
                        }
                    },
                    onSelectWinner = { matchId, player ->
                        if (!swapMode) viewModel.selectWinner(matchId, player)
                    }
                )

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.undo() }, enabled = !swapMode) { Text("Undo") }
                    Button(onClick = {
                        firstPick = null
                        swapMode = false
                        viewModel.reset()
                    }) { Text("Reset") }
                    Button(onClick = {
                        // Toggle swap mode; clear any pending selection
                        if (swapMode) firstPick = null
                        swapMode = !swapMode
                    }) {
                        Text(if (swapMode) "Done Swapping" else "Swap Players")
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}




@Composable
fun PlayerSetup(viewModel: PodViewModel) {
    var playerCount by remember { mutableStateOf(4) }
    val playerNames = remember { mutableStateListOf("", "", "", "") }
    val focusManager = LocalFocusManager.current

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
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = if (i == playerNames.lastIndex) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth()
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
    swapMode: Boolean,
    selectedSlot: SlotRef?,
    onPickSlot: (SlotRef) -> Unit,
    onSelectWinner: (Int, Player) -> Unit
) {
    val grouped = matches.groupBy { it.round }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        grouped.toSortedMap().forEach { (round, roundMatches) ->
            Column {
                Text("Round $round", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                roundMatches.forEach { match ->
                    MatchItem(
                        match = match,
                        swapMode = swapMode,
                        selectedSlot = selectedSlot,
                        onPickSlot = onPickSlot,
                        onSelectWinner = onSelectWinner
                    )
                }
            }
        }
    }
}

@Composable
fun MatchItem(
    match: com.pumpkinprod.ludopax.pod.model.Match,
    swapMode: Boolean,
    selectedSlot: SlotRef?,
    onPickSlot: (SlotRef) -> Unit,
    onSelectWinner: (Int, Player) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        PlayerItem(
            player = match.player1,
            winner = match.winner,
            selected = selectedSlot?.let { it.matchId == match.id && it.isPlayer1 } == true,
            enabled = match.player1 != null && match.winner == null,
            onClick = {
                if (swapMode) onPickSlot(SlotRef(match.id, true))
                else match.player1?.let { if (it.id != -1) onSelectWinner(match.id, it) }
            }
        )
        PlayerItem(
            player = match.player2,
            winner = match.winner,
            selected = selectedSlot?.let { it.matchId == match.id && !it.isPlayer1 } == true,
            enabled = match.player2 != null && match.winner == null,
            onClick = {
                if (swapMode) onPickSlot(SlotRef(match.id, false))
                else match.player2?.let { if (it.id != -1) onSelectWinner(match.id, it) }
            }
        )
    }
}

@Composable
fun PlayerItem(
    player: Player?,
    winner: Player?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val isWinner = player != null && winner?.id == player.id
    val bg = when {
        selected -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
        isWinner -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(enabled = enabled && player?.id != -1, onClick = onClick),
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

