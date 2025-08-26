package com.pumpkinprod.ludopax.lifecounter.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.pumpkinprod.ludopax.lifecounter.domain.CounterType
import com.pumpkinprod.ludopax.lifecounter.domain.PlayerState
import com.pumpkinprod.ludopax.lifecounter.ui.state.LifeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LifeCounterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LifeUiState())
    val uiState: StateFlow<LifeUiState> = _uiState

    fun setPlayers(count: Int) {
        val startLife = if (count <= 2) 20 else 40
        val palette = PALETTE

        val players = List(count) { i ->
            PlayerState(
                id = i,
                color = palette[i % palette.size],
                counters = PlayerState.defaultCounters(startLife = startLife)
            )
        }
        _uiState.value = LifeUiState(players)
    }

    fun incrementCounter(playerId: Int, type: CounterType, delta: Int) {
        _uiState.updateEachPlayer(playerId) { p ->
            val newCounters = p.counters.toMutableMap().apply {
                this[type] = (this[type] ?: 0) + delta
            }
            p.copy(counters = newCounters)
        }
    }

    fun switchCounter(playerId: Int, newType: CounterType) {
        _uiState.updateEachPlayer(playerId) { p -> p.copy(activeCounter = newType) }
    }

    fun reset() {
        val count = _uiState.value.players.size
        if (count > 0) setPlayers(count)
    }

    fun clearPlayers() {
        _uiState.value = LifeUiState(emptyList())
    }

    // --- Private helpers ---

    private fun MutableStateFlow<LifeUiState>.updateEachPlayer(
        playerId: Int,
        transform: (PlayerState) -> PlayerState
    ) {
        update { state ->
            state.copy(
                players = state.players.map { p -> if (p.id == playerId) transform(p) else p }
            )
        }
    }

    private companion object {
        // Centralized color palette (unchanged values)
        val PALETTE = listOf(
            Color(0xFFE57373), // red
            Color(0xFF64B5F6), // blue
            Color(0xFF81C784), // green
            Color(0xFFFFF176), // yellow
            Color(0xFFBA68C8), // purple
            Color(0xFF4DD0E1)  // cyan
        )
    }
}
