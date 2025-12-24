package com.gymtracker.domain.repository

import com.gymtracker.domain.model.Routine
import com.gymtracker.domain.model.RoutineExercise
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getAllRoutines(): Flow<List<Routine>>
    suspend fun getRoutineById(id: Long): Routine?
    suspend fun getRoutineWithExercises(id: Long): Routine?
    suspend fun createRoutine(name: String, description: String?): Long
    suspend fun updateRoutine(routine: Routine)
    suspend fun deleteRoutine(routineId: Long)
    suspend fun addExerciseToRoutine(
        routineId: Long,
        exerciseId: Long,
        orderIndex: Int,
        targetSets: Int,
        targetReps: String
    )
    suspend fun removeExerciseFromRoutine(routineId: Long, exerciseId: Long)
    suspend fun updateRoutineExercises(routineId: Long, exercises: List<RoutineExercise>)
}
