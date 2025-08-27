package com.pumpkinprod.ludopax.randomizer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pumpkinprod.ludopax.R

private object Ui {
    val ScreenPadding = 16.dp
    val SectionSpacing = 12.dp
    val RowSpacing = 12.dp
    val FieldWidth = 280.dp
    val LogoWidth = 700.dp
}

@Composable
fun RandomizerScreen(viewModel: RandomizerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    when (state.mode) {
        RandomizerMode.HOME -> RandomizerHome(
            tiles = listOf(
                RandomizerTile("D4",    RandomizerMode.D4,    R.drawable.randomizer_d4),
                RandomizerTile("D6",    RandomizerMode.D6,    R.drawable.randomizer_d6),
                RandomizerTile("D8",    RandomizerMode.D8,    R.drawable.randomizer_d8),
                RandomizerTile("D12",   RandomizerMode.D12,   R.drawable.randomizer_d12),
                RandomizerTile("D20",   RandomizerMode.D20,   R.drawable.randomizer_d20),
                RandomizerTile("2×D6",  RandomizerMode.D6PLUS6,  R.drawable.randomizer_d6plusd6),
                RandomizerTile("Packs", RandomizerMode.PACKS, R.drawable.randomizer_pack)
            ),
            onSelect = { viewModel.setMode(it) }
        )

        RandomizerMode.PACKS -> PackRandomizerScreen(
            state = state,
            onBack = { viewModel.setMode(RandomizerMode.HOME) },
            onTotalChange = viewModel::updateTotalPacks,
            onPickChange = viewModel::updatePacksToPick,
            onPick = viewModel::pickPacksReducingMax,
            onReset = viewModel::resetPacks,
            onPreset = viewModel::applyPreset
        )

        RandomizerMode.D4  -> DiceScreen(4,  state.lastRoll, state.rollHistory,
            onBack = { viewModel.setMode(RandomizerMode.HOME) },
            onRoll = { viewModel.rollDie(4) },  onClear = viewModel::clearDice)

        RandomizerMode.D6  -> DiceScreen(6,  state.lastRoll, state.rollHistory,
            onBack = { viewModel.setMode(RandomizerMode.HOME) },
            onRoll = { viewModel.rollDie(6) },  onClear = viewModel::clearDice)

        RandomizerMode.D8  -> DiceScreen(8,  state.lastRoll, state.rollHistory,
            onBack = { viewModel.setMode(RandomizerMode.HOME) },
            onRoll = { viewModel.rollDie(8) },  onClear = viewModel::clearDice)

        RandomizerMode.D12 -> DiceScreen(12, state.lastRoll, state.rollHistory,
            onBack = { viewModel.setMode(RandomizerMode.HOME) },
            onRoll = { viewModel.rollDie(12) }, onClear = viewModel::clearDice)

        RandomizerMode.D20 -> DiceScreen(20, state.lastRoll, state.rollHistory,
            onBack = { viewModel.setMode(RandomizerMode.HOME) },
            onRoll = { viewModel.rollDie(20) }, onClear = viewModel::clearDice)

        RandomizerMode.D6PLUS6 -> DicePairScreen(
            sides = 6,
            last = state.lastRoll2d6,
            history = state.history2d6,
            onBack = { viewModel.setMode(RandomizerMode.HOME) },
            onRoll = { viewModel.roll2d6() },
            onClear = { viewModel.clear2d6() }
        )
    }
}

/* ---------------------- Sub-screens ---------------------- */

@Composable
private fun PackRandomizerScreen(
    state: RandomizerUiState,
    onBack: () -> Unit,
    onTotalChange: (String) -> Unit,
    onPickChange: (String) -> Unit,
    onPick: () -> Unit,
    onReset: () -> Unit,
    onPreset: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Ui.ScreenPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Ui.SectionSpacing)
    ) {
        // Back + Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBack) { Text("← Back") }
            Text("Pack Randomizer")
            Spacer(Modifier.width(1.dp))
        }

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Randomizer",
            modifier = Modifier.width(Ui.LogoWidth),
            contentScale = ContentScale.Fit
        )

        Row(horizontalArrangement = Arrangement.spacedBy(Ui.RowSpacing)) {
            TextField(
                value = state.totalPacks,
                onValueChange = onTotalChange,
                label = { Text("Total Packs") },
                modifier = Modifier.width(Ui.FieldWidth),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TextField(
                value = state.packsToPick,
                onValueChange = onPickChange,
                label = { Text("Packs to Pick") },
                modifier = Modifier.width(Ui.FieldWidth),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Ui.RowSpacing)) {
            Button(onClick = onPick) { Text("Pick Packs") }
            if (state.result.isNotEmpty()) {
                Button(onClick = onReset) { Text("Reset") }
            }
        }

        when {
            state.errorMessage != null -> Text(state.errorMessage!!, color = androidx.compose.ui.graphics.Color.Red)
            state.result.isNotEmpty() -> {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("Result:")
                    state.result.forEach { number -> Text("• Pack $number") }
                }
            }
        }

        Divider()

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Presets:")
            Row(horizontalArrangement = Arrangement.spacedBy(Ui.RowSpacing)) {
                Button(onClick = { onPreset(24, 8) }) { Text("Draft 2P x4") }
                Button(onClick = { onPreset(24, 6) }) { Text("Draft 2P x3") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Ui.RowSpacing)) {
                Button(onClick = { onPreset(24, 9) }) { Text("Draft 3P x3") }
                Button(onClick = { onPreset(24, 12) }) { Text("Draft 4P x3") }
            }
        }
    }
}

@Composable
private fun DiceScreen(
    sides: Int,
    last: Int?,
    history: List<Int>,
    onBack: () -> Unit,
    onRoll: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Ui.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Ui.SectionSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBack) { Text("← Back") }
            Text("d$sides")
            Spacer(Modifier.width(1.dp))
        }

        Text(text = last?.toString() ?: "—", fontSize = 64.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(Ui.RowSpacing)) {
            Button(onClick = onRoll) { Text("Roll") }
            if (history.isNotEmpty()) {
                Button(onClick = onClear) { Text("Clear") }
            }
        }

        if (history.isNotEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("History (last ${history.size}):")
                Text(history.joinToString(", "))
            }
        }
    }
}

@Composable
private fun DicePairScreen(
    sides: Int,
    last: Pair<Int, Int>?,
    history: List<Pair<Int, Int>>,
    onBack: () -> Unit,
    onRoll: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Ui.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Ui.SectionSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBack) { Text("← Back") }
            Text("2 × d$sides")
            Spacer(Modifier.width(1.dp))
        }

        val a = last?.first
        val b = last?.second
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(text = a?.toString() ?: "—", fontSize = 56.sp)
                Text(text = b?.toString() ?: "—", fontSize = 56.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(text = if (a != null && b != null) "Total: ${a + b}" else "Total: —", fontSize = 20.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Ui.RowSpacing)) {
            Button(onClick = onRoll) { Text("Roll") }
            if (history.isNotEmpty()) {
                Button(onClick = onClear) { Text("Clear") }
            }
        }

        if (history.isNotEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("History (last ${history.size}):")
                Text(history.joinToString(", ") { "${it.first}+${it.second}=${it.first + it.second}" })
            }
        }
    }
}
