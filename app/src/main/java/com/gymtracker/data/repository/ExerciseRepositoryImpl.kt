package com.gymtracker.data.repository

import com.gymtracker.data.local.database.ExerciseDao
import com.gymtracker.data.local.entity.ExerciseEntity
import com.gymtracker.domain.model.Equipment
import com.gymtracker.domain.model.Exercise
import com.gymtracker.domain.model.ExerciseCategory
import com.gymtracker.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExercisesByCategory(category: ExerciseCategory): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByCategory(category.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseDao.searchExercises(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getExerciseById(id: Long): Exercise? {
        return exerciseDao.getExerciseById(id)?.toDomain()
    }

    override suspend fun insertExercise(exercise: Exercise): Long {
        return exerciseDao.insertExercise(exercise.toEntity())
    }

    override suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.updateExercise(exercise.toEntity())
    }

    override suspend fun deleteExercise(exercise: Exercise) {
        exerciseDao.deleteExercise(exercise.toEntity())
    }

    override suspend fun initializeDefaultExercises() {
        if (exerciseDao.getExerciseCount() == 0) {
            exerciseDao.insertExercises(getDefaultExercises())
        }
    }

    private fun getDefaultExercises(): List<ExerciseEntity> = listOf(
        // PUSH exercises
        ExerciseEntity(name = "Bench Press", category = "PUSH", muscleGroups = "Chest,Triceps,Shoulders", equipment = "BARBELL"),
        ExerciseEntity(name = "Incline Bench Press", category = "PUSH", muscleGroups = "Upper Chest,Triceps,Shoulders", equipment = "BARBELL"),
        ExerciseEntity(name = "Dumbbell Bench Press", category = "PUSH", muscleGroups = "Chest,Triceps,Shoulders", equipment = "DUMBBELL"),
        ExerciseEntity(name = "Overhead Press", category = "PUSH", muscleGroups = "Shoulders,Triceps", equipment = "BARBELL"),
        ExerciseEntity(name = "Dumbbell Shoulder Press", category = "PUSH", muscleGroups = "Shoulders,Triceps", equipment = "DUMBBELL"),
        ExerciseEntity(name = "Push-ups", category = "PUSH", muscleGroups = "Chest,Triceps,Shoulders", equipment = "BODYWEIGHT"),
        ExerciseEntity(name = "Dips", category = "PUSH", muscleGroups = "Chest,Triceps,Shoulders", equipment = "BODYWEIGHT"),
        ExerciseEntity(name = "Tricep Pushdown", category = "PUSH", muscleGroups = "Triceps", equipment = "CABLE"),
        ExerciseEntity(name = "Lateral Raises", category = "PUSH", muscleGroups = "Shoulders", equipment = "DUMBBELL"),
        ExerciseEntity(name = "Chest Fly", category = "PUSH", muscleGroups = "Chest", equipment = "DUMBBELL"),
        ExerciseEntity(name = "Cable Chest Fly", category = "PUSH", muscleGroups = "Chest", equipment = "CABLE"),

        // PULL exercises
        ExerciseEntity(name = "Deadlift", category = "PULL", muscleGroups = "Back,Hamstrings,Glutes", equipment = "BARBELL"),
        ExerciseEntity(name = "Barbell Row", category = "PULL", muscleGroups = "Back,Biceps", equipment = "BARBELL"),
        ExerciseEntity(name = "Dumbbell Row", category = "PULL", muscleGroups = "Back,Biceps", equipment = "DUMBBELL"),
        ExerciseEntity(name = "Pull-ups", category = "PULL", muscleGroups = "Back,Biceps", equipment = "BODYWEIGHT"),
        ExerciseEntity(name = "Chin-ups", category = "PULL", muscleGroups = "Back,Biceps", equipment = "BODYWEIGHT"),
        ExerciseEntity(name = "Lat Pulldown", category = "PULL", muscleGroups = "Back,Biceps", equipment = "CABLE"),
        ExerciseEntity(name = "Seated Cable Row", category = "PULL", muscleGroups = "Back,Biceps", equipment = "CABLE"),
        ExerciseEntity(name = "Face Pulls", category = "PULL", muscleGroups = "Rear Delts,Upper Back", equipment = "CABLE"),
        ExerciseEntity(name = "Barbell Curl", category = "PULL", muscleGroups = "Biceps", equipment = "BARBELL"),
        ExerciseEntity(name = "Dumbbell Curl", category = "PULL", muscleGroups = "Biceps", equipment = "DUMBBELL"),
        ExerciseEntity(name = "Hammer Curl", category = "PULL", muscleGroups = "Biceps,Forearms", equipment = "DUMBBELL"),

        // LEGS exercises
        ExerciseEntity(name = "Squat", category = "LEGS", muscleGroups = "Quads,Glutes,Hamstrings", equipment = "BARBELL"),
        ExerciseEntity(name = "Front Squat", category = "LEGS", muscleGroups = "Quads,Glutes,Core", equipment = "BARBELL"),
        ExerciseEntity(name = "Leg Press", category = "LEGS", muscleGroups = "Quads,Glutes,Hamstrings", equipment = "MACHINE"),
        ExerciseEntity(name = "Romanian Deadlift", category = "LEGS", muscleGroups = "Hamstrings,Glutes,Lower Back", equipment = "BARBELL"),
        ExerciseEntity(name = "Lunges", category = "LEGS", muscleGroups = "Quads,Glutes,Hamstrings", equipment = "DUMBBELL"),
        ExerciseEntity(name = "Bulgarian Split Squat", category = "LEGS", muscleGroups = "Quads,Glutes", equipment = "DUMBBELL"),
        ExerciseEntity(name = "Leg Extension", category = "LEGS", muscleGroups = "Quads", equipment = "MACHINE"),
        ExerciseEntity(name = "Leg Curl", category = "LEGS", muscleGroups = "Hamstrings", equipment = "MACHINE"),
        ExerciseEntity(name = "Calf Raises", category = "LEGS", muscleGroups = "Calves", equipment = "MACHINE"),
        ExerciseEntity(name = "Hip Thrust", category = "LEGS", muscleGroups = "Glutes,Hamstrings", equipment = "BARBELL"),

        // CORE exercises
        ExerciseEntity(name = "Plank", category = "CORE", muscleGroups = "Core", equipment = "BODYWEIGHT"),
        ExerciseEntity(name = "Crunches", category = "CORE", muscleGroups = "Abs", equipment = "BODYWEIGHT"),
        ExerciseEntity(name = "Leg Raises", category = "CORE", muscleGroups = "Lower Abs", equipment = "BODYWEIGHT"),
        ExerciseEntity(name = "Russian Twist", category = "CORE", muscleGroups = "Obliques", equipment = "BODYWEIGHT"),
        ExerciseEntity(name = "Cable Woodchop", category = "CORE", muscleGroups = "Obliques,Core", equipment = "CABLE"),
        ExerciseEntity(name = "Ab Rollout", category = "CORE", muscleGroups = "Core", equipment = "OTHER"),
        ExerciseEntity(name = "Dead Bug", category = "CORE", muscleGroups = "Core", equipment = "BODYWEIGHT"),
        ExerciseEntity(name = "Mountain Climbers", category = "CORE", muscleGroups = "Core,Shoulders", equipment = "BODYWEIGHT"),

        // CARDIO exercises
        ExerciseEntity(name = "Treadmill Running", category = "CARDIO", muscleGroups = "Legs,Cardio", equipment = "MACHINE"),
        ExerciseEntity(name = "Cycling", category = "CARDIO", muscleGroups = "Legs,Cardio", equipment = "MACHINE"),
        ExerciseEntity(name = "Rowing Machine", category = "CARDIO", muscleGroups = "Full Body,Cardio", equipment = "MACHINE"),
        ExerciseEntity(name = "Stair Climber", category = "CARDIO", muscleGroups = "Legs,Cardio", equipment = "MACHINE"),
        ExerciseEntity(name = "Jump Rope", category = "CARDIO", muscleGroups = "Full Body,Cardio", equipment = "OTHER"),
        ExerciseEntity(name = "Burpees", category = "CARDIO", muscleGroups = "Full Body,Cardio", equipment = "BODYWEIGHT")
    )

    // Mapper functions
    private fun ExerciseEntity.toDomain(): Exercise = Exercise(
        id = id,
        name = name,
        category = ExerciseCategory.valueOf(category),
        muscleGroups = muscleGroups.split(",").map { it.trim() },
        equipment = Equipment.valueOf(equipment),
        isCustom = isCustom
    )

    private fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
        id = id,
        name = name,
        category = category.name,
        muscleGroups = muscleGroups.joinToString(","),
        equipment = equipment.name,
        isCustom = isCustom
    )
}
