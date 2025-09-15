package com.pumpkinprod.ludopax.pod

import androidx.lifecycle.ViewModel
import com.pumpkinprod.ludopax.pod.model.Match
import com.pumpkinprod.ludopax.pod.model.Player
import com.pumpkinprod.ludopax.pod.model.SlotRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log2

/**
 * UI state for the bracket screen.
 */
data class BracketUiState(
    val players: List<Player> = emptyList(),
    val matches: List<Match> = emptyList(),
    val totalRounds: Int = 1,          // Number of rounds including prelim (if any)
    val isBracketStarted: Boolean = false
)

/**
 * Orchestrates bracket generation, progression, undo/redo, and slot swapping.
 * Supports two paths:
 * - Power-of-two player counts: regular Round 1 bracket.
 * - Non-power-of-two: Round 0 "prelim" to trim field to the next lower power-of-two, then Round 1+.
 */
class PodViewModel : ViewModel() {

    // --- Constants / ID scheme ---------------------------------------------------------------

    private companion object {
        /** Unique base to keep prelim (round 0) match ids from colliding with other rounds. */
        const val PRELIM_ID_BASE: Int = 10_000
    }

    // --- State -------------------------------------------------------------------------------

    private val _uiState = MutableStateFlow(BracketUiState())
    val uiState: StateFlow<BracketUiState> = _uiState.asStateFlow()

    /** Simple LIFO history for undo (stores immutable UI states). */
    private val history = ArrayDeque<BracketUiState>()

    /**
     * Map: prelimMatchId -> (targetRound1MatchId, insertIntoPlayer1?)
     * Used only while a bracket generated with prelims is active.
     */
    private val prelimRouting = mutableMapOf<Int, Pair<Int, Boolean>>()

    // --- Public API --------------------------------------------------------------------------

    /**
     * Prepare the player list before generating a bracket.
     * - Trims names, fills defaults, and de-duplicates by appending (2), (3), ...
     */
    fun setPlayers(rawNames: List<String>) {
        val cleaned = rawNames.mapIndexed { i, s ->
            val t = s.trim()
            if (t.isEmpty()) "Player ${i + 1}" else t
        }.let { list ->
            val seen = mutableMapOf<String, Int>()
            list.map { n ->
                val count = (seen[n] ?: 0) + 1
                seen[n] = count
                if (count == 1) n else "$n ($count)"
            }
        }

        val players = cleaned.mapIndexed { i, name -> Player(i, name) }
        _uiState.value = _uiState.value.copy(
            players = players,
            isBracketStarted = false,
            matches = emptyList(),
            totalRounds = 1
        )
    }

    /**
     * Generate a new bracket from current players.
     * Power-of-two -> Round 1 only. Non-power-of-two -> Round 0 prelim + Round 1.
     */
    fun startBracket() {
        val players = _uiState.value.players
        if (players.isEmpty()) return

        history.clear()
        prelimRouting.clear()

        if (isPowerOfTwo(players.size)) {
            val rounds = ceil(log2(players.size.toDouble())).toInt()

            val slots = players.shuffled()
            val firstRound = slots.chunked(2).mapIndexed { i, pair ->
                Match(
                    id = i,             // Round 1 ids: 0..(n/2 - 1)
                    round = 1,
                    player1 = pair.getOrNull(0),
                    player2 = pair.getOrNull(1)
                )
            }

            _uiState.value = BracketUiState(
                players = players,
                matches = firstRound,
                totalRounds = rounds,
                isBracketStarted = true
            )
            return
        }

        // --- Play-in (prelim) plan for non-power-of-two -------------------------------------

        val n = players.size
        val rFloor = floor(log2(n.toDouble())).toInt()     // rounds after prelim
        val mainSize = 1 shl rFloor                        // target size after prelim (power-of-two)
        val prelimMatchesCount = n - mainSize              // # of prelim matches

        val shuffled = players.shuffled()
        val prelimPlayers = shuffled.take(prelimMatchesCount * 2)
        val directPlayers = shuffled.drop(prelimMatchesCount * 2)

        // Round 0: prelim matches
        val prelimMatches = prelimPlayers
            .chunked(2)
            .mapIndexed { i, pair ->
                Match(
                    id = PRELIM_ID_BASE + i,
                    round = 0,
                    player1 = pair.getOrNull(0),
                    player2 = pair.getOrNull(1)
                )
            }

        // Round 1: allocate 'mainSize' slots, some reserved for prelim winners
        val round1SlotsIndices = (0 until mainSize).shuffled()
        val placeholderSlots = round1SlotsIndices.take(prelimMatchesCount)     // filled by prelim winners
        val directSlots = (0 until mainSize).filter { it !in placeholderSlots }

        val round1Slots = Array<Player?>(mainSize) { null }
        directSlots.forEachIndexed { i, pos ->
            round1Slots[pos] = directPlayers[i]
        }

        val round1Matches = round1Slots
            .asList()
            .chunked(2)
            .mapIndexed { i, pair ->
                Match(
                    id = i,            // Round 1 ids: 0..(mainSize/2 - 1)
                    round = 1,
                    player1 = pair.getOrNull(0),
                    player2 = pair.getOrNull(1)
                )
            }

        // Map prelim winners to their Round 1 target slots
        placeholderSlots.forEachIndexed { i, slotIndex ->
            val targetMatchId = slotIndex / 2
            val toPlayer1 = (slotIndex % 2 == 0)
            prelimRouting[PRELIM_ID_BASE + i] = targetMatchId to toPlayer1
        }

        _uiState.value = BracketUiState(
            players = players,
            matches = prelimMatches + round1Matches,
            totalRounds = rFloor,         // ✅ highest round number (final)
            isBracketStarted = true
        )

    }

    /**
     * Select a winner for a match and advance them to the next round.
     * - Prelim winners are routed into their Round 1 slot.
     * - Round 1+ winners advance based on id scheme (round*100 + index).
     */
    fun selectWinner(matchId: Int, winner: Player) {
        pushHistory()

        val state = _uiState.value
        val matches = state.matches.toMutableList()
        val idx = matches.indexOfFirst { it.id == matchId }
        if (idx == -1) return
        val match = matches[idx]
        matches[idx] = match.copy(winner = winner)

        // If this was a prelim, route into Round 1
        prelimRouting[matchId]?.let { (targetId, toPlayer1) ->
            upsertPlayerIntoMatch(
                matches = matches,
                matchId = targetId,
                round = 1,
                toPlayer1 = toPlayer1,
                player = winner
            )
            _uiState.value = state.copy(matches = matches)
            return
        }

        // Regular advancement for Round 1+
        if (match.round < state.totalRounds) {
            val nextId = nextMatchId(match)
            val toP1 = (match.id % 2 == 0) // even id feeds player1, odd feeds player2
            upsertPlayerIntoMatch(
                matches = matches,
                matchId = nextId,
                round = match.round + 1,
                toPlayer1 = toP1,
                player = winner
            )
        }

        _uiState.value = state.copy(matches = matches)
    }

    /** Undo the last winner selection. */
    fun undo() {
        if (history.isNotEmpty()) {
            _uiState.value = history.removeLast()
        }
    }

    /** Reset whole bracket (players list preserved? No: clean slate). */
    fun reset() {
        _uiState.value = BracketUiState()
        history.clear()
        prelimRouting.clear()
    }

    /**
     * Swap two players' slots anywhere in the bracket **before any winner is chosen**.
     * Returns true if swap succeeded.
     */
    fun swapPlayers(a: SlotRef, b: SlotRef): Boolean {
        val state = _uiState.value
        val matches = state.matches.toMutableList()

        // Simplest/safest: block swaps after any progress
        if (matches.any { it.winner != null }) return false

        val aIdx = matches.indexOfFirst { it.id == a.matchId }
        val bIdx = matches.indexOfFirst { it.id == b.matchId }
        if (aIdx == -1 || bIdx == -1) return false

        val ma = matches[aIdx]
        val mb = matches[bIdx]

        // Prevent swapping into a Round 1 placeholder reserved for a prelim winner
        if (isPlaceholderSlot(a.matchId, a.isPlayer1) || isPlaceholderSlot(b.matchId, b.isPlayer1)) return false

        val pa = if (a.isPlayer1) ma.player1 else ma.player2
        val pb = if (b.isPlayer1) mb.player1 else mb.player2
        if (pa == null || pb == null) return false

        matches[aIdx] = if (a.isPlayer1) ma.copy(player1 = pb) else ma.copy(player2 = pb)
        matches[bIdx] = if (b.isPlayer1) mb.copy(player1 = pa) else mb.copy(player2 = pa)

        _uiState.value = state.copy(matches = matches)
        return true
    }

    // --- Helpers -----------------------------------------------------------------------------

    /** True if (id, side) is a Round 1 placeholder reserved for a prelim winner. */
    private fun isPlaceholderSlot(matchId: Int, isPlayer1: Boolean): Boolean {
        return prelimRouting.values.any { (targetId, toP1) ->
            targetId == matchId && toP1 == isPlayer1
        }
    }

    /** Compute the next match id using the current id scheme. */
    private fun nextMatchId(current: Match): Int {
        val nextRound = current.round + 1
        val nextIndex = current.id / 2
        return (nextRound * 100) + nextIndex
    }

    /** Push current UI state onto the undo stack. */
    private fun pushHistory() {
        history.addLast(_uiState.value)
    }

    /** Insert or update a player into a given match/slot. Creates the match if missing. */
    private fun upsertPlayerIntoMatch(
        matches: MutableList<Match>,
        matchId: Int,
        round: Int,
        toPlayer1: Boolean,
        player: Player
    ) {
        val existing = matches.firstOrNull { it.id == matchId }
        val base = existing ?: Match(
            id = matchId,
            round = round,
            player1 = null,
            player2 = null
        )
        val filled = if (toPlayer1) base.copy(player1 = player) else base.copy(player2 = player)
        if (existing == null) {
            matches.add(filled)
        } else {
            matches[matches.indexOf(existing)] = filled
        }
    }

    /** Classic bit trick. */
    private fun isPowerOfTwo(x: Int): Boolean = x > 0 && (x and (x - 1)) == 0
}
