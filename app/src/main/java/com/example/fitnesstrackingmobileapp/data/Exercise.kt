package com.example.fitnesstrackingmobileapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exerciseTable")
class Exercise(
        @ColumnInfo(name = "title") val activity_Name: String,
        @ColumnInfo(name = "metric1") val metric_one: Double,
        @ColumnInfo(name = "metric2") val metric_two: Double,
        @ColumnInfo(name = "calburned") val cal_burned: Double,
        @ColumnInfo(name = "timestamp") val timeStamp: String
) {
    // on below line we are specifying our key and
    // then auto generate as true and we are
    // specifying its initial value as 0
    @PrimaryKey(autoGenerate = true) var id = 0
}
