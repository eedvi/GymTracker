package com.gymtracker.di

import android.content.Context
import androidx.room.Room
import com.gymtracker.data.local.database.ExerciseDao
import com.gymtracker.data.local.database.GymTrackerDatabase
import com.gymtracker.data.local.database.RoutineDao
import com.gymtracker.data.local.database.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): GymTrackerDatabase {
        return Room.databaseBuilder(
            context,
            GymTrackerDatabase::class.java,
            "gym_tracker_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideExerciseDao(database: GymTrackerDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    @Provides
    @Singleton
    fun provideRoutineDao(database: GymTrackerDatabase): RoutineDao {
        return database.routineDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutDao(database: GymTrackerDatabase): WorkoutDao {
        return database.workoutDao()
    }
}
