package com.gymtracker.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.domain.model.AppSettings
import com.gymtracker.domain.model.Exercise
import com.gymtracker.domain.model.RoutineExercise
import com.gymtracker.domain.model.WeightUnit
import com.gymtracker.domain.model.WorkoutSet
import com.gymtracker.domain.repository.ExerciseRepository
import com.gymtracker.domain.repository.RoutineRepository
import com.gymtracker.domain.repository.SettingsRepository
import com.gymtracker.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _workoutId = MutableStateFlow<Long?>(null)
    private val _workoutSets = MutableStateFlow<List<WorkoutSet>>(emptyList())
    private val _selectedExercise = MutableStateFlow<Exercise?>(null)
    private val _isAddingSet = MutableStateFlow(false)
    private val _restTimerSeconds = MutableStateFlow(0)
    private val _isRestTimerRunning = MutableStateFlow(false)
    private val _workoutStartTime = MutableStateFlow<Long?>(null)
    private val _routineExercises = MutableStateFlow<List<RoutineExercise>>(emptyList())
    private val _routineName = MutableStateFlow<String?>(null)

    private var timerJob: Job? = null

    val selectedExercise: StateFlow<Exercise?> = _selectedExercise
    val isAddingSet: StateFlow<Boolean> = _isAddingSet
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds
    val isRestTimerRunning: StateFlow<Boolean> = _isRestTimerRunning

    val routineExercises: StateFlow<List<RoutineExercise>> = _routineExercises
    val routineName: StateFlow<String?> = _routineName

    val weightUnit: StateFlow<WeightUnit> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )
        .let { flow ->
            MutableStateFlow(WeightUnit.KG).also { unitFlow ->
                viewModelScope.launch {
                    flow.collect { settings ->
                        unitFlow.value = settings.weightUnit
                    }
                }
            }
        }

    val uiState: StateFlow<WorkoutUiState> = combine(
        _workoutId,
        _workoutSets,
        _workoutStartTime,
        _routineName
    ) { workoutId, sets, startTime, routineName ->
        if (workoutId == null) {
            WorkoutUiState.NoActiveWorkout
        } else {
            val groupedSets = sets.groupBy { it.exerciseId }
                .map { (exerciseId, exerciseSets) ->
                    ExerciseWithSets(
                        exerciseId = exerciseId,
                        exerciseName = exerciseSets.firstOrNull()?.exerciseName ?: "Unknown",
                        sets = exerciseSets.sortedBy { it.setNumber }
                    )
                }
            WorkoutUiState.Active(
                workoutId = workoutId,
                startTime = startTime ?: System.currentTimeMillis(),
                exercisesWithSets = groupedSets,
                totalSets = sets.size,
                totalVolume = sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat(),
                routineName = routineName
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutUiState.Loading
    )

    init {
        checkForActiveWorkout()
    }

    private fun checkForActiveWorkout() {
        viewModelScope.launch {
            val activeWorkout = workoutRepository.getActiveWorkout()
            if (activeWorkout != null) {
                _workoutId.value = activeWorkout.id
                _workoutStartTime.value = activeWorkout.startTime
                loadWorkoutSets(activeWorkout.id)
            }
        }
    }

    private fun loadWorkoutSets(workoutId: Long) {
        viewModelScope.launch {
            workoutRepository.getWorkoutSets(workoutId).collect { sets ->
                _workoutSets.value = sets
            }
        }
    }

    fun startNewWorkout() {
        viewModelScope.launch {
            val workoutId = workoutRepository.startWorkout()
            _workoutId.value = workoutId
            _workoutStartTime.value = System.currentTimeMillis()
            _workoutSets.value = emptyList()
            _routineExercises.value = emptyList()
            _routineName.value = null
        }
    }

    fun startFromRoutine(routineId: Long) {
        viewModelScope.launch {
            val routine = routineRepository.getRoutineWithExercises(routineId) ?: return@launch
            val workoutId = workoutRepository.startWorkout(routineId)
            _workoutId.value = workoutId
            _workoutStartTime.value = System.currentTimeMillis()
            _workoutSets.value = emptyList()
            _routineExercises.value = routine.exercises
            _routineName.value = routine.name
        }
    }

    fun selectExercise(exercise: Exercise) {
        _selectedExercise.value = exercise
        _isAddingSet.value = true
    }

    fun selectExerciseById(exerciseId: Long, exerciseName: String) {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExerciseById(exerciseId)
            if (exercise != null) {
                _selectedExercise.value = exercise
                _isAddingSet.value = true
            }
        }
    }

    fun clearSelectedExercise() {
        _selectedExercise.value = null
        _isAddingSet.value = false
    }

    fun addSet(weight: Float, reps: Int, rpe: Int? = null) {
        val workoutId = _workoutId.value ?: return
        val exercise = _selectedExercise.value ?: return

        viewModelScope.launch {
            workoutRepository.addSet(
                workoutId = workoutId,
                exerciseId = exercise.id,
                weight = weight,
                reps = reps,
                rpe = rpe
            )
            loadWorkoutSets(workoutId)
            _isAddingSet.value = false

            // Start rest timer after adding set
            startRestTimer(90) // Default 90 seconds rest
        }
    }

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            workoutRepository.deleteSet(set.id)
            val workoutId = _workoutId.value ?: return@launch
            loadWorkoutSets(workoutId)
        }
    }

    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _restTimerSeconds.value = seconds
        _isRestTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_restTimerSeconds.value > 0) {
                delay(1000)
                _restTimerSeconds.value -= 1
            }
            _isRestTimerRunning.value = false
        }
    }

    fun stopRestTimer() {
        timerJob?.cancel()
        _isRestTimerRunning.value = false
        _restTimerSeconds.value = 0
    }

    fun addRestTime(seconds: Int) {
        _restTimerSeconds.value += seconds
    }

    fun finishWorkout(notes: String? = null) {
        val workoutId = _workoutId.value ?: return
        viewModelScope.launch {
            workoutRepository.endWorkout(workoutId, notes)
            _workoutId.value = null
            _workoutSets.value = emptyList()
            _workoutStartTime.value = null
            stopRestTimer()
        }
    }

    fun cancelWorkout() {
        val workoutId = _workoutId.value ?: return
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workoutId)
            _workoutId.value = null
            _workoutSets.value = emptyList()
            _workoutStartTime.value = null
            stopRestTimer()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

sealed interface WorkoutUiState {
    data object Loading : WorkoutUiState
    data object NoActiveWorkout : WorkoutUiState
    data class Active(
        val workoutId: Long,
        val startTime: Long,
        val exercisesWithSets: List<ExerciseWithSets>,
        val totalSets: Int,
        val totalVolume: Float,
        val routineName: String? = null
    ) : WorkoutUiState
}

data class ExerciseWithSets(
    val exerciseId: Long,
    val exerciseName: String,
    val sets: List<WorkoutSet>
)
