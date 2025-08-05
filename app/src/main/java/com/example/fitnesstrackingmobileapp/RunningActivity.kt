package com.example.fitnesstrackingmobileapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackingmobileapp.data.Exercise
import com.example.fitnesstrackingmobileapp.data.ExerciseRepository
import java.text.SimpleDateFormat
import java.util.Date

class RunningActivity : AppCompatActivity(), SensorEventListener  {
    var running=false
    private var totalSteps = 0f
    private var sensorManager:SensorManager?=null
    lateinit var stepsValue: TextView
    lateinit var weight: EditText
    lateinit var save: ImageButton
    var startTime:Long=0
    var endTime:Long =0
    var  currentSteps =0
    private lateinit var repository : ExerciseRepository
    private var previousTotalSteps = 0f
    private lateinit var weight_kg:String
    lateinit var viewModal: ExerciseViewModal
    val _SensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        }
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_step_counter)
        stepsValue= findViewById(R.id.stepsValue)
        weight=findViewById(R.id.idweight)
        save=findViewById(R.id.idsave)
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(ExerciseViewModal::class.java)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
   //     weight_kg=weight.text.toString()
        loadData()
        resetSteps()

        // Adding a context of SENSOR_SERVICE as Sensor Manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        save.setOnClickListener(){
            saveData()
            val intent= Intent(this,HomeScreenActivity::class.java)
            startActivity(intent)
        }
    }

    private fun resetSteps() {
        var tv_stepsTaken = findViewById<TextView>(R.id.stepsValue)
        tv_stepsTaken.setOnClickListener {
            // This will give a toast message if the user want to reset the steps
            Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
            previousTotalSteps = totalSteps
            // When the user will click long tap on the screen,
            // the steps will be reset to 0
            tv_stepsTaken.text = 0.toString()

            // This will save the data

            true
        }
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        endTime=System.currentTimeMillis()
        var totalTime=(endTime-startTime)/1000*60
        val title:String ="Running"
        val metric_one:Double= weight.text.toString().toDouble()
        val metric_two:Double= currentSteps.toDouble()
        val calburned= ( 3.5* metric_two/metric_one) / 200
        val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
        val currentDateAndTime: String = sdf.format(Date())
        // if the string is not empty we are calling a
        // add note method to add data to our room database.
        viewModal.addNote(Exercise(title, metric_one, metric_two,calburned,currentDateAndTime))
        Toast.makeText(this, "Running Metric added", Toast.LENGTH_LONG).show()
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()

    }

    private fun loadData() {
        startTime=System.currentTimeMillis()
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)

        // Log.d is used for debugging purposes
        Log.d("StepCounter", "$savedNumber")

         previousTotalSteps = savedNumber
    }

    override fun onResume() {
        super.onResume()
        running = true

        // Returns the number of steps taken by the user since the last reboot while activated
        // This sensor requires permission android.permission.ACTIVITY_RECOGNITION.
        // So don't forget to add the following permission in AndroidManifest.xml present in manifest folder of the app.
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)


        if (stepSensor == null) {
            // This will give a toast message to the user if there is no sensor in the device
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            // Rate suitable for the user interface
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(_SensorEventListener)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        var tv_stepsTaken = findViewById<TextView>(R.id.stepsValue)

        if (running) {
            totalSteps = event!!.values[0]

            // Current steps are calculated by taking the difference of total steps
            // and previous steps
            currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()

            // It will show the current steps to the user
            tv_stepsTaken.text = ("$currentSteps")
        }
    }
}