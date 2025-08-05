package com.example.fitnesstrackingmobileapp.workout

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [Users::class, WorkOutActivities::class], version = 1)
abstract class ActivityDatabase: RoomDatabase() {
    abstract fun activityDao(): ActivitiesDao
}