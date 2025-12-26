package com.gymtracker.ui.screens.workoutdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.domain.model.WeightUnit
import com.gymtracker.domain.model.Workout
import com.gymtracker.domain.model.WorkoutSet
import com.gymtracker.domain.repository.SettingsRepository
import com.gymtracker.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val workoutId: Long = savedStateHandle.get<Long>("workoutId") ?: 0L

    private val _uiState = MutableStateFlow<WorkoutDetailUiState>(WorkoutDetailUiState.Loading)
    val uiState: StateFlow<WorkoutDetailUiState> = _uiState

    private val _weightUnit = MutableStateFlow(WeightUnit.KG)
    val weightUnit: StateFlow<WeightUnit> = _weightUnit

    init {
        loadWorkoutDetails()
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _weightUnit.value = settings.weightUnit
            }
        }
    }

    private fun loadWorkoutDetails() {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutById(workoutId)
            if (workout == null) {
                _uiState.value = WorkoutDetailUiState.Error("Workout not found")
                return@launch
            }

            val sets = workoutRepository.getWorkoutSets(workoutId).first()
            val groupedSets = sets.groupBy { it.exerciseId }
                .map { (exerciseId, exerciseSets) ->
                    ExerciseDetail(
                        exerciseId = exerciseId,
                        exerciseName = exerciseSets.firstOrNull()?.exerciseName ?: "Unknown",
                        sets = exerciseSets.sortedBy { it.setNumber }
                    )
                }

            val totalVolume = sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()

            _uiState.value = WorkoutDetailUiState.Success(
                workout = workout,
                exercises = groupedSets,
                totalVolume = totalVolume,
                totalSets = sets.size
            )
        }
    }
}

sealed interface WorkoutDetailUiState {
    data object Loading : WorkoutDetailUiState
    data class Error(val message: String) : WorkoutDetailUiState
    data class Success(
        val workout: Workout,
        val exercises: List<ExerciseDetail>,
        val totalVolume: Float,
        val totalSets: Int
    ) : WorkoutDetailUiState
}

data class ExerciseDetail(
    val exerciseId: Long,
    val exerciseName: String,
    val sets: List<WorkoutSet>
)
