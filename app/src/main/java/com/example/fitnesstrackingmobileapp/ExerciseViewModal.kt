package com.example.fitnesstrackingmobileapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import androidx.lifecycle.LiveData
import com.example.fitnesstrackingmobileapp.data.Exercise
import com.example.fitnesstrackingmobileapp.data.ExerciseDatabase

import com.example.fitnesstrackingmobileapp.data.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseViewModal (application: Application) : AndroidViewModel(application) {
    val allNotes : LiveData<List<Exercise>>
    var totalCal: Double = 0.0
    val repository : ExerciseRepository

    init {
        val dao = ExerciseDatabase.getDatabase(application).getNotesDao()
        repository = ExerciseRepository(dao)
        allNotes = repository.allExecs

    }

    // on below line we are creating a new method for deleting a note. In this we are
    // calling a delete method from our repository to delete our note.
    fun deleteNote (note: Exercise) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(note)
    }

    // on below line we are creating a new method for updating a note. In this we are
    // calling a update method from our repository to update our note.
    fun updateNote(note: Exercise) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(note)
    }
    // we are calling a method from our repository to add a new note.
    fun addNote(note: Exercise) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(note)
    }
}