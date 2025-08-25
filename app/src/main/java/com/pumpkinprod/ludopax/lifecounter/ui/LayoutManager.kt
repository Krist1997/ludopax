package com.pumpkinprod.ludopax.lifecounter.ui

fun getRowSpec(playerCount: Int): List<Int> = when (playerCount) {
    2 -> listOf(1, 1)
    3 -> listOf(2, 1)
    4 -> listOf(2, 2)
    5 -> listOf(2, 2, 1)
    6 -> listOf(2, 2, 2)
    else -> List(playerCount) { 1 }
}


