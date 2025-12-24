package com.gymtracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.domain.model.Workout
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
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _workoutsThisWeek = MutableStateFlow(0)
    private val _hasActiveWorkout = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        workoutRepository.getAllWorkouts(),
        _workoutsThisWeek,
        _hasActiveWorkout
    ) { workouts, weekCount, hasActive ->
        val recentWorkouts = workouts
            .filter { it.endTime != null }
            .take(5)

        HomeUiState(
            recentWorkouts = recentWorkouts,
            workoutsThisWeek = weekCount,
            hasActiveWorkout = hasActive,
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
    val isLoading: Boolean = false
)
