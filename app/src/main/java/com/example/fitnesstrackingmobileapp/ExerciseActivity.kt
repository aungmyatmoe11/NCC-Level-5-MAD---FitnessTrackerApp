package com.example.fitnesstrackingmobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstrackingmobileapp.data.Exercise
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExerciseActivity : AppCompatActivity(), NoteClickInterface, NoteClickDeleteInterface {

    // on below line we are creating a variable
    // for our recycler view, exit text, button and viewmodel.
    lateinit var viewModal: ExerciseViewModal
    lateinit var notesRV: RecyclerView
    lateinit var addFAB: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        // on below line we are initializing
        // all our variables.
        notesRV = findViewById(R.id.notesRV)
        addFAB = findViewById(R.id.idFAB)

        // on below line we are setting layout
        // manager to our recycler view.
        notesRV.layoutManager = LinearLayoutManager(this)

        // on below line we are initializing our adapter class.
        val noteRVAdapter = ExerciseRVAdapter(this, this, this)

        // on below line we are setting
        // adapter to our recycler view.
        notesRV.adapter = noteRVAdapter

        // on below line we are
        // initializing our view modal.
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(ExerciseViewModal::class.java)

        // on below line we are calling all notes method
        // from our view modal class to observer the changes on list.
        viewModal.allNotes.observe(this, Observer { list ->
            list?.let {
                // on below line we are updating our list.
                noteRVAdapter.updateList(it)
            }
        })
        addFAB.setOnClickListener {
            // adding a click listener for fab button
            // and opening a new intent to add a new note.
            val intent = Intent(this@ExerciseActivity, AddEditExecActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }

    override fun onNoteClick(exer: Exercise) {
        // opening a new intent and passing a data to it.
        val intent = Intent(this@ExerciseActivity, AddEditExecActivity::class.java)

        intent.putExtra("noteType", "Edit")
        intent.putExtra("noteTitle", exer.activity_Name)
        intent.putExtra("Metric1", exer.metric_one)
        intent.putExtra("Metric2", exer.metric_two)
        intent.putExtra("totalCal", exer.cal_burned)
        intent.putExtra("noteId", exer.id)
        startActivity(intent)
        this.finish()
    }

    override fun onDeleteIconClick(note: Exercise) {
        // in on note click method we are calling delete
        // method from our view modal to delete our not.
        viewModal.deleteNote(note)
        // displaying a toast message
        Toast.makeText(this, "${note.activity_Name} Deleted", Toast.LENGTH_LONG).show()
    }
}
