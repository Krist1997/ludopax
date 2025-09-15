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
    /** Set players before generating bracket */
    fun setPlayers(rawNames: List<String>) {
        // Trim, drop empties, fill defaults, enforce uniqueness
        val cleaned = rawNames.mapIndexed { i, s ->
            val t = s.trim()
            if (t.isEmpty()) "Player ${i + 1}" else t
        }.let { list ->
            // de-duplicate by appending (#) when needed
            val seen = mutableMapOf<String, Int>()
            list.map { n ->
                val count = (seen[n] ?: 0) + 1
                seen[n] = count
                if (count == 1) n else "$n ($count)"
            }
        }

        val players = cleaned.mapIndexed { i, name -> Player(i, name) }
        _uiState.value = _uiState.value.copy(players = players, isBracketStarted = false)
    }


    /** Start a new bracket */
    fun startBracket() {
        val players = _uiState.value.players
        if (players.isEmpty()) return

        val rounds = ceil(log2(players.size.toDouble())).toInt()
        val totalSlots = 1 shl rounds

        // Players + BYEs, then shuffle to randomize pairings and BYE placement
        val slots = (players + List(totalSlots - players.size) { Player(-1, "BYE") }).shuffled()

        // Build round 1
        val firstRoundMatches = slots.chunked(2).mapIndexed { i, pair ->
            Match(
                id = i,                 // round 1 ids: 0..(n/2 - 1)
                round = 1,
                player1 = pair.getOrNull(0),
                player2 = pair.getOrNull(1)
            )
        }.toMutableList()

        // Auto-advance any BYEs (may create next-round matches)
        autoAdvanceByes(firstRoundMatches, rounds)

        _uiState.value = BracketUiState(
            players = players,
            matches = firstRoundMatches,
            totalRounds = rounds,
            isBracketStarted = true
        )
        history.clear()
    }


    private fun nextMatchId(current: Match): Int {
        val nextRound = current.round + 1
        val nextIndex = current.id / 2
        return (nextRound * 100) + nextIndex
    }


    /** Select winner and auto-advance */
    fun selectWinner(matchId: Int, winner: Player) {
        val state = _uiState.value
        val matches = state.matches.toMutableList()
        val idx = matches.indexOfFirst { it.id == matchId }
        if (idx == -1) return

        val match = matches[idx]
        if (match.winner?.id == winner.id) return // no-op
        if (match.player1 == null || match.player2 == null) return

        history.push(state)
        matches[idx] = match.copy(winner = winner)

        if (match.round < state.totalRounds) {
            val nextMatchId = nextMatchId(match)
            val existing = matches.firstOrNull { it.id == nextMatchId }
            val base = existing ?: Match(
                id = nextMatchId, round = match.round + 1, player1 = null, player2 = null
            )
            val filled =
                if (match.id % 2 == 0) base.copy(player1 = winner) else base.copy(player2 = winner)
            if (existing == null) matches.add(filled) else matches[matches.indexOf(existing)] =
                filled

            // Optional: if the other slot is a BYE, auto-advance
            autoAdvanceByes(matches, state.totalRounds)
        }

        _uiState.value = state.copy(matches = matches)
    }


    private fun autoAdvanceByes(
        matches: MutableList<Match>,
        totalRounds: Int
    ) {
        val BYE_ID = -1
        var progressed: Boolean
        do {
            progressed = false
            // Process earlier rounds first; stable ids second
            matches.sortWith(compareBy<Match> { it.round }.thenBy { it.id })

            // Work on a snapshot so we can modify the original list safely
            val snapshot = matches.toList()
            for (m in snapshot) {
                if (m.winner != null) continue

                val p1 = m.player1
                val p2 = m.player2

                val hasBye = (p1?.id == BYE_ID) xor (p2?.id == BYE_ID)
                val bothReal = (p1 != null && p1.id != BYE_ID) && (p2 != null && p2.id != BYE_ID)

                // Only auto-advance when exactly one side is a BYE and the other is a real player
                if (hasBye) {
                    val winner = if (p1?.id == BYE_ID) p2!! else p1!!

                    // Mark current match winner
                    val idx = matches.indexOfFirst { it.id == m.id && it.round == m.round }
                    if (idx != -1) {
                        matches[idx] = m.copy(winner = winner)
                    }

                    // Advance to next round
                    if (m.round < totalRounds) {
                        val nextRound = m.round + 1
                        val nextMatchIndex = m.id / 2
                        val nextMatchId = (nextRound * 100) + nextMatchIndex

                        val existingNext = matches.firstOrNull { it.id == nextMatchId }
                        val baseNext = existingNext ?: Match(
                            id = nextMatchId,
                            round = nextRound,
                            player1 = null,
                            player2 = null
                        )

                        val filledNext = if (m.id % 2 == 0) {
                            baseNext.copy(player1 = winner)
                        } else {
                            baseNext.copy(player2 = winner)
                        }

                        if (existingNext == null) {
                            matches.add(filledNext)
                        } else {
                            matches[matches.indexOf(existingNext)] = filledNext
                        }
                    }

                    progressed = true
                } else {
                    // Do nothing:
                    // - bothReal -> wait for user to pick a winner
                    // - not fully formed yet (null slot) -> will be handled in a later pass
                }
            }
            // Loop again in case advancing created a new match that also has a BYE
        } while (progressed)
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

    // In ViewModel
    private var lastSeed: Long = System.currentTimeMillis()

    fun shufflePlayers() {
        val players = _uiState.value.players
        if (players.isEmpty()) return
        lastSeed = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(
            players = players.shuffled(java.util.Random(lastSeed))
        )
    }

}
