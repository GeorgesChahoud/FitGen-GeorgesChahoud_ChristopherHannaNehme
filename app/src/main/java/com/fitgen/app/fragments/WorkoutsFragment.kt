package com.fitgen.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitgen.app.R
import com.fitgen.app.activities.WorkoutCategoryActivity
import com.fitgen.app.adapters.WorkoutCategory
import com.fitgen.app.adapters.WorkoutCategoryAdapter
import com.fitgen.app.utils.Constants
import com.fitgen.app.viewmodels.UserViewModel
import com.fitgen.app.viewmodels.WorkoutViewModel

class WorkoutsFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()
    private val workoutViewModel: WorkoutViewModel by activityViewModels()
    
    private lateinit var categoriesRecyclerView: RecyclerView
    
    private val categoryAdapter = WorkoutCategoryAdapter { categoryKey ->
        val intent = Intent(requireContext(), WorkoutCategoryActivity::class.java)
        intent.putExtra("category", categoryKey)
        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workouts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        loadCategories()
    }

    private fun initViews(view: View) {
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView)
    }

    private fun setupRecyclerView() {
        categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun loadCategories() {
        val categories = listOf(
            WorkoutCategory(
                name = getString(R.string.abs),
                count = workoutViewModel.getWorkoutCountByCategory(Constants.CATEGORY_ABS),
                color = ContextCompat.getColor(requireContext(), R.color.category_abs),
                categoryKey = Constants.CATEGORY_ABS
            ),
            WorkoutCategory(
                name = getString(R.string.biceps),
                count = workoutViewModel.getWorkoutCountByCategory(Constants.CATEGORY_BICEPS),
                color = ContextCompat.getColor(requireContext(), R.color.category_biceps),
                categoryKey = Constants.CATEGORY_BICEPS
            ),
            WorkoutCategory(
                name = getString(R.string.chest),
                count = workoutViewModel.getWorkoutCountByCategory(Constants.CATEGORY_CHEST),
                color = ContextCompat.getColor(requireContext(), R.color.category_chest),
                categoryKey = Constants.CATEGORY_CHEST
            ),
            WorkoutCategory(
                name = getString(R.string.legs),
                count = workoutViewModel.getWorkoutCountByCategory(Constants.CATEGORY_LEGS),
                color = ContextCompat.getColor(requireContext(), R.color.category_legs),
                categoryKey = Constants.CATEGORY_LEGS
            ),
            WorkoutCategory(
                name = getString(R.string.shoulders),
                count = workoutViewModel.getWorkoutCountByCategory(Constants.CATEGORY_SHOULDERS),
                color = ContextCompat.getColor(requireContext(), R.color.category_shoulders),
                categoryKey = Constants.CATEGORY_SHOULDERS
            ),
            WorkoutCategory(
                name = getString(R.string.back),
                count = workoutViewModel.getWorkoutCountByCategory(Constants.CATEGORY_BACK),
                color = ContextCompat.getColor(requireContext(), R.color.category_back),
                categoryKey = Constants.CATEGORY_BACK
            ),
            WorkoutCategory(
                name = getString(R.string.triceps),
                count = workoutViewModel.getWorkoutCountByCategory(Constants.CATEGORY_TRICEPS),
                color = ContextCompat.getColor(requireContext(), R.color.category_triceps),
                categoryKey = Constants.CATEGORY_TRICEPS
            ),
            WorkoutCategory(
                name = getString(R.string.cardio),
                count = workoutViewModel.getWorkoutCountByCategory(Constants.CATEGORY_CARDIO),
                color = ContextCompat.getColor(requireContext(), R.color.category_cardio),
                categoryKey = Constants.CATEGORY_CARDIO
            )
        )
        
        categoryAdapter.submitList(categories)
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }
}
