package com.example.fitnesstrackingmobileapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note : Exercise)

    @Delete
    suspend fun delete(note:Exercise)

    @Query("Select * from exerciseTable order by id ASC")
     fun  getAllExercises(): LiveData<List<Exercise>>

    @Update
    suspend fun update(note: Exercise)

  //  @Query("SELECT SUM(calburned) AS total_calburned FROM notesTable")
    //suspend fun  totalCal():Double
    @Insert
     suspend fun insertLocation(locationData: LocationData)

    @Query("SELECT * FROM location_table ORDER BY timestamp ASC")
    fun getAllLocations(): LiveData<List<LocationData>> // Use LiveData to observe changes

}