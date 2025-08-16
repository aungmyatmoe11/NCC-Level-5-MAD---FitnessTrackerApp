package com.example.fitnesstrackingmobileapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_table")
class LocationData(
        @ColumnInfo(name = "latitude") val latitude: Double,
        @ColumnInfo(name = "longitude") val longitude: Double,
        @ColumnInfo(name = "timestamp") val timestamp: Long
) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}
