package com.example.fitnesstrackingmobileapp.data

import androidx.lifecycle.LiveData

class ExerciseRepository(private val execDao: ExerciseDao) {
    val allExecs: LiveData<List<Exercise>> = execDao.getAllExercises()
    //val totalCalBurned:Double = notesDao.totalCal()
    suspend fun insert(exec: Exercise) {
        execDao.insert(exec)
    }
    suspend fun delete(exec:Exercise){
        execDao.delete(exec)
    }
    suspend fun update(exec: Exercise){
        execDao.update(exec)
    }


}