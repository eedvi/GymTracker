package com.gymtracker.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymtracker.data.local.entity.RoutineEntity
import com.gymtracker.data.local.entity.RoutineExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("""
        SELECT r.*, COUNT(re.exerciseId) as exerciseCount
        FROM routines r
        LEFT JOIN routine_exercises re ON r.id = re.routineId
        GROUP BY r.id
        ORDER BY r.createdAt DESC
    """)
    fun getAllRoutinesWithCount(): Flow<List<RoutineWithExerciseCount>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): RoutineEntity?

    @Query("SELECT COUNT(*) FROM routine_exercises WHERE routineId = :routineId")
    suspend fun getExerciseCountForRoutine(routineId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    // Routine Exercises
    @Query("""
        SELECT re.*, e.name as exerciseName
        FROM routine_exercises re
        INNER JOIN exercises e ON re.exerciseId = e.id
        WHERE re.routineId = :routineId
        ORDER BY re.orderIndex
    """)
    fun getRoutineExercises(routineId: Long): Flow<List<RoutineExerciseWithName>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercise(routineExercise: RoutineExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExerciseEntity>)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteRoutineExercises(routineId: Long)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId AND exerciseId = :exerciseId")
    suspend fun deleteRoutineExercise(routineId: Long, exerciseId: Long)
}

data class RoutineExerciseWithName(
    val routineId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val targetSets: Int,
    val targetReps: String,
    val exerciseName: String
)

data class RoutineWithExerciseCount(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val exerciseCount: Int
)
