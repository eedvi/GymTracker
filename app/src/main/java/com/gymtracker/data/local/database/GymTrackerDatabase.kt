package com.gymtracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gymtracker.data.local.entity.ExerciseEntity
import com.gymtracker.data.local.entity.RoutineEntity
import com.gymtracker.data.local.entity.RoutineExerciseEntity
import com.gymtracker.data.local.entity.WorkoutEntity
import com.gymtracker.data.local.entity.WorkoutSetEntity

@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutSetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GymTrackerDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutDao(): WorkoutDao
}
