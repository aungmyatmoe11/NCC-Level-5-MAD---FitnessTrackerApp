package com.example.fitnesstrackingmobileapp.workout

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "users")
data class Users(
    @PrimaryKey val userId: Long,
    val username: String,
    val email: String,
    val goal: Double
)
@Entity(tableName = "workout_activities")
data class WorkOutActivities(
    @PrimaryKey val actId: Long,
    val userId: Long,
    val act_name:String,
    val metric1: Double,
    val metric2: Double
)
data class UserWithActivities(
    @Embedded val user: Users,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val workout_activities: List<WorkOutActivities>
)