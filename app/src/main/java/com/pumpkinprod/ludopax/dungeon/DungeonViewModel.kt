package com.pumpkinprod.ludopax.dungeon

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.lifecycle.ViewModel
import com.pumpkinprod.ludopax.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class DungeonType(val label: String, @DrawableRes val imageRes: Int) {
    UNDERCITY("Undercity", R.drawable.dungeons_undercity),
    BALDURS_GATE_WILDERNESS("Baldur's Gate Wilderness", R.drawable.dungeons_baldurs_gate_wilderness),
    DUNGEON_OF_THE_MAD_MAGE("Dungeon of the Mad Mage", R.drawable.dungeons_dungeon_of_the_mad_mage),
    LOST_MINE_OF_PHANDELVER("Lost Mine of Phandelver", R.drawable.dungeons_lost_mine_of_phandelver),
    TOMB_OF_ANNIHILATION("Tomb of Annihilation", R.drawable.dungeons_tomb_of_annihilation);
}

const val MAX_PAWNS = 6

data class DungeonUiState(
    val selectedDungeon: DungeonType = DungeonType.UNDERCITY,
    val pawnPositions: List<Offset> = List(MAX_PAWNS) { Offset.Unspecified }
)

class DungeonViewModel : ViewModel() {

    private val _selected = MutableStateFlow(DungeonType.UNDERCITY)

    private val _positionsByDungeon = MutableStateFlow<Map<DungeonType, List<Offset>>>(
        buildMap {
            DungeonType.entries.forEach { dungeon ->
                put(dungeon, List(MAX_PAWNS) { Offset.Unspecified })
            }
        }
    )

    val uiState: StateFlow<DungeonUiState> =
        combine(_selected, _positionsByDungeon) { selected, map ->
            DungeonUiState(
                selectedDungeon = selected,
                pawnPositions = map[selected] ?: List(MAX_PAWNS) { Offset.Unspecified }
            )
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Main.immediate),
            started = SharingStarted.Eagerly,
            initialValue = DungeonUiState()
        )

    fun selectDungeon(dungeon: DungeonType) {
        _selected.value = dungeon
        _positionsByDungeon.update { map ->
            if (map.containsKey(dungeon)) map
            else map + (dungeon to List(MAX_PAWNS) { Offset.Unspecified })
        }
    }

    fun initializeCurrentDungeonPositionsIfNeeded(defaults: List<Offset>) {
        val dungeon = _selected.value
        _positionsByDungeon.update { map ->
            val current = map[dungeon] ?: List(MAX_PAWNS) { Offset.Unspecified }
            if (current.all { !it.isSpecified }) map + (dungeon to defaults) else map
        }
    }

    fun offsetPawn(index: Int, delta: Offset) {
        val dungeon = _selected.value
        _positionsByDungeon.update { map ->
            val list = (map[dungeon] ?: List(MAX_PAWNS) { Offset.Unspecified }).toMutableList()
            val cur = list.getOrNull(index) ?: Offset.Unspecified
            if (index in 0 until MAX_PAWNS && cur.isSpecified) list[index] = cur + delta
            map + (dungeon to list)
        }
    }

    /** Set absolute positions for the CURRENT dungeon (used by Reset). */
    fun setCurrentDungeonPositions(positions: List<Offset>) {
        val dungeon = _selected.value
        _positionsByDungeon.update { map -> map + (dungeon to positions.take(MAX_PAWNS)) }
    }
}
