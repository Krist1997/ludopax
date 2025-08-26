package com.pumpkinprod.ludopax.randomizer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

enum class RandomizerMode { HOME, PACKS, D4, D6, D8, D12, D20, D6X6 }

data class RandomizerUiState(
    // Navigation
    val mode: RandomizerMode = RandomizerMode.HOME,

    // Pack randomizer
    val totalPacks: String = "24",
    val packsToPick: String = "8",
    val result: List<Int> = emptyList(),
    val errorMessage: String? = null,

    // Single-die rolls
    val lastRoll: Int? = null,
    val rollHistory: List<Int> = emptyList(),

    // 2d6 (D6×6)
    val lastRoll2d6: Pair<Int, Int>? = null,
    val history2d6: List<Pair<Int, Int>> = emptyList()
)

class RandomizerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RandomizerUiState())
    val uiState: StateFlow<RandomizerUiState> = _uiState

    /* ---------------- Navigation ---------------- */

    fun setMode(mode: RandomizerMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    /* ---------------- Packs ---------------- */

    fun updateTotalPacks(value: String) {
        _uiState.update { it.copy(totalPacks = value) }
    }

    fun updatePacksToPick(value: String) {
        _uiState.update { it.copy(packsToPick = value) }
    }

    fun pickPacksReducingMax() {
        val total = _uiState.value.totalPacks.toIntOrNull()
        val count = _uiState.value.packsToPick.toIntOrNull()

        if (total == null || count == null || total <= 0 || count <= 0) {
            _uiState.update {
                it.copy(errorMessage = "Please enter valid positive numbers", result = emptyList())
            }
            return
        }
        if (count > total) {
            _uiState.update {
                it.copy(errorMessage = "Packs to pick cannot exceed total packs", result = emptyList())
            }
            return
        }

        val picked = mutableListOf<Int>()
        var currentMax = total
        repeat(count) {
            val number = (1..currentMax).random()
            picked.add(number)
            currentMax--
        }

        _uiState.update { it.copy(result = picked, errorMessage = null) }
    }

    fun applyPreset(total: Int, pick: Int) {
        _uiState.update {
            it.copy(
                totalPacks = total.toString(),
                packsToPick = pick.toString(),
                result = emptyList(),
                errorMessage = null
            )
        }
        pickPacksReducingMax()
    }

    fun resetPacks() {
        _uiState.update { it.copy(result = emptyList(), errorMessage = null) }
    }

    /* ---------------- Single-die ---------------- */

    fun rollDie(sides: Int) {
        val roll = (1..sides).random()
        _uiState.update {
            it.copy(
                lastRoll = roll,
                rollHistory = (it.rollHistory + roll).takeLast(20)
            )
        }
    }

    fun clearDice() {
        _uiState.update { it.copy(lastRoll = null, rollHistory = emptyList()) }
    }

    /* ---------------- 2d6 (D6×6) ---------------- */

    fun roll2d6() {
        val a = (1..6).random()
        val b = (1..6).random()
        _uiState.update {
            it.copy(
                lastRoll2d6 = a to b,
                history2d6 = (it.history2d6 + (a to b)).takeLast(20)
            )
        }
    }

    fun clear2d6() {
        _uiState.update { it.copy(lastRoll2d6 = null, history2d6 = emptyList()) }
    }
}
