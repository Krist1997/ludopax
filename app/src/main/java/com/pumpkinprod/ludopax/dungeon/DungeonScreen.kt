package com.pumpkinprod.ludopax.dungeon

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pumpkinprod.ludopax.R
import kotlin.math.roundToInt

private object Ui {
    val ScreenPadding = 16.dp
    val TopSpacing = 8.dp
    val PawnSize = 48.dp
    val BottomMargin = 12.dp
    val SideMargin = 12.dp
}

/** Map each pawn index (0..5) to its drawable name you provided. */
private val PawnDrawables = intArrayOf(
    R.drawable.pawns_red,
    R.drawable.pawns_blue,
    R.drawable.pawns_green,
    R.drawable.pawns_yellow,
    R.drawable.pawns_purple,
    R.drawable.pawns_cyan
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UndercityScreen(viewModel: DungeonViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // --- Dungeon selector (top) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Ui.ScreenPadding)
        ) {
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                OutlinedTextField(
                    value = uiState.selectedDungeon.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dungeon") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DungeonType.entries.forEach { dungeon ->
                        DropdownMenuItem(
                            text = { Text(dungeon.label) },
                            onClick = {
                                expanded = false
                                viewModel.selectDungeon(dungeon)
                            }
                        )
                    }
                }
            }
        }

        // --- Board area (image + pawns) ---
        var containerSize by remember { mutableStateOf(IntSize.Zero) }
        val density = LocalDensity.current
        val pawnSizePx = with(density) { Ui.PawnSize.toPx() }
        val sideMarginPx = with(density) { Ui.SideMargin.toPx() }
        val bottomMarginPx = with(density) { Ui.BottomMargin.toPx() }

        // When the container is known or dungeon changes, initialize default positions once
        LaunchedEffect(uiState.selectedDungeon, containerSize) {
            if (containerSize.width > 0 && containerSize.height > 0) {
                val defaults = defaultBottomPositions(
                    count = MAX_PAWNS,
                    widthPx = containerSize.width.toFloat(),
                    heightPx = containerSize.height.toFloat(),
                    pawnSizePx = pawnSizePx,
                    sideMarginPx = sideMarginPx,
                    bottomMarginPx = bottomMarginPx
                )
                viewModel.initializeCurrentDungeonPositionsIfNeeded(defaults)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(top = Ui.TopSpacing)
                .onGloballyPositioned { containerSize = it.size }
        ) {
            Image(
                painter = painterResource(id = uiState.selectedDungeon.imageRes),
                contentDescription = uiState.selectedDungeon.label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Draw up to 6 pawns
            uiState.pawnPositions.forEachIndexed { index, pos ->
                val resId = PawnDrawables.getOrNull(index) ?: return@forEachIndexed
                DraggablePawn(
                    initialPosition = pos,
                    pawnRes = resId,
                    onDrag = { dragAmount ->
                        // Apply deltas in the VM; avoids stale 'pos' capture
                        viewModel.offsetPawn(index, dragAmount)
                    },
                    // End event is optional now – positions already updated continuously.
                    onDragEnd = { /* no-op for now */ }
                )
            }
        }
    }
}

@Composable
private fun DraggablePawn(
    initialPosition: Offset,
    pawnRes: Int,
    onDrag: (dragAmount: Offset) -> Unit,
    onDragEnd: (endPosition: Offset) -> Unit
) {
    if (initialPosition == Offset.Unspecified) return

    Image(
        painter = painterResource(id = pawnRes),
        contentDescription = "Pawn",
        modifier = Modifier
            .offset { IntOffset(initialPosition.x.roundToInt(), initialPosition.y.roundToInt()) }
            .size(Ui.PawnSize)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, drag ->
                        change.consume()
                        onDrag(drag)
                    },
                    onDragEnd = { onDragEnd(initialPosition) }
                )
            }
    )
}


/**
 * Produces evenly-spaced starting positions along the bottom of the board area.
 * Pawns are centered on X with margins, and sit just above the bottom margin.
 */
private fun defaultBottomPositions(
    count: Int,
    widthPx: Float,
    heightPx: Float,
    pawnSizePx: Float,
    sideMarginPx: Float,
    bottomMarginPx: Float
): List<Offset> {
    if (count <= 0) return emptyList()

    // Available width between side margins
    val available = (widthPx - 2 * sideMarginPx)
        .coerceAtLeast(pawnSizePx) // avoid negative/too small

    // Even spacing: place centers at equal segments
    val step = available / (count + 1)
    val y = heightPx - bottomMarginPx - pawnSizePx // top-left y of the image (since we offset by top-left)

    return List(count) { i ->
        val centerX = sideMarginPx + step * (i + 1)
        // Convert centerX to top-left X for the Image (offset uses top-left)
        val x = centerX - (pawnSizePx / 2f)
        Offset(x, y)
    }
}
