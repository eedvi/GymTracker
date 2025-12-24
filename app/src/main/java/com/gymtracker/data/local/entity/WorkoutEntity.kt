package com.gymtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("routineId")]
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long? = null,  // Optional link to routine
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val notes: String? = null
)
