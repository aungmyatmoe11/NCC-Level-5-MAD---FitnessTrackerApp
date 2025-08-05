package com.example.fitnesstrackingmobileapp.workout

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insert(workOut: WorkOut)
    @Delete
    suspend  fun delete(workOut: WorkOut)

    @Query("SELECT * FROM workout_table WHERE wid =:tid")
    suspend fun getWorkout(tid: String): WorkOut

    @Query("SELECT * FROM workout_table")
    fun getWorkouts(): LiveData<List<WorkOut>>


}