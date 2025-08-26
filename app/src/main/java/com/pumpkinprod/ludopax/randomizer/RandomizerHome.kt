package com.pumpkinprod.ludopax.randomizer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

data class RandomizerTile(
    val label: String,              // kept for a11y (contentDescription)
    val mode: RandomizerMode,
    val iconRes: Int                // now required (no more null)
)

@Composable
fun RandomizerHome(
    tiles: List<RandomizerTile>,
    onSelect: (RandomizerMode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tiles) { tile ->
            ElevatedCard(
                onClick = { onSelect(tile.mode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(tile.iconRes),
                        contentDescription = tile.label, // a11y
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth() // let your PNG decide padding
                    )
                }
            }
        }
    }
}
