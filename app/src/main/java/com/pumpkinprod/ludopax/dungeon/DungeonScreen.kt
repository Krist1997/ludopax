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
import com.pumpkinprod.ludopax.lifecounter.ui.screen.ConfirmResetDialog
import kotlin.math.roundToInt

private object Ui {
    val ScreenPadding = 16.dp
    val PawnSize = 48.dp
    val BottomMargin = 12.dp
    val SideMargin = 12.dp
}

/** Map each pawn index (0..5) to its drawable name. */
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
fun DungeonScreen(viewModel: DungeonViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // state for dialog
    var showResetDialog by remember { mutableStateOf(false) }

    // state for board size (needed to recompute default positions)
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val pawnSizePx = with(density) { Ui.PawnSize.toPx() }
    val sideMarginPx = with(density) { Ui.SideMargin.toPx() }
    val bottomMarginPx = with(density) { Ui.BottomMargin.toPx() }

    Column(modifier = Modifier.fillMaxSize()) {

        // --- Top controls stacked: Dropdown then Reset ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Ui.ScreenPadding)
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


            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Pawns")
            }
        }

        // Confirmation dialog for reset
        if (showResetDialog) {
            ConfirmResetDialog(
                title = "Reset Dungeon",
                message = "Do you want to reset all pawns to their starting positions?",
                onConfirm = {
                    showResetDialog = false
                    if (containerSize.width > 0 && containerSize.height > 0) {
                        val defaults = defaultBottomPositions(
                            count = MAX_PAWNS,
                            widthPx = containerSize.width.toFloat(),
                            heightPx = containerSize.height.toFloat(),
                            pawnSizePx = pawnSizePx,
                            sideMarginPx = sideMarginPx,
                            bottomMarginPx = bottomMarginPx
                        )
                        viewModel.setCurrentDungeonPositions(defaults)
                    }
                },
                onDismiss = { showResetDialog = false }
            )
        }

        // --- Board area (image + pawns) ---
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
                .onGloballyPositioned { containerSize = it.size }
        ) {
            Image(
                painter = painterResource(id = uiState.selectedDungeon.imageRes),
                contentDescription = uiState.selectedDungeon.label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            uiState.pawnPositions.forEachIndexed { index, pos ->
                val resId = PawnDrawables.getOrNull(index) ?: return@forEachIndexed
                DraggablePawn(
                    initialPosition = pos,
                    pawnRes = resId,
                    onDrag = { delta -> viewModel.offsetPawn(index, delta) },
                    onDragEnd = { /* no-op */ }
                )
            }
        }
    }
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
    val available = (widthPx - 2 * sideMarginPx).coerceAtLeast(pawnSizePx)
    val step = available / (count + 1)
    val y = heightPx - bottomMarginPx - pawnSizePx
    return List(count) { i ->
        val centerX = sideMarginPx + step * (i + 1)
        val x = centerX - (pawnSizePx / 2f)
        Offset(x, y)
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
                        change.consume(); onDrag(drag)
                    },
                    onDragEnd = { onDragEnd(initialPosition) }
                )
            }
    )
}
