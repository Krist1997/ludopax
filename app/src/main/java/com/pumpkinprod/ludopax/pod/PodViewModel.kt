package com.pumpkinprod.ludopax.pod

import androidx.lifecycle.ViewModel
import com.pumpkinprod.ludopax.pod.model.Match
import com.pumpkinprod.ludopax.pod.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Stack
import kotlin.math.ceil
import kotlin.math.log2

data class BracketUiState(
    val players: List<Player> = emptyList(),
    val matches: List<Match> = emptyList(),
    val totalRounds: Int = 1,
    val isBracketStarted: Boolean = false
)

class PodViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BracketUiState())
    val uiState: StateFlow<BracketUiState> = _uiState

    private val history = Stack<BracketUiState>() // For undo

    /** Set players before generating bracket */
    fun setPlayers(names: List<String>) {
        val players = names.mapIndexed { i, name -> Player(i, name) }
        _uiState.value = _uiState.value.copy(players = players, isBracketStarted = false)
    }

    /** Start a new bracket */
    fun startBracket() {
        val players = _uiState.value.players
        if (players.isEmpty()) return

        val rounds = ceil(log2(players.size.toDouble())).toInt()
        val totalSlots = 1 shl rounds

        // Create the full list of slots (players + BYEs) and shuffle it
        val slots = (players + List(totalSlots - players.size) {
            Player(-1, "BYE")
        }).shuffled()

        val firstRoundMatches = slots.chunked(2).mapIndexed { i, pair ->
            Match(
                id = i,
                round = 1,
                player1 = pair.getOrNull(0),
                player2 = pair.getOrNull(1)
            )
        }

        _uiState.value = BracketUiState(
            players = players,
            matches = firstRoundMatches,
            totalRounds = rounds,
            isBracketStarted = true
        )
        history.clear()
    }


    /** Select winner and auto-advance */
    fun selectWinner(matchId: Int, winner: Player) {
        history.push(_uiState.value)

        val matches = _uiState.value.matches.toMutableList()
        val match = matches.firstOrNull { it.id == matchId } ?: return
        matches[matches.indexOf(match)] = match.copy(winner = winner)

        // Always advance winner to next round if possible
        if (match.round < _uiState.value.totalRounds) {
            val nextRound = match.round + 1
            val nextMatchIndex = match.id / 2
            val nextMatchId = (nextRound * 100) + nextMatchIndex

            val existingNextMatch = matches.firstOrNull { it.id == nextMatchId }
            val updatedNext = existingNextMatch ?: Match(
                id = nextMatchId,
                round = nextRound,
                player1 = null,
                player2 = null
            )

            val filledNext = if (match.id % 2 == 0) {
                updatedNext.copy(player1 = winner)
            } else {
                updatedNext.copy(player2 = winner)
            }

            if (existingNextMatch == null) {
                matches.add(filledNext)
            } else {
                matches[matches.indexOf(existingNextMatch)] = filledNext
            }
        }

        _uiState.value = _uiState.value.copy(matches = matches)
    }

    /** Undo last winner selection */
    fun undo() {
        if (history.isNotEmpty()) {
            _uiState.value = history.pop()
        }
    }

    /** Reset whole bracket */
    fun reset() {
        _uiState.value = BracketUiState()
        history.clear()
    }
}
