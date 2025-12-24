package com.gymtracker.data.repository

import com.gymtracker.data.local.database.WorkoutDao
import com.gymtracker.data.local.entity.WorkoutEntity
import com.gymtracker.data.local.entity.WorkoutSetEntity
import com.gymtracker.domain.model.Workout
import com.gymtracker.domain.model.WorkoutSet
import com.gymtracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override fun getAllWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getWorkoutSets(workoutId: Long): Flow<List<WorkoutSet>> {
        return workoutDao.getWorkoutSets(workoutId).map { sets ->
            sets.map { it.toDomain() }
        }
    }

    override suspend fun getWorkoutById(id: Long): Workout? {
        return workoutDao.getWorkoutById(id)?.toDomain()
    }

    override suspend fun getActiveWorkout(): Workout? {
        return workoutDao.getActiveWorkout()?.toDomain()
    }

    override suspend fun startWorkout(routineId: Long?): Long {
        val workout = WorkoutEntity(
            routineId = routineId,
            startTime = System.currentTimeMillis()
        )
        return workoutDao.insertWorkout(workout)
    }

    override suspend fun endWorkout(workoutId: Long, notes: String?) {
        val workout = workoutDao.getWorkoutById(workoutId) ?: return
        workoutDao.updateWorkout(
            workout.copy(
                endTime = System.currentTimeMillis(),
                notes = notes
            )
        )
    }

    override suspend fun deleteWorkout(workoutId: Long) {
        val workout = workoutDao.getWorkoutById(workoutId) ?: return
        workoutDao.deleteWorkout(workout)
    }

    override suspend fun addSet(
        workoutId: Long,
        exerciseId: Long,
        weight: Float,
        reps: Int,
        rpe: Int?
    ): Long {
        // Get current set count for this exercise in this workout
        val existingSets = workoutDao.getWorkoutById(workoutId) ?: return -1

        val setEntity = WorkoutSetEntity(
            workoutId = workoutId,
            exerciseId = exerciseId,
            setNumber = 1, // Will be calculated properly
            weight = weight,
            reps = reps,
            rpe = rpe
        )
        return workoutDao.insertWorkoutSet(setEntity)
    }

    override suspend fun updateSet(set: WorkoutSet) {
        workoutDao.updateWorkoutSet(set.toEntity())
    }

    override suspend fun deleteSet(setId: Long) {
        // Need to get the set first to delete it
        // For now, we'll create a minimal entity
        val entity = WorkoutSetEntity(
            id = setId,
            workoutId = 0,
            exerciseId = 0,
            setNumber = 0,
            weight = 0f,
            reps = 0
        )
        workoutDao.deleteWorkoutSet(entity)
    }

    override suspend fun getPersonalRecord(exerciseId: Long): Float? {
        return workoutDao.getPersonalRecord(exerciseId)
    }

    override suspend fun getWorkoutCountThisWeek(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return workoutDao.getWorkoutCountSince(calendar.timeInMillis)
    }

    // Mapper functions
    private fun WorkoutEntity.toDomain(): Workout = Workout(
        id = id,
        routineId = routineId,
        startTime = startTime,
        endTime = endTime,
        notes = notes
    )

    private fun com.gymtracker.data.local.database.WorkoutSetWithExercise.toDomain(): WorkoutSet = WorkoutSet(
        id = id,
        workoutId = workoutId,
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        setNumber = setNumber,
        weight = weight,
        reps = reps,
        rpe = rpe
    )

    private fun WorkoutSet.toEntity(): WorkoutSetEntity = WorkoutSetEntity(
        id = id,
        workoutId = workoutId,
        exerciseId = exerciseId,
        setNumber = setNumber,
        weight = weight,
        reps = reps,
        rpe = rpe
    )
}
