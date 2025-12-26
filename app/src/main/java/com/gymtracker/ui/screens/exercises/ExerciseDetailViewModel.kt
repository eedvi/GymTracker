package com.gymtracker.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.domain.model.Exercise
import com.gymtracker.domain.model.WeightUnit
import com.gymtracker.domain.repository.ExerciseHistoryItem
import com.gymtracker.domain.repository.ExerciseRepository
import com.gymtracker.domain.repository.SettingsRepository
import com.gymtracker.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: 0L

    private val _uiState = MutableStateFlow<ExerciseDetailUiState>(ExerciseDetailUiState.Loading)
    val uiState: StateFlow<ExerciseDetailUiState> = _uiState

    private val _weightUnit = MutableStateFlow(WeightUnit.KG)
    val weightUnit: StateFlow<WeightUnit> = _weightUnit

    init {
        loadExerciseDetails()
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _weightUnit.value = settings.weightUnit
            }
        }
    }

    private fun loadExerciseDetails() {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExerciseById(exerciseId)
            if (exercise == null) {
                _uiState.value = ExerciseDetailUiState.Error("Exercise not found")
                return@launch
            }

            val pr = workoutRepository.getPersonalRecord(exerciseId)
            val workoutCount = workoutRepository.getExerciseWorkoutCount(exerciseId)
            val totalVolume = workoutRepository.getExerciseTotalVolume(exerciseId)
            val history = workoutRepository.getExerciseHistory(exerciseId).first()

            _uiState.value = ExerciseDetailUiState.Success(
                exercise = exercise,
                personalRecord = pr,
                workoutCount = workoutCount,
                totalVolume = totalVolume,
                history = history.take(10) // Last 10 workouts
            )
        }
    }
}

sealed interface ExerciseDetailUiState {
    data object Loading : ExerciseDetailUiState
    data class Error(val message: String) : ExerciseDetailUiState
    data class Success(
        val exercise: Exercise,
        val personalRecord: Float?,
        val workoutCount: Int,
        val totalVolume: Float,
        val history: List<ExerciseHistoryItem>
    ) : ExerciseDetailUiState
}
