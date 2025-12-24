package com.gymtracker.domain.repository

import com.gymtracker.domain.model.Exercise
import com.gymtracker.domain.model.ExerciseCategory
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    fun getExercisesByCategory(category: ExerciseCategory): Flow<List<Exercise>>
    fun searchExercises(query: String): Flow<List<Exercise>>
    suspend fun getExerciseById(id: Long): Exercise?
    suspend fun insertExercise(exercise: Exercise): Long
    suspend fun updateExercise(exercise: Exercise)
    suspend fun deleteExercise(exercise: Exercise)
    suspend fun initializeDefaultExercises()
}
