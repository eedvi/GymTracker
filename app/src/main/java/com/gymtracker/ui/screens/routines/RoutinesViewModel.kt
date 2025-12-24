package com.gymtracker.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.domain.model.Routine
import com.gymtracker.domain.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineRepository: RoutineRepository
) : ViewModel() {

    val uiState: StateFlow<RoutinesUiState> = routineRepository.getAllRoutines()
        .map { routines ->
            RoutinesUiState(
                routines = routines,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RoutinesUiState(isLoading = true)
        )

    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routineId)
        }
    }
}

data class RoutinesUiState(
    val routines: List<Routine> = emptyList(),
    val isLoading: Boolean = false
)
