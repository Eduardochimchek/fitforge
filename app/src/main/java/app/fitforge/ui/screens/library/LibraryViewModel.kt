package app.fitforge.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fitforge.data.prefs.UserPrefsRepository
import app.fitforge.data.repo.ExerciseRepository
import app.fitforge.domain.model.Category
import app.fitforge.domain.model.Equipment
import app.fitforge.domain.model.Exercise
import app.fitforge.domain.model.Level
import app.fitforge.domain.model.Muscle
import app.fitforge.domain.model.Place
import app.fitforge.domain.model.normalize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryFilters(
    val categories: Set<Category> = emptySet(),
    val muscles: Set<Muscle> = emptySet(),
    val equipment: Set<Equipment> = emptySet(),
    val levels: Set<Level> = emptySet(),
    val place: Place? = null,
    val withMyEquipment: Boolean = false,
    val favorites: Boolean = false,
    val customOnly: Boolean = false,
    val translatedOnly: Boolean = false,
) {
    val sheetCount: Int get() = categories.size + muscles.size + equipment.size + levels.size + (if (translatedOnly) 1 else 0)
    val isEmpty: Boolean get() = this == LibraryFilters()
}

data class LibraryState(
    val loading: Boolean = true,
    val query: String = "",
    val filters: LibraryFilters = LibraryFilters(),
    val results: List<Exercise> = emptyList(),
    val total: Int = 0,
    val homeEquipment: Set<String> = setOf("nenhum"),
)

@OptIn(FlowPreview::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: ExerciseRepository,
    prefs: UserPrefsRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filters = MutableStateFlow(LibraryFilters())

    val state: StateFlow<LibraryState> = combine(
        repo.all,
        query.debounce(120),
        filters,
        prefs.settings,
    ) { all, q, f, settings ->
        LibraryState(
            loading = false,
            query = q,
            filters = f,
            results = filter(all, q, f, settings.homeEquipment),
            total = all.size,
            homeEquipment = settings.homeEquipment,
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryState())

    val rawQuery: StateFlow<String> = query

    fun setQuery(q: String) {
        query.value = q
    }

    fun updateFilters(block: (LibraryFilters) -> LibraryFilters) = filters.update(block)

    fun clearFilters() {
        filters.value = LibraryFilters()
    }

    fun toggleFavorite(e: Exercise) {
        viewModelScope.launch { repo.setFavorite(e.id, !e.favorite) }
    }

    private fun filter(all: List<Exercise>, q: String, f: LibraryFilters, home: Set<String>): List<Exercise> {
        val terms = normalize(q.trim()).split(' ').filter { it.isNotBlank() }
        val list = all.filter { e ->
            (f.categories.isEmpty() || e.category in f.categories) &&
                (f.muscles.isEmpty() || e.primaryMuscles.any { m -> f.muscles.any { it.code == m } }) &&
                (f.equipment.isEmpty() || e.equipment in f.equipment) &&
                (f.levels.isEmpty() || e.level in f.levels) &&
                (f.place == null || (f.place == Place.CASA) == e.homeFriendly) &&
                (!f.withMyEquipment || e.equipment.code in home) &&
                (!f.favorites || e.favorite) &&
                (!f.customOnly || e.custom) &&
                (!f.translatedOnly || e.translated) &&
                (terms.isEmpty() || terms.all { it in e.searchKey })
        }
        if (terms.isEmpty()) return list
        val first = terms.first()
        return list.sortedWith(
            compareByDescending<Exercise> { normalize(it.name).startsWith(first) }
                .thenByDescending { it.translated }
                .thenBy { it.name.length },
        )
    }
}
