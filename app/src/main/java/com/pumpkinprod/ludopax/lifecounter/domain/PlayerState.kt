package com.pumpkinprod.ludopax.lifecounter.domain

import androidx.compose.ui.graphics.Color

data class PlayerState(
    val id: Int,
    val color: Color,
    val activeCounter: CounterType = CounterType.LIFE,
    val counters: Map<CounterType, Int> = defaultCounters(startLife = 20)
) {

    /** Current value of the active counter (safe default to 0). */
    val activeValue: Int get() = counters[activeCounter] ?: 0

    /** Convenience accessor for any counter (safe default to 0). */
    fun valueOf(type: CounterType): Int = counters[type] ?: 0

    companion object {
        /**
         * Default counters map for a new player.
         * Keep this here so both ViewModel and tests can reuse it.
         */
        fun defaultCounters(startLife: Int): Map<CounterType, Int> = mapOf(
            CounterType.LIFE to startLife,
            CounterType.POISON to 0,
            CounterType.ENERGY to 0,
            CounterType.EXPERIENCE to 0,
            CounterType.RAD to 0,
            CounterType.BOUNTY to 0
        )
    }
}
