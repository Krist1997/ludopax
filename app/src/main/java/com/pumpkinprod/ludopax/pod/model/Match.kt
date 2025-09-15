package com.pumpkinprod.ludopax.pod.model


data class MatchId(val round: Int, val index: Int)

data class Match(
    val id: Int,
    val round: Int,
    val player1: Player?,
    val player2: Player?,
    val winner: Player? = null
)
