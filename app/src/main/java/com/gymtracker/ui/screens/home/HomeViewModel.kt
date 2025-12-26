package com.gymtracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.domain.model.WeightUnit
import com.gymtracker.domain.model.Workout
import com.gymtracker.domain.repository.SettingsRepository
import com.gymtracker.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _workoutsThisWeek = MutableStateFlow(0)
    private val _hasActiveWorkout = MutableStateFlow(false)
    private val _weeklyVolume = MutableStateFlow(0f)

    val uiState: StateFlow<HomeUiState> = combine(
        workoutRepository.getAllWorkouts(),
        _workoutsThisWeek,
        _hasActiveWorkout,
        _weeklyVolume,
        settingsRepository.getSettings()
    ) { workouts, weekCount, hasActive, volume, settings ->
        val recentWorkouts = workouts
            .filter { it.endTime != null }
            .take(5)

        HomeUiState(
            recentWorkouts = recentWorkouts,
            workoutsThisWeek = weekCount,
            hasActiveWorkout = hasActive,
            weeklyVolume = volume,
            weightUnit = settings.weightUnit,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _workoutsThisWeek.value = workoutRepository.getWorkoutCountThisWeek()
            _hasActiveWorkout.value = workoutRepository.getActiveWorkout() != null
            _weeklyVolume.value = workoutRepository.getWeeklyVolume()
        }
    }

    fun refresh() {
        loadStats()
    }
}

data class HomeUiState(
    val recentWorkouts: List<Workout> = emptyList(),
    val workoutsThisWeek: Int = 0,
    val hasActiveWorkout: Boolean = false,
    val weeklyVolume: Float = 0f,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val isLoading: Boolean = false
)
