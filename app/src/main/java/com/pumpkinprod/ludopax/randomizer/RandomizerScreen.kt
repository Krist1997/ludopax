package com.pumpkinprod.ludopax.randomizer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pumpkinprod.ludopax.R

@Composable
fun RandomizerScreen(viewModel: RandomizerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 🖼 Top image
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Magic Pack",
            modifier = Modifier.width(700.dp),
            contentScale = ContentScale.Fit
        )

        // 🎯 Total Packs input
        TextField(
            value = state.totalPacks,
            onValueChange = viewModel::updateTotalPacks,
            label = { Text("Total Packs") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // 🎯 Packs to Pick input
        TextField(
            value = state.packsToPick,
            onValueChange = viewModel::updatePacksToPick,
            label = { Text("Packs to Pick") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // 🎲 Pick button
        Button(onClick = { viewModel.pickPacksReducingMax() }) {
            Text("Pick Packs")
        }

        // 🧾 Result or Error
        if (state.errorMessage != null) {
            Text(state.errorMessage!!, color = androidx.compose.ui.graphics.Color.Red)
        } else {
            if (state.result.isNotEmpty()) {
                Text("Result:")
                Column(
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.padding(top = 7.dp)
                ) {
                    state.result.forEach { number ->
                        Text("• Pack $number")
                    }
                }
            }
        }

        // 🔁 Reset button if needed
        if (state.result.isNotEmpty()) {
            Button(onClick = { viewModel.reset() }) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Presets:")

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { viewModel.applyPreset(24, 8) }) {
                Text("Draft 2P x4")
            }
            Button(onClick = { viewModel.applyPreset(24, 6) }) {
                Text("Draft 2P x3")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { viewModel.applyPreset(24, 9) }) {
                Text("Draft 3P x3")
            }
            Button(onClick = { viewModel.applyPreset(24, 12) }) {
                Text("Draft 4P x3")
            }
        }
    }
}
