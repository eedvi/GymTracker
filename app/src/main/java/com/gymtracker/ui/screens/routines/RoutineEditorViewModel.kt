package com.gymtracker.ui.screens.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.domain.model.Exercise
import com.gymtracker.domain.model.Routine
import com.gymtracker.domain.model.RoutineExercise
import com.gymtracker.domain.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineEditorViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routineId: Long? = savedStateHandle.get<Long>("routineId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(RoutineEditorUiState())
    val uiState: StateFlow<RoutineEditorUiState> = _uiState.asStateFlow()

    val isEditMode: Boolean = routineId != null

    init {
        if (routineId != null) {
            loadRoutine(routineId)
        }
    }

    private fun loadRoutine(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val routine = routineRepository.getRoutineWithExercises(id)
            if (routine != null) {
                _uiState.update {
                    it.copy(
                        routineId = routine.id,
                        name = routine.name,
                        description = routine.description ?: "",
                        exercises = routine.exercises,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Routine not found") }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun addExercise(exercise: Exercise) {
        _uiState.update { state ->
            // Check if exercise already exists
            if (state.exercises.any { it.exercise.id == exercise.id }) {
                return@update state
            }

            val newExercise = RoutineExercise(
                exercise = exercise,
                orderIndex = state.exercises.size,
                targetSets = 3,
                targetReps = "8-12"
            )
            state.copy(exercises = state.exercises + newExercise)
        }
    }

    fun removeExercise(exerciseId: Long) {
        _uiState.update { state ->
            val updatedExercises = state.exercises
                .filter { it.exercise.id != exerciseId }
                .mapIndexed { index, exercise ->
                    exercise.copy(orderIndex = index)
                }
            state.copy(exercises = updatedExercises)
        }
    }

    fun updateExerciseSets(exerciseId: Long, targetSets: Int) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.map { exercise ->
                if (exercise.exercise.id == exerciseId) {
                    exercise.copy(targetSets = targetSets)
                } else {
                    exercise
                }
            }
            state.copy(exercises = updatedExercises)
        }
    }

    fun updateExerciseReps(exerciseId: Long, targetReps: String) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.map { exercise ->
                if (exercise.exercise.id == exerciseId) {
                    exercise.copy(targetReps = targetReps)
                } else {
                    exercise
                }
            }
            state.copy(exercises = updatedExercises)
        }
    }

    fun moveExerciseUp(exerciseId: Long) {
        _uiState.update { state ->
            val index = state.exercises.indexOfFirst { it.exercise.id == exerciseId }
            if (index <= 0) return@update state

            val mutableList = state.exercises.toMutableList()
            val temp = mutableList[index]
            mutableList[index] = mutableList[index - 1]
            mutableList[index - 1] = temp

            // Update order indices
            val updatedExercises = mutableList.mapIndexed { i, exercise ->
                exercise.copy(orderIndex = i)
            }
            state.copy(exercises = updatedExercises)
        }
    }

    fun moveExerciseDown(exerciseId: Long) {
        _uiState.update { state ->
            val index = state.exercises.indexOfFirst { it.exercise.id == exerciseId }
            if (index < 0 || index >= state.exercises.size - 1) return@update state

            val mutableList = state.exercises.toMutableList()
            val temp = mutableList[index]
            mutableList[index] = mutableList[index + 1]
            mutableList[index + 1] = temp

            // Update order indices
            val updatedExercises = mutableList.mapIndexed { i, exercise ->
                exercise.copy(orderIndex = i)
            }
            state.copy(exercises = updatedExercises)
        }
    }

    fun saveRoutine(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a routine name") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val id = if (routineId != null) {
                    // Update existing routine
                    routineRepository.updateRoutine(
                        Routine(
                            id = routineId,
                            name = state.name.trim(),
                            description = state.description.trim().ifBlank { null }
                        )
                    )
                    routineRepository.updateRoutineExercises(routineId, state.exercises)
                    routineId
                } else {
                    // Create new routine
                    val newId = routineRepository.createRoutine(
                        name = state.name.trim(),
                        description = state.description.trim().ifBlank { null }
                    )
                    routineRepository.updateRoutineExercises(newId, state.exercises)
                    newId
                }

                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save routine: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class RoutineEditorUiState(
    val routineId: Long? = null,
    val name: String = "",
    val description: String = "",
    val exercises: List<RoutineExercise> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)
