package com.example.fitnesstrackingmobileapp.workout

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_table")
class WorkOut {
    @PrimaryKey(autoGenerate = true)
    public var id: Int = 0

    @ColumnInfo(name = "wid")
    public var workoutid: String = ""

    @ColumnInfo(name = "wname")
    public var  wname: String = ""

    @ColumnInfo(name = "metric1")
    public var  metric1: Float = 0.0f

    @ColumnInfo(name = "metric2")
    public var  metric2: Float = 0.0f

}