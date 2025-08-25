package com.pumpkinprod.ludopax.undercity

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UndercityViewModel : ViewModel() {

    private val _pawnPositions = MutableStateFlow(
        listOf(Offset(100f, 100f), Offset(200f, 100f))
    )
    val pawnPositions: StateFlow<List<Offset>> = _pawnPositions

    fun updatePawn(index: Int, newPos: Offset) {
        _pawnPositions.value = _pawnPositions.value.toMutableList().also {
            it[index] = newPos
        }
    }
}
