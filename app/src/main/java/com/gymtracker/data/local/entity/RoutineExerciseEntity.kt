package com.gymtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "routine_exercises",
    primaryKeys = ["routineId", "exerciseId", "orderIndex"],
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routineId"),
        Index("exerciseId")
    ]
)
data class RoutineExerciseEntity(
    val routineId: Long,
    val exerciseId: Long,
    val orderIndex: Int,         // Order in routine
    val targetSets: Int = 3,
    val targetReps: String = "8-12"  // e.g., "8-12", "5", "AMRAP"
)
