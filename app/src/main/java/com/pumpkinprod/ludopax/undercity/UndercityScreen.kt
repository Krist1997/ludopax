package com.pumpkinprod.ludopax.undercity

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pumpkinprod.ludopax.R

@Composable
fun UndercityScreen(viewModel: UndercityViewModel) {
    val pawnPositions by viewModel.pawnPositions.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.undercity),
                contentDescription = "Undercity Dungeon",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            pawnPositions.forEachIndexed { index, pos ->
                DraggablePawn(
                    initialPosition = pos,
                    pawnRes = if (index == 0) R.drawable.red_pawn else R.drawable.blue_pawn,
                    onDrag = { dragAmount -> viewModel.updatePawn(index, pos + dragAmount) },
                    onDragEnd = { endPos -> viewModel.updatePawn(index, endPos) }
                )
            }
        }
    }
}


@Composable
fun DraggablePawn(
    initialPosition: Offset,
    pawnRes: Int,
    // Callback for when the pawn is dragged
    onDrag: (dragAmount: Offset) -> Unit,
    // Callback for when the drag gesture ends
    onDragEnd: (endPosition: Offset) -> Unit
) {
    var currentPosition by remember { mutableStateOf(initialPosition) }

    Image(
        painter = painterResource(id = pawnRes),
        contentDescription = "Pawn",
        modifier = Modifier
            .offset { IntOffset(currentPosition.x.toInt(), currentPosition.y.toInt()) }
            .size(48.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Update the local state
                        currentPosition += dragAmount
                        // Pass the drag amount to the ViewModel
                        onDrag(dragAmount)
                    },
                    onDragEnd = {
                        // Pass the final position to the ViewModel
                        onDragEnd(currentPosition)
                    }
                )
            }
    )
}
