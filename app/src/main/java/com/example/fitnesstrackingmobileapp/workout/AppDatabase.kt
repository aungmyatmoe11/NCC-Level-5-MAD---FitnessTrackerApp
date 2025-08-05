package com.example.fitnesstrackingmobileapp.workout

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlin.concurrent.Volatile

@Database(entities = [WorkOut::class], exportSchema = true, version = 1)
abstract class AppDatabase :  RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, applicationScope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_table"
                )
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }



}