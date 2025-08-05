package com.example.fitnesstrackingmobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackingmobileapp.data.Exercise
import java.text.SimpleDateFormat
import java.util.*

class AddEditExecActivity : AppCompatActivity() {
    // on below line we are creating
    // variables for our UI components.
    lateinit var noteTitleEdt: EditText
    lateinit var noteEdt: EditText
    lateinit var noteEdt1: EditText
    lateinit var totalCal: EditText
    var calburned: Double = 0.0

    lateinit var saveBtn: Button

    // on below line we are creating variable for
    // viewmodal and integer for our note id.
    lateinit var viewModal: ExerciseViewModal
    var noteID = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        // on below line we are initializing our view modal.
        viewModal =
                ViewModelProvider(
                                this,
                                ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                        )
                        .get(ExerciseViewModal::class.java)

        // on below line we are initializing all our variables.
        noteTitleEdt = findViewById(R.id.idActivityName)
        noteEdt = findViewById(R.id.idmetric1)
        noteEdt1 = findViewById(R.id.idmetric2)

        saveBtn = findViewById(R.id.idBtn)

        // on below line we are getting data passed via an intent.
        val noteType = intent.getStringExtra("noteType")
        if (noteType.equals("Edit")) {
            // on below line we are setting data to edit text.
            val noteTitle = intent.getStringExtra("activity_name")
            val metric1 = intent.getStringExtra("metric_one")
            val metric2 = intent.getStringExtra("metric_two")
            noteID = intent.getIntExtra("noteId", -1)
            saveBtn.setText("Update activity")
            noteTitleEdt.setText(noteTitle)
            noteEdt.setText(metric1)
            noteEdt1.setText(metric2)
        } else {
            saveBtn.setText("Save Activity")
        }

        // on below line we are adding
        // click listener to our save button.
        saveBtn.setOnClickListener {
            // on below line we are getting
            // title and desc from edit text.
            val noteTitle = noteTitleEdt.text.toString()
            val noteDescription = noteEdt.text.toString()
            val metric1 = noteEdt.text.toString().toDouble()
            val metric2 = noteEdt1.text.toString().toDouble()
            calburned =
                    noteEdt.text.toString().toDouble() * noteEdt1.text.toString().toDouble() * 0.3
            // on below line we are checking the type
            // and then saving or updating the data.
            if (noteType.equals("Edit")) {
                if (noteTitle.isNotEmpty() && noteDescription.isNotEmpty()) {
                    val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
                    val currentDateAndTime: String = sdf.format(Date())
                    val updatedNote =
                            Exercise(noteTitle, metric1, metric2, calburned, currentDateAndTime)
                    updatedNote.id = noteID
                    viewModal.updateNote(updatedNote)
                    Toast.makeText(this, "Note Updated..", Toast.LENGTH_LONG).show()
                }
            } else {
                if (noteTitle.isNotEmpty() && noteDescription.isNotEmpty()) {
                    val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
                    val currentDateAndTime: String = sdf.format(Date())
                    // if the string is not empty we are calling a
                    // add note method to add data to our room database.
                    viewModal.addNote(
                            Exercise(noteTitle, metric1, metric2, calburned, currentDateAndTime)
                    )
                    Toast.makeText(this, "$noteTitle Added", Toast.LENGTH_LONG).show()
                }
            }
            // opening the new activity on below line
            startActivity(Intent(applicationContext, ExerciseActivity::class.java))
            this.finish()
        }
    }
}
