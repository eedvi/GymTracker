package com.gymtracker.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.domain.model.Exercise
import com.gymtracker.domain.model.ExerciseCategory
import com.gymtracker.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<ExerciseCategory?>(null)
    val selectedCategory: StateFlow<ExerciseCategory?> = _selectedCategory

    val uiState: StateFlow<ExercisesUiState> = combine(
        exerciseRepository.getAllExercises(),
        _searchQuery,
        _selectedCategory
    ) { exercises, query, category ->
        val filtered = exercises
            .filter { exercise ->
                (category == null || exercise.category == category) &&
                (query.isBlank() || exercise.name.contains(query, ignoreCase = true))
            }

        val grouped = filtered.groupBy { it.category }

        ExercisesUiState(
            exercises = filtered,
            groupedExercises = grouped,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExercisesUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            exerciseRepository.initializeDefaultExercises()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: ExerciseCategory?) {
        _selectedCategory.value = category
    }
}

data class ExercisesUiState(
    val exercises: List<Exercise> = emptyList(),
    val groupedExercises: Map<ExerciseCategory, List<Exercise>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)
