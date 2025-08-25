package com.pumpkinprod.ludopax.lifecounter

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

enum class CounterType(val label: String) { LIFE("Life"), POISON("Poison"), ENERGY("Energy"), EXPERIENCE("Exp") }

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

data class LifeUiState(
    val players: List<PlayerState> = emptyList()
)

class LifeCounterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LifeUiState())
    val uiState: StateFlow<LifeUiState> = _uiState

    fun setPlayers(count: Int) {
        val startLife = if (count <= 2) 20 else 40
        val palette = listOf(
            Color(0xFFE57373), // red
            Color(0xFF64B5F6), // blue
            Color(0xFF81C784), // green
            Color(0xFFFFF176), // yellow
            Color(0xFFBA68C8), // purple
            Color(0xFF4DD0E1)  // cyan
        )

        val players = List(count) { i ->
            PlayerState(
                id = i,
                color = palette[i % palette.size],
                counters = mapOf(
                    CounterType.LIFE to startLife,
                    CounterType.POISON to 0,
                    CounterType.ENERGY to 0,
                    CounterType.EXPERIENCE to 0
                )
            )
        }
        _uiState.value = LifeUiState(players)
    }

    fun incrementCounter(playerId: Int, type: CounterType, delta: Int) {
        _uiState.update { state ->
            val updated = state.players.map { p ->
                if (p.id == playerId) {
                    val newCounters = p.counters.toMutableMap()
                    newCounters[type] = (newCounters[type] ?: 0) + delta
                    p.copy(counters = newCounters)
                } else p
            }
            state.copy(players = updated)
        }
    }

    fun switchCounter(playerId: Int, newType: CounterType) {
        _uiState.update { state ->
            state.copy(players = state.players.map { p ->
                if (p.id == playerId) p.copy(activeCounter = newType) else p
            })
        }
    }

    fun reset() {
        val count = _uiState.value.players.size
        if (count > 0) setPlayers(count)
    }

    fun clearPlayers() {
        _uiState.value = LifeUiState(emptyList())
    }
}
