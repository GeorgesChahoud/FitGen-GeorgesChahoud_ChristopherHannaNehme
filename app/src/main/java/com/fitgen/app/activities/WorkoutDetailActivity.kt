package com.fitgen.app.activities

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fitgen.app.R
import com.fitgen.app.models.Workout
import com.fitgen.app.utils.Constants
import com.fitgen.app.viewmodels.WorkoutViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar

class WorkoutDetailActivity : AppCompatActivity() {

    private val workoutViewModel: WorkoutViewModel by viewModels()
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var workoutNameTextView: TextView
    private lateinit var durationValueTextView: TextView
    private lateinit var caloriesValueTextView: TextView
    private lateinit var difficultyTextView: TextView
    private lateinit var setsValueTextView: TextView
    private lateinit var repsValueTextView: TextView
    private lateinit var instructionsTextView: TextView
    
    private var currentWorkout: Workout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_detail)

        initViews()
        setupToolbar()
        loadWorkout()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        workoutNameTextView = findViewById(R.id.workoutNameTextView)
        durationValueTextView = findViewById(R.id.durationValueTextView)
        caloriesValueTextView = findViewById(R.id.caloriesValueTextView)
        difficultyTextView = findViewById(R.id.difficultyTextView)
        setsValueTextView = findViewById(R.id.setsValueTextView)
        repsValueTextView = findViewById(R.id.repsValueTextView)
        instructionsTextView = findViewById(R.id.instructionsTextView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadWorkout() {
        val workoutId = intent.getStringExtra("workout_id") ?: return
        val workout = workoutViewModel.getWorkoutById(workoutId)
        
        if (workout == null) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Workout not found",
                Snackbar.LENGTH_SHORT
            ).show()
            finish()
            return
        }
        
        currentWorkout = workout
        displayWorkout(workout)
    }

    private fun displayWorkout(workout: Workout) {
        toolbar.title = getString(R.string.workout_details)
        workoutNameTextView.text = workout.name
        durationValueTextView.text = workout.duration.toString()
        caloriesValueTextView.text = workout.caloriesBurn.toString()
        setsValueTextView.text = workout.sets.toString()
        repsValueTextView.text = workout.reps
        instructionsTextView.text = workout.instructions
        
        // Set difficulty text
        difficultyTextView.text = when (workout.difficulty) {
            Constants.DIFFICULTY_BEGINNER -> getString(R.string.beginner)
            Constants.DIFFICULTY_INTERMEDIATE -> getString(R.string.intermediate)
            Constants.DIFFICULTY_ADVANCED -> getString(R.string.advanced)
            else -> workout.difficulty
        }
        
        // Set difficulty background drawable
        val difficultyBackground = when (workout.difficulty) {
            Constants.DIFFICULTY_BEGINNER -> R.drawable.bg_difficulty_beginner
            Constants.DIFFICULTY_INTERMEDIATE -> R.drawable.bg_difficulty_intermediate
            Constants.DIFFICULTY_ADVANCED -> R.drawable.bg_difficulty_advanced
            else -> R.drawable.bg_difficulty_default
        }
        difficultyTextView.setBackgroundResource(difficultyBackground)
    }
}
