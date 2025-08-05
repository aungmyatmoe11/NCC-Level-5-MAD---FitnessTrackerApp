package com.example.fitnesstrackingmobileapp.workout

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ActivitiesDao {
    @Transaction
    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUsersWithActivities(userId: Long): LiveData<List<UserWithActivities>>

    @Insert
    suspend fun insertUser(user: Users): Long

    @Insert
    suspend fun insertWorkout(workoutAct: WorkOutActivities): Long
}