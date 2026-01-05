package com.fitgen.app.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitgen.app.R
import com.fitgen.app.activities.WorkoutDetailActivity
import com.fitgen.app.adapters.WeeklyPlanAdapter
import com.fitgen.app.repositories.WeeklyPlanRepository
import com.fitgen.app.utils.WorkoutPlanGenerator
import com.fitgen.app.viewmodels.UserViewModel
import com.fitgen.app.viewmodels.WorkoutViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class HomeFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()
    private val workoutViewModel: WorkoutViewModel by activityViewModels()
    private val weeklyPlanRepository = WeeklyPlanRepository()
    
    private lateinit var welcomeTextView: TextView
    private lateinit var userNameTextView: TextView
    private lateinit var calorieValueTextView: TextView
    private lateinit var bmrValueTextView: TextView
    private lateinit var bmiValueTextView: TextView
    private lateinit var weeklyPlanRecyclerView: RecyclerView
    private lateinit var profileImageView: com.google.android.material.imageview.ShapeableImageView
    
    private val weeklyPlanAdapter = WeeklyPlanAdapter { workout ->
        val intent = Intent(requireContext(), WorkoutDetailActivity::class.java)
        intent.putExtra("workout_id", workout.id)
        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        observeViewModels()
    }

    private fun initViews(view: View) {
        welcomeTextView = view.findViewById(R.id.welcomeTextView) ?: run {
            Log.e("HomeFragment", "welcomeTextView not found in layout")
            return
        }
        userNameTextView = view.findViewById(R.id.userNameTextView) ?: run {
            Log.e("HomeFragment", "userNameTextView not found in layout")
            return
        }
        calorieValueTextView = view.findViewById(R.id.calorieValueTextView) ?: run {
            Log.e("HomeFragment", "calorieValueTextView not found in layout")
            return
        }
        bmrValueTextView = view.findViewById(R.id.bmrValueTextView) ?: run {
            Log.e("HomeFragment", "bmrValueTextView not found in layout")
            return
        }
        bmiValueTextView = view.findViewById(R.id.bmiValueTextView) ?: run {
            Log.e("HomeFragment", "bmiValueTextView not found in layout")
            return
        }
        weeklyPlanRecyclerView = view.findViewById(R.id.weeklyPlanRecyclerView) ?: run {
            Log.e("HomeFragment", "weeklyPlanRecyclerView not found in layout")
            return
        }
        profileImageView = view.findViewById(R.id.profileImageView) ?: run {
            Log.e("HomeFragment", "profileImageView not found in layout")
            return
        }
    }

    private fun setupRecyclerView() {
        weeklyPlanRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = weeklyPlanAdapter
        }
    }

    private fun observeViewModels() {
        // Observe user data
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                // Use the actual username from user profile
                val displayName = it.username.ifEmpty { 
                    // Fallback to email if username is empty (shouldn't happen)
                    it.email.substringBefore("@")
                }.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
                userNameTextView.text = displayName
            }
        }

        // Observe daily calorie target
        userViewModel.dailyCalorieTarget.observe(viewLifecycleOwner) { calories ->
            calorieValueTextView.text = calories.toString()
        }

        // Observe BMR
        userViewModel.bmr.observe(viewLifecycleOwner) { bmr ->
            bmrValueTextView.text = bmr.toInt().toString()
        }

        // Observe BMI
        userViewModel.bmi.observe(viewLifecycleOwner) { bmi ->
            bmiValueTextView.text = String.format("%.1f", bmi)
        }

        // Generate and display weekly workout plan
        setupWeeklyPlan()
    }
    
    private fun setupWeeklyPlan() {
        // Track if we've already loaded to prevent multiple triggers
        var hasLoadedPlan = false
        
        // Observe both user and workouts
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                workoutViewModel.allWorkouts.observe(viewLifecycleOwner) { allWorkouts ->
                    // Only load once when both user and workouts are available
                    if (allWorkouts.isNotEmpty() && !hasLoadedPlan) {
                        hasLoadedPlan = true
                        
                        viewLifecycleOwner.lifecycleScope.launch {
                            // Try to load existing plan from Firestore
                            val weeklyPlan = weeklyPlanRepository.getCurrentWeekPlan(user.uid)
                            
                            if (weeklyPlan == null) {
                                // No plan exists - generate new one
                                Log.d("HomeFragment", "No existing plan, generating new one")
                                val newPlan = WorkoutPlanGenerator.generateWeeklyPlan(user, allWorkouts)
                                
                                // Save to Firestore
                                weeklyPlanRepository.saveWeeklyPlan(newPlan)
                                
                                // Display immediately
                                weeklyPlanAdapter.submitList(newPlan.dailyPlans)
                            } else {
                                // Plan exists - load workout details and display
                                Log.d("HomeFragment", "Loaded existing plan for week ${weeklyPlan.weekStartDate}")
                                
                                // Populate workout objects from IDs
                                val dailyPlansWithWorkouts = weeklyPlan.dailyPlans.map { dailyPlan ->
                                    val workouts = dailyPlan.workoutIds.mapNotNull { id ->
                                        allWorkouts.find { it.id == id }
                                    }
                                    // Create a copy with populated workouts to avoid mutation
                                    dailyPlan.copy().apply {
                                        this.workouts = workouts
                                    }
                                }
                                
                                weeklyPlanAdapter.submitList(dailyPlansWithWorkouts)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload user data when fragment resumes
        userViewModel.loadCurrentUser()
    }
}
