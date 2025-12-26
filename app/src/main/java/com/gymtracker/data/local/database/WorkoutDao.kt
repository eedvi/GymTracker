package com.gymtracker.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymtracker.data.local.entity.WorkoutEntity
import com.gymtracker.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // Workouts
    @Query("SELECT * FROM workouts ORDER BY startTime DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE startTime >= :startOfDay AND startTime < :endOfDay")
    fun getWorkoutsForDate(startOfDay: Long, endOfDay: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    // Workout Sets
    @Query("""
        SELECT ws.*, e.name as exerciseName
        FROM workout_sets ws
        INNER JOIN exercises e ON ws.exerciseId = e.id
        WHERE ws.workoutId = :workoutId
        ORDER BY ws.id
    """)
    fun getWorkoutSets(workoutId: Long): Flow<List<WorkoutSetWithExercise>>

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId AND exerciseId = :exerciseId ORDER BY setNumber")
    fun getSetsForExercise(workoutId: Long, exerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSet(workoutSet: WorkoutSetEntity): Long

    @Update
    suspend fun updateWorkoutSet(workoutSet: WorkoutSetEntity)

    @Delete
    suspend fun deleteWorkoutSet(workoutSet: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE workoutId = :workoutId")
    suspend fun deleteAllSetsForWorkout(workoutId: Long)

    // Stats queries
    @Query("""
        SELECT MAX(weight) FROM workout_sets
        WHERE exerciseId = :exerciseId
    """)
    suspend fun getPersonalRecord(exerciseId: Long): Float?

    @Query("SELECT COUNT(*) FROM workouts WHERE startTime >= :since")
    suspend fun getWorkoutCountSince(since: Long): Int

    @Query("""
        SELECT COALESCE(SUM(ws.weight * ws.reps), 0)
        FROM workout_sets ws
        INNER JOIN workouts w ON ws.workoutId = w.id
        WHERE w.startTime >= :since AND w.endTime IS NOT NULL
    """)
    suspend fun getVolumeSince(since: Long): Float
}

data class WorkoutSetWithExercise(
    val id: Long,
    val workoutId: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val rpe: Int?,
    val exerciseName: String
)
