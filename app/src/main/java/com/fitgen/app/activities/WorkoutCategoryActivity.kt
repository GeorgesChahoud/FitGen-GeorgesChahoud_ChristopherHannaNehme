package com.fitgen.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitgen.app.R
import com.fitgen.app.adapters.WorkoutAdapter
import com.fitgen.app.viewmodels.WorkoutViewModel
import com.google.android.material.appbar.MaterialToolbar

class WorkoutCategoryActivity : AppCompatActivity() {

    private val workoutViewModel: WorkoutViewModel by viewModels()
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var workoutsRecyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    
    private val workoutAdapter = WorkoutAdapter { workout ->
        val intent = Intent(this, WorkoutDetailActivity::class.java)
        intent.putExtra("workout_id", workout.id)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_category)

        initViews()
        setupToolbar()
        setupRecyclerView()
        loadWorkouts()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        workoutsRecyclerView = findViewById(R.id.workoutsRecyclerView)
        emptyTextView = findViewById(R.id.emptyTextView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        workoutsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WorkoutCategoryActivity)
            adapter = workoutAdapter
        }
    }

    private fun loadWorkouts() {
        val category = intent.getStringExtra("category") ?: return
        
        // Set toolbar title based on category
        toolbar.title = when (category) {
            "abs" -> getString(R.string.abs)
            "biceps" -> getString(R.string.biceps)
            "chest" -> getString(R.string.chest)
            "legs" -> getString(R.string.legs)
            "shoulders" -> getString(R.string.shoulders)
            "back" -> getString(R.string.back)
            "triceps" -> getString(R.string.triceps)
            "cardio" -> getString(R.string.cardio)
            else -> category
        }
        
        val workouts = workoutViewModel.getWorkoutsByCategory(category)
        
        if (workouts.isEmpty()) {
            workoutsRecyclerView.visibility = View.GONE
            emptyTextView.visibility = View.VISIBLE
            emptyTextView.text = "No workouts available for this category"
        } else {
            workoutsRecyclerView.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
            workoutAdapter.submitList(workouts)
        }
    }
}
