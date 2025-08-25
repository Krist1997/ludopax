package com.pumpkinprod.ludopax.lifecounter.model

import androidx.compose.ui.graphics.Color

data class PlayerState(
    val id: Int,
    val color: Color,
    val activeCounter: CounterType = CounterType.LIFE,
    val counters: Map<CounterType, Int> = mapOf(
        CounterType.LIFE to 20,
        CounterType.POISON to 0,
        CounterType.ENERGY to 0,
        CounterType.EXPERIENCE to 0
    )
)
