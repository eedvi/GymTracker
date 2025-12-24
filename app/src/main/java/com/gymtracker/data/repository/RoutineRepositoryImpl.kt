package com.gymtracker.data.repository

import com.gymtracker.data.local.database.ExerciseDao
import com.gymtracker.data.local.database.RoutineDao
import com.gymtracker.data.local.database.RoutineWithExerciseCount
import com.gymtracker.data.local.entity.RoutineEntity
import com.gymtracker.data.local.entity.RoutineExerciseEntity
import com.gymtracker.domain.model.Equipment
import com.gymtracker.domain.model.Exercise
import com.gymtracker.domain.model.ExerciseCategory
import com.gymtracker.domain.model.Routine
import com.gymtracker.domain.model.RoutineExercise
import com.gymtracker.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao,
    private val exerciseDao: ExerciseDao
) : RoutineRepository {

    override fun getAllRoutines(): Flow<List<Routine>> {
        return routineDao.getAllRoutinesWithCount().map { routinesWithCount ->
            routinesWithCount.map { it.toDomain() }
        }
    }

    override suspend fun getRoutineById(id: Long): Routine? {
        return routineDao.getRoutineById(id)?.toDomain()
    }

    override suspend fun getRoutineWithExercises(id: Long): Routine? {
        val routine = routineDao.getRoutineById(id) ?: return null
        val routineExercises = routineDao.getRoutineExercises(id).first()

        val exercises = routineExercises.map { re ->
            val exercise = exerciseDao.getExerciseById(re.exerciseId)
            RoutineExercise(
                exercise = exercise?.let {
                    Exercise(
                        id = it.id,
                        name = it.name,
                        category = ExerciseCategory.valueOf(it.category),
                        muscleGroups = it.muscleGroups.split(",").map { m -> m.trim() },
                        equipment = Equipment.valueOf(it.equipment),
                        isCustom = it.isCustom
                    )
                } ?: Exercise(
                    id = re.exerciseId,
                    name = re.exerciseName,
                    category = ExerciseCategory.PUSH,
                    muscleGroups = emptyList(),
                    equipment = Equipment.OTHER
                ),
                orderIndex = re.orderIndex,
                targetSets = re.targetSets,
                targetReps = re.targetReps
            )
        }

        return routine.toDomain().copy(exercises = exercises)
    }

    override suspend fun createRoutine(name: String, description: String?): Long {
        val entity = RoutineEntity(
            name = name,
            description = description
        )
        return routineDao.insertRoutine(entity)
    }

    override suspend fun updateRoutine(routine: Routine) {
        routineDao.updateRoutine(
            RoutineEntity(
                id = routine.id,
                name = routine.name,
                description = routine.description,
                createdAt = routine.createdAt
            )
        )
    }

    override suspend fun deleteRoutine(routineId: Long) {
        val routine = routineDao.getRoutineById(routineId) ?: return
        routineDao.deleteRoutine(routine)
    }

    override suspend fun addExerciseToRoutine(
        routineId: Long,
        exerciseId: Long,
        orderIndex: Int,
        targetSets: Int,
        targetReps: String
    ) {
        routineDao.insertRoutineExercise(
            RoutineExerciseEntity(
                routineId = routineId,
                exerciseId = exerciseId,
                orderIndex = orderIndex,
                targetSets = targetSets,
                targetReps = targetReps
            )
        )
    }

    override suspend fun removeExerciseFromRoutine(routineId: Long, exerciseId: Long) {
        routineDao.deleteRoutineExercise(routineId, exerciseId)
    }

    override suspend fun updateRoutineExercises(routineId: Long, exercises: List<RoutineExercise>) {
        // Delete all existing exercises for this routine
        routineDao.deleteRoutineExercises(routineId)

        // Insert the new list
        val entities = exercises.mapIndexed { index, re ->
            RoutineExerciseEntity(
                routineId = routineId,
                exerciseId = re.exercise.id,
                orderIndex = index,
                targetSets = re.targetSets,
                targetReps = re.targetReps
            )
        }
        routineDao.insertRoutineExercises(entities)
    }

    private fun RoutineEntity.toDomain(): Routine = Routine(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt
    )

    private fun RoutineWithExerciseCount.toDomain(): Routine = Routine(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt,
        exerciseCount = exerciseCount
    )
}
