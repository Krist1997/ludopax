package com.pumpkinprod.ludopax.lifecounter.ui.state

import com.pumpkinprod.ludopax.lifecounter.domain.PlayerState

data class LifeUiState(
    val players: List<PlayerState> = emptyList()
)
