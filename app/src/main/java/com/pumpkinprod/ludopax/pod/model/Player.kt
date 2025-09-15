package com.pumpkinprod.ludopax.pod.model

data class Player(
    val id: Int,
    val name: String
)

data class SlotRef(val matchId: Int, val isPlayer1: Boolean)
