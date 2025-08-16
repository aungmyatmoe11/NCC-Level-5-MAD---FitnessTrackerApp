package com.example.fitnesstrackingmobileapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
        entities =
                [
                        Exercise::class,
                        FitnessActivity::class,
                        WeightliftingSession::class,
                        ActivityGoal::class,
                        LocationData::class],
        version = 5,
        exportSchema = false
)
abstract class FitnessDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun fitnessActivityDao(): FitnessActivityDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile private var INSTANCE: FitnessDatabase? = null

        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        val instance =
                                Room.databaseBuilder(
                                                context.applicationContext,
                                                FitnessDatabase::class.java,
                                                "fitness_database"
                                        )
                                        .fallbackToDestructiveMigration()
                                        .build()
                        INSTANCE = instance
                        instance
                    }
        }
    }
}

// Type converters for Room
class Converters {
    // Add any type converters if needed for complex data types
    // For now, we don't need any converters
}
