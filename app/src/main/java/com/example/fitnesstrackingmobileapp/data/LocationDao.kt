package com.example.fitnesstrackingmobileapp.data
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {

    @Insert
    suspend fun insert(locationData: LocationData)

    @Query("SELECT * FROM location_table ORDER BY timestamp ASC")
    fun getAllLocations(): LiveData<List<LocationData>> // Use LiveData to observe changes
}