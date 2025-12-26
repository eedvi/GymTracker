package com.gymtracker.di

import com.gymtracker.data.repository.ExerciseRepositoryImpl
import com.gymtracker.data.repository.RoutineRepositoryImpl
import com.gymtracker.data.repository.SettingsRepositoryImpl
import com.gymtracker.data.repository.WorkoutRepositoryImpl
import com.gymtracker.domain.repository.ExerciseRepository
import com.gymtracker.domain.repository.RoutineRepository
import com.gymtracker.domain.repository.SettingsRepository
import com.gymtracker.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        exerciseRepositoryImpl: ExerciseRepositoryImpl
    ): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: WorkoutRepositoryImpl
    ): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        routineRepositoryImpl: RoutineRepositoryImpl
    ): RoutineRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
