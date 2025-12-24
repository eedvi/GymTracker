package com.gymtracker.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Exercise(
    val id: Long = 0,
    val name: String,
    val category: ExerciseCategory,
    val muscleGroups: List<String>,
    val equipment: Equipment,
    val isCustom: Boolean = false
) : Parcelable

enum class ExerciseCategory(val displayName: String) {
    PUSH("Push"),
    PULL("Pull"),
    LEGS("Legs"),
    CORE("Core"),
    CARDIO("Cardio")
}

enum class Equipment(val displayName: String) {
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    MACHINE("Machine"),
    CABLE("Cable"),
    BODYWEIGHT("Bodyweight"),
    KETTLEBELL("Kettlebell"),
    BANDS("Resistance Bands"),
    OTHER("Other")
}
