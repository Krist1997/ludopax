package com.pumpkinprod.ludopax.randomizer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class RandomizerUiState(
    val totalPacks: String = "24",
    val packsToPick: String = "8",
    val result: List<Int> = emptyList(),
    val errorMessage: String? = null
)

class RandomizerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RandomizerUiState())
    val uiState: StateFlow<RandomizerUiState> = _uiState

    fun updateTotalPacks(value: String) {
        _uiState.update { it.copy(totalPacks = value) }
    }

    fun updatePacksToPick(value: String) {
        _uiState.update { it.copy(packsToPick = value) }
    }

    fun pickPacks() {
        val total = _uiState.value.totalPacks.toIntOrNull()
        val count = _uiState.value.packsToPick.toIntOrNull()

        if (total == null || count == null || count > total || count <= 0) {
            _uiState.update { it.copy(errorMessage = "Invalid input", result = emptyList()) }
            return
        }

        val packPool = (1..total).toMutableList()
        val result = List(count) {
            val index = Random.nextInt(packPool.size)
            packPool.removeAt(index)
        }

        _uiState.update { it.copy(result = result, errorMessage = null) }
    }

    fun reset() {
        _uiState.update {
            it.copy(result = emptyList(), errorMessage = null)
        }
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


    fun pickPacksReducingMax() {
        val total = _uiState.value.totalPacks.toIntOrNull()
        val count = _uiState.value.packsToPick.toIntOrNull()

        if (total == null || count == null || total <= 0 || count <= 0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter valid positive numbers",
                result = emptyList()
            )
            return
        }

        if (count > total) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Packs to pick cannot exceed total packs",
                result = emptyList()
            )
            return
        }

        val picked = mutableListOf<Int>()
        var currentMax = total

        repeat(count) {
            val number = (1..currentMax).random()
            picked.add(number)
            currentMax-- // Reduce the range for the next pick
        }

        _uiState.value = _uiState.value.copy(
            result = picked,
            errorMessage = null
        )
    }
}
