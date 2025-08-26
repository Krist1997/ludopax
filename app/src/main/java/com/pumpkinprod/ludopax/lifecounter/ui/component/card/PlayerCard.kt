package com.pumpkinprod.ludopax.lifecounter.ui.component.card

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pumpkinprod.ludopax.lifecounter.domain.CounterType
import com.pumpkinprod.ludopax.lifecounter.domain.PlayerState
import com.pumpkinprod.ludopax.lifecounter.viewmodel.LifeCounterViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

private object UiDefaults {
    const val SWIPE_THRESHOLD = 60f
    const val SYMBOL_ALPHA = 0.25f

    // Flash animation (strip-only overlay)
    const val FLASH_MAX_ALPHA = 0.55f
    const val FLASH_DURATION_MS = 220

    // Separate sizes for both directions:
    // - Left/Right strips use width
    // - Top/Bottom strips use height
    val STRIP_WIDTH_DP = 120.dp   // ← tweak to make side strips wider
    val STRIP_HEIGHT_DP = 90.dp  // ← tweak to make top/bottom strips taller
}

private enum class StripSide { Left, Right, Top, Bottom }

@Composable
fun PlayerCard(
    player: PlayerState,
    viewModel: LifeCounterViewModel,
    modifier: Modifier = Modifier,
    rotationAngle: Float = 0f
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = player.color.copy(alpha = 0.28f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PlayerCardContent(
                player = player,
                viewModel = viewModel,
                rotationAngle = rotationAngle
            )
        }
    }
}

@Composable
fun PlayerCardContent(
    player: PlayerState,
    viewModel: LifeCounterViewModel,
    rotationAngle: Float = 0f
) {
    val angle = normalizedAngle(rotationAngle)
    val isHorizontal = angle == 0f || angle == 180f
    val leftIsPlus = angle == 180f
    val topIsPlus = angle == 270f

    // Always fetch the latest active counter from state when tapping +/-.
    val currentType: () -> CounterType = {
        viewModel.uiState.value.players
            .first { it.id == player.id }
            .activeCounter
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isHorizontal) {
            Row(Modifier.fillMaxSize()) {
                CounterTapStrip(
                    plus = leftIsPlus,
                    onIncrement = { delta ->
                        viewModel.incrementCounter(player.id, currentType(), delta)
                    },
                    modifier = Modifier
                        .width(UiDefaults.STRIP_WIDTH_DP)   // ← side strips use width
                        .fillMaxHeight(),
                    alignment = Alignment.CenterStart,
                    side = StripSide.Left
                )

                CounterSwipeArea(player, viewModel, angle, Modifier.weight(1f))

                CounterTapStrip(
                    plus = !leftIsPlus,
                    onIncrement = { delta ->
                        viewModel.incrementCounter(player.id, currentType(), delta)
                    },
                    modifier = Modifier
                        .width(UiDefaults.STRIP_WIDTH_DP)   // ← side strips use width
                        .fillMaxHeight(),
                    alignment = Alignment.CenterEnd,
                    side = StripSide.Right
                )
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                CounterTapStrip(
                    plus = topIsPlus,
                    onIncrement = { delta ->
                        viewModel.incrementCounter(player.id, currentType(), delta)
                    },
                    modifier = Modifier
                        .height(UiDefaults.STRIP_HEIGHT_DP) // ← top/bottom use height
                        .fillMaxWidth(),
                    alignment = Alignment.Center,
                    side = StripSide.Top
                )

                CounterSwipeArea(player, viewModel, angle, Modifier.weight(1f))

                CounterTapStrip(
                    plus = !topIsPlus,
                    onIncrement = { delta ->
                        viewModel.incrementCounter(player.id, currentType(), delta)
                    },
                    modifier = Modifier
                        .height(UiDefaults.STRIP_HEIGHT_DP) // ← top/bottom use height
                        .fillMaxWidth(),
                    alignment = Alignment.Center,
                    side = StripSide.Bottom
                )
            }
        }
    }
}

// -------------------- HELPER COMPOSABLES --------------------

@Composable
private fun CounterTapStrip(
    plus: Boolean,
    onIncrement: (Int) -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    side: StripSide
) {
    val scope = rememberCoroutineScope()
    val alpha = remember { Animatable(0f) }

    Box(
        modifier = modifier.pointerInput(plus, side) {
            detectTapGestures(
                onTap = {
                    // animate flash on this strip only
                    scope.launch {
                        alpha.snapTo(UiDefaults.FLASH_MAX_ALPHA)
                        alpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = UiDefaults.FLASH_DURATION_MS,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                    onIncrement(if (plus) +1 else -1)
                }
            )
        },
        contentAlignment = alignment
    ) {
        // Content (+ / – sign)
        Text(
            if (plus) "+" else "–",
            fontSize = 56.sp,
            color = Color.Black.copy(alpha = UiDefaults.SYMBOL_ALPHA)
        )

        // Directional flash overlay (inner edge near the HP number is brightest)
        StripFlashOverlay(alpha = alpha.value, side = side)
    }
}

@Composable
private fun StripFlashOverlay(alpha: Float, side: StripSide) {
    if (alpha <= 0f) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strong = Color.White.copy(alpha = alpha)          // inner edge (near HP)
        val mid    = Color.White.copy(alpha = alpha * 0.35f)  // middle
        val none   = Color.Transparent                         // outer edge

        when (side) {
            StripSide.Left -> {
                // Inner edge is RIGHT (toward center). Fade from right → left.
                drawRect(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to none,
                            0.6f to mid,
                            1f to strong
                        ),
                        startX = 0f,
                        endX = size.width
                    ),
                    size = size
                )
            }
            StripSide.Right -> {
                // Inner edge is LEFT (toward center). Fade from left → right.
                drawRect(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to strong,
                            0.4f to mid,
                            1f to none
                        ),
                        startX = 0f,
                        endX = size.width
                    ),
                    size = size
                )
            }
            StripSide.Top -> {
                // Inner edge is BOTTOM. Fade from bottom → top.
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to none,
                            0.6f to mid,
                            1f to strong
                        ),
                        startY = 0f,
                        endY = size.height
                    ),
                    size = size
                )
            }
            StripSide.Bottom -> {
                // Inner edge is TOP. Fade from top → bottom.
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to strong,
                            0.4f to mid,
                            1f to none
                        ),
                        startY = 0f,
                        endY = size.height
                    ),
                    size = size
                )
            }
        }
    }
}

@Composable
private fun CounterSwipeArea(
    player: PlayerState,
    viewModel: LifeCounterViewModel,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(player.id, rotation) {
                detectCounterSwipe(player.id, viewModel, rotation)
            },
        contentAlignment = Alignment.Center
    ) {
        val uiState by viewModel.uiState.collectAsState()

        // Compute both values using derivedStateOf directly from uiState
        val counterType by remember(uiState, player.id) {
            derivedStateOf {
                uiState.players.firstOrNull { it.id == player.id }?.activeCounter
                    ?: CounterType.LIFE
            }
        }

        val value by remember(uiState, player.id, counterType) {
            derivedStateOf {
                val cp = uiState.players.firstOrNull { it.id == player.id }
                cp?.counters?.get(counterType) ?: 0
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .graphicsLayer { rotationZ = rotation }
                .padding(8.dp)
        ) {
            Text(text = "$value", fontSize = 42.sp)
            Spacer(Modifier.height(6.dp))
            Text(text = counterType.label, fontSize = 14.sp)
        }
    }
}

// -------------------- UTILITY --------------------

/** Snap any float angle to one of 0/90/180/270, tolerant to small float errors. */
private fun normalizedAngle(rotationAngle: Float): Float {
    val a = ((rotationAngle % 360) + 360) % 360
    return when {
        abs(a - 0f) < 1e-3 -> 0f
        abs(a - 90f) < 1e-3 -> 90f
        abs(a - 180f) < 1e-3 -> 180f
        else -> 270f
    }
}

// #################### HELPER FUNCTIONS ####################

// --- Swipe detection ---
suspend fun PointerInputScope.detectCounterSwipe(
    playerId: Int,
    viewModel: LifeCounterViewModel,
    rotation: Float
) {
    awaitPointerEventScope {
        while (true) {
            awaitFirstDown()
            var acc = 0f
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull() ?: continue
                if (!change.pressed) break
                val dx = change.positionChange().x
                val dy = change.positionChange().y
                val adjusted = when (rotation) {
                    0f -> dy
                    180f -> -dy
                    90f -> -dx
                    270f -> dx
                    else -> dy
                }
                acc += adjusted
                change.consume()
            }

            val currentType = viewModel.uiState.value.players
                .firstOrNull { it.id == playerId }?.activeCounter
                ?: CounterType.LIFE

            if (acc < -UiDefaults.SWIPE_THRESHOLD) {
                viewModel.switchCounter(playerId, nextCounter(currentType))
            } else if (acc > UiDefaults.SWIPE_THRESHOLD) {
                viewModel.switchCounter(playerId, prevCounter(currentType))
            }
        }
    }
}

// --- Counter cycling helpers ---
fun nextCounter(t: CounterType): CounterType {
    val arr = CounterType.values()
    return arr[(t.ordinal + 1) % arr.size]
}

fun prevCounter(t: CounterType): CounterType {
    val arr = CounterType.values()
    return arr[(t.ordinal - 1 + arr.size) % arr.size]
}

/* -------------------- PREVIEW (optional) -------------------- */

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
@SuppressLint("ViewModelConstructorInComposition") // preview-only
private fun PlayerCardPreview() {
    val vm = remember {
        LifeCounterViewModel().apply { setPlayers(2) }
    }
    val ui by vm.uiState.collectAsState()
    val player = ui.players.firstOrNull()
        ?: PlayerState(id = 0, color = Color(0xFF64B5F6))

    PlayerCard(player = player, viewModel = vm, rotationAngle = 0f)
}
