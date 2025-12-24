package com.gymtracker.domain.model

data class Routine(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val exercises: List<RoutineExercise> = emptyList(),
    val exerciseCount: Int = exercises.size  // For list display when exercises aren't loaded
)

data class RoutineExercise(
    val exercise: Exercise,
    val orderIndex: Int,
    val targetSets: Int = 3,
    val targetReps: String = "8-12"
)
