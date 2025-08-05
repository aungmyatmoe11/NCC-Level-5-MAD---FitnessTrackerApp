package com.example.fitnesstrackingmobileapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities =arrayOf(Exercise::class,LocationData::class), version = 2, exportSchema = false)
//@Database(entities = [Note::class, LocationData::class], version = 1, exportSchema = false)

abstract class ExerciseDatabase():RoomDatabase() {
    abstract fun getNotesDao(): ExerciseDao

    companion object {
        // Singleton prevents multiple
        // instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null
        fun getDatabase(context: Context): ExerciseDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    "exercise_database"
                )//.addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

    /*   val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE Notes ADD COLUMN image STRING")
            }
        }*/
    }
}
