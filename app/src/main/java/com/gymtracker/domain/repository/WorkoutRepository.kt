package com.gymtracker.domain.repository

import com.gymtracker.domain.model.Workout
import com.gymtracker.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<Workout>>
    fun getWorkoutSets(workoutId: Long): Flow<List<WorkoutSet>>
    suspend fun getWorkoutById(id: Long): Workout?
    suspend fun getActiveWorkout(): Workout?
    suspend fun startWorkout(routineId: Long? = null): Long
    suspend fun endWorkout(workoutId: Long, notes: String? = null)
    suspend fun deleteWorkout(workoutId: Long)
    suspend fun addSet(workoutId: Long, exerciseId: Long, weight: Float, reps: Int, rpe: Int? = null): Long
    suspend fun updateSet(set: WorkoutSet)
    suspend fun deleteSet(setId: Long)
    suspend fun getPersonalRecord(exerciseId: Long): Float?
    suspend fun getWorkoutCountThisWeek(): Int
}
