package com.gymtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,        // Push, Pull, Legs, Core, Cardio
    val muscleGroups: String,    // Comma-separated: "Chest,Triceps"
    val equipment: String,       // Barbell, Dumbbell, Machine, Bodyweight, Cable
    val isCustom: Boolean = false
)
