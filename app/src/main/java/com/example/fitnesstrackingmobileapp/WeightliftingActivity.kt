package com.example.fitnesstrackingmobileapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstrackingmobileapp.data.FitnessActivity
import com.example.fitnesstrackingmobileapp.data.FitnessActivityDao
import com.example.fitnesstrackingmobileapp.data.FitnessDatabase
import com.example.fitnesstrackingmobileapp.data.WeightliftingSession
import com.example.fitnesstrackingmobileapp.utils.NetworkUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class WeightliftingActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WeightliftingActivity"
    }

    private lateinit var fitnessActivityDao: FitnessActivityDao
    private lateinit var exercisesAdapter: ExercisesAdapter
    private val exercisesList = mutableListOf<WeightliftingSession>()

    // UI Components
    private lateinit var exerciseNameInput: TextInputEditText
    private lateinit var setsInput: TextInputEditText
    private lateinit var repsInput: TextInputEditText
    private lateinit var weightInput: TextInputEditText
    private lateinit var addExerciseButton: MaterialButton
    private lateinit var saveWorkoutButton: MaterialButton
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var totalExercisesText: TextView
    private lateinit var totalVolumeText: TextView
    private lateinit var toolbar: MaterialToolbar

    // Tracking variables
    private var workoutStartTime = 0L
    private var totalVolume = 0.0
    private var totalCalories = 0.0
    private var userId: String = "default_user"
    private var isSaving = false // Prevent duplicate saves

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weightlifting)

        // Initialize database
        fitnessActivityDao = FitnessDatabase.getDatabase(this).fitnessActivityDao()

        // Get user ID from session
        userId = UserSession.getUserId(this)

        // Initialize UI
        initializeUI()

        // Start workout timer
        workoutStartTime = System.currentTimeMillis()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeUI() {
        // Initialize UI components
        exerciseNameInput = findViewById(R.id.exerciseNameInput)
        setsInput = findViewById(R.id.setsInput)
        repsInput = findViewById(R.id.repsInput)
        weightInput = findViewById(R.id.weightInput)
        addExerciseButton = findViewById(R.id.addExerciseButton)
        saveWorkoutButton = findViewById(R.id.saveWorkoutButton)
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView)
        totalExercisesText = findViewById(R.id.totalExercisesText)
        totalVolumeText = findViewById(R.id.totalVolumeText)
        toolbar = findViewById(R.id.toolbar)

        // Setup toolbar
        toolbar.setNavigationOnClickListener { finish() }

        // Setup add exercise button
        addExerciseButton.setOnClickListener { addExercise() }

        // Setup save workout button
        saveWorkoutButton.setOnClickListener { saveWorkout() }

        // Setup recycler view
        setupRecyclerView()

        // Update initial stats
        updateStats()
    }

    private fun setupRecyclerView() {
        exercisesAdapter = ExercisesAdapter { position -> removeExercise(position) }

        exercisesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WeightliftingActivity)
            adapter = exercisesAdapter
        }
    }

    private fun addExercise() {
        try {
            val exerciseName = exerciseNameInput.text.toString().trim()
            val setsStr = setsInput.text.toString().trim()
            val repsStr = repsInput.text.toString().trim()
            val weightStr = weightInput.text.toString().trim()

            Log.d(
                    TAG,
                    "Raw input values - Name: '$exerciseName', Sets: '$setsStr', Reps: '$repsStr', Weight: '$weightStr'"
            )

            if (exerciseName.isEmpty() ||
                            setsStr.isEmpty() ||
                            repsStr.isEmpty() ||
                            weightStr.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return
            }

            val sets = setsStr.toInt()
            val reps = repsStr.toInt()
            val weight = weightStr.toDouble()

            if (sets <= 0 || reps <= 0 || weight <= 0) {
                Toast.makeText(
                                this,
                                "Please enter valid values (greater than 0)",
                                Toast.LENGTH_SHORT
                        )
                        .show()
                return
            }

            val exercise =
                    WeightliftingSession(
                            activityId = 0, // Will be set when saving to database
                            exerciseName = exerciseName,
                            sets = sets,
                            reps = reps,
                            weightKg = weight
                    )

            Log.d(
                    TAG,
                    "Created exercise: ${exercise.exerciseName} - ${exercise.sets} sets, ${exercise.reps} reps, ${exercise.weightKg}kg"
            )

            exercisesList.add(exercise)
            exercisesAdapter.submitList(exercisesList.toList())
            updateStats()

            // Clear inputs
            exerciseNameInput.text?.clear()
            setsInput.text?.clear()
            repsInput.text?.clear()
            weightInput.text?.clear()

            Log.d(TAG, "Added exercise: $exerciseName")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding exercise: ${e.message}")
        }
    }

    private fun removeExercise(position: Int) {
        if (position in 0 until exercisesList.size) {
            exercisesList.removeAt(position)
            exercisesAdapter.submitList(exercisesList.toList())
            updateStats()
            Log.d(TAG, "Removed exercise at position: $position")
        }
    }

    private fun updateStats() {
        totalExercisesText.text = "${exercisesList.size} exercises"

        totalVolume = exercisesList.sumOf { it.sets * it.reps * it.weightKg }
        totalVolumeText.text = "%.1f kg".format(totalVolume)

        // Calculate calories (simplified formula)
        val duration = (System.currentTimeMillis() - workoutStartTime) / 1000.0 / 3600.0 // hours
        totalCalories = (70.0 * 6.0 * duration) // MET value for weightlifting
    }

    private fun saveWorkout() {
        if (exercisesList.isEmpty()) {
            Toast.makeText(this, "Please add at least one exercise", Toast.LENGTH_SHORT).show()
            return
        }

        // Prevent duplicate saves
        if (isSaving) {
            Log.d(TAG, "Already saving workout, ignoring duplicate request")
            return
        }

        isSaving = true
        Log.d(TAG, "Starting to save weightlifting workout")

        lifecycleScope.launch {
            try {
                val duration = (System.currentTimeMillis() - workoutStartTime) / 1000

                val activity =
                        FitnessActivity(
                                userId = userId,
                                activityType = "WEIGHTLIFTING",
                                title =
                                        "Weightlifting - ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())}",
                                startTime = workoutStartTime,
                                endTime = System.currentTimeMillis(),
                                durationSeconds = duration,
                                caloriesBurned = totalCalories,
                                averageSpeed =
                                        exercisesList.size.toDouble(), // exercises per session
                                notes = "Total volume: ${totalVolume}kg"
                        )

                val loadingDialog =
                        AlertDialog.Builder(this@WeightliftingActivity)
                                .setView(R.layout.loading_dialog)
                                .setCancelable(false)
                                .create()
                loadingDialog.show()

                if (!NetworkUtils.isNetworkAvailable(this@WeightliftingActivity)) {

                    val activityId = fitnessActivityDao.insertActivity(activity)

                    // Save individual exercises
                    exercisesList.forEach { exercise ->
                        val sessionWithActivityId = exercise.copy(activityId = activityId.toInt())
                        fitnessActivityDao.insertWeightliftingSession(sessionWithActivityId)
                    }

                    // Save to server via API with loading dialog

                    Log.d(TAG, "Saving to server with ${exercisesList.size} exercises:")
                    exercisesList.forEach { exercise ->
                        Log.d(
                                TAG,
                                "  - ${exercise.exerciseName}: ${exercise.sets} sets, ${exercise.reps} reps, ${exercise.weightKg}kg"
                        )
                    }
                    loadingDialog.dismiss()
                } else {
                    // အင်တာနက်ရှိလျှင် server သို့သိမ်းဆည်းမည်။
                    saveActivityToServer(activity, exercisesList, loadingDialog)
                }

                Log.d(TAG, "Saved weightlifting workout: ${activity.title}")

                // Finish activity
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving weightlifting workout: ${e.message}")
                isSaving = false // Reset flag on error
            }
        }
    }

    private fun saveActivityToServer(
            activity: FitnessActivity,
            exercises: List<WeightliftingSession>,
            loadingDialog: AlertDialog
    ) {
        try {
            val apiService = ApiService(this)
            apiService.saveActivity(
                    activity = activity,
                    exercises = exercises,
                    onSuccess = { message ->
                        Log.d(TAG, "Activity saved to server: $message")
                        loadingDialog.dismiss()

                        // Show success message
                        Toast.makeText(
                                        this@WeightliftingActivity,
                                        "Activity saved successfully!",
                                        Toast.LENGTH_SHORT
                                )
                                .show()

                        // Navigate back to home screen
                        val intent =
                                Intent(this@WeightliftingActivity, HomeScreenActivity::class.java)
                        intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    },
                    onError = { error ->
                        Log.e(TAG, "Error saving to server: $error")
                        loadingDialog.dismiss()
                        isSaving = false // Reset flag on error

                        // Show more specific error message
                        val errorMessage =
                                when {
                                    error.contains("Network error") ->
                                            "Network connection failed. Activity saved locally."
                                    error.contains("null") ->
                                            "Server connection failed. Activity saved locally."
                                    else -> "Server sync failed: $error"
                                }

                        Toast.makeText(this@WeightliftingActivity, errorMessage, Toast.LENGTH_LONG)
                                .show()
                    }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception in saveActivityToServer: ${e.message}")
            loadingDialog.dismiss()
            isSaving = false // Reset flag on error
            Toast.makeText(
                            this@WeightliftingActivity,
                            "Error saving activity: ${e.message}",
                            Toast.LENGTH_LONG
                    )
                    .show()
        }
    }
}

// Adapter for exercises list
class ExercisesAdapter(private val onRemoveClick: (Int) -> Unit) :
        androidx.recyclerview.widget.ListAdapter<WeightliftingSession, ExercisesAdapter.ViewHolder>(
                ExerciseDiffCallback()
        ) {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view =
                android.view.LayoutInflater.from(parent.context)
                        .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ViewHolder(itemView: android.view.View) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(exercise: WeightliftingSession, position: Int) {
            text1.text = exercise.exerciseName
            text2.text = "${exercise.sets} sets × ${exercise.reps} reps @ ${exercise.weightKg}kg"

            itemView.setOnLongClickListener {
                onRemoveClick(position)
                true
            }
        }
    }
}

class ExerciseDiffCallback :
        androidx.recyclerview.widget.DiffUtil.ItemCallback<WeightliftingSession>() {
    override fun areItemsTheSame(
            oldItem: WeightliftingSession,
            newItem: WeightliftingSession
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
            oldItem: WeightliftingSession,
            newItem: WeightliftingSession
    ): Boolean {
        return oldItem == newItem
    }
}
