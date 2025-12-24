package com.gymtracker.domain.model

data class Workout(
    val id: Long = 0,
    val routineId: Long? = null,
    val routineName: String? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val notes: String? = null,
    val sets: List<WorkoutSet> = emptyList()
)

data class WorkoutSet(
    val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val rpe: Int? = null
)
