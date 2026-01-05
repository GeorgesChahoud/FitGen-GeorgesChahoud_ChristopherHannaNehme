package com.fitgen.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fitgen.app.models.Workout
import com.fitgen.app.repositories.WorkoutRepository

class WorkoutViewModel : ViewModel() {
    private val repository = WorkoutRepository()
    
    private val _allWorkouts = MutableLiveData<List<Workout>>()
    val allWorkouts: LiveData<List<Workout>> = _allWorkouts
    
    private val _filteredWorkouts = MutableLiveData<List<Workout>>()
    val filteredWorkouts: LiveData<List<Workout>> = _filteredWorkouts
    
    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories
    
    private val _selectedCategory = MutableLiveData<String?>()
    val selectedCategory: LiveData<String?> = _selectedCategory
    
    private val _selectedWorkout = MutableLiveData<Workout?>()
    val selectedWorkout: LiveData<Workout?> = _selectedWorkout

    init {
        loadWorkouts()
        loadCategories()
    }

    /**
     * Load all workouts
     */
    fun loadWorkouts() {
        val workouts = repository.getAllWorkouts()
        _allWorkouts.value = workouts
        applyFilters()
    }

    /**
     * Load categories
     */
    private fun loadCategories() {
        _categories.value = repository.getAllCategories()
    }

    /**
     * Set selected category
     */
    fun selectCategory(category: String?) {
        _selectedCategory.value = category
        applyFilters()
    }

    /**
     * Get workouts by category
     */
    fun getWorkoutsByCategory(category: String): List<Workout> {
        val allWorkouts = _allWorkouts.value ?: return emptyList()
        return allWorkouts.filter { it.category == category }
    }

    /**
     * Apply filters (category)
     */
    private fun applyFilters() {
        var workouts = _allWorkouts.value ?: return
        
        // Filter by category if selected
        val category = _selectedCategory.value
        if (category != null) {
            workouts = workouts.filter { it.category == category }
        }
        
        _filteredWorkouts.value = workouts
    }

    /**
     * Select a workout
     */
    fun selectWorkout(workout: Workout) {
        _selectedWorkout.value = workout
    }

    /**
     * Get workout by ID
     */
    fun getWorkoutById(id: String): Workout? {
        return repository.getWorkoutById(id)
    }

    /**
     * Get workouts count by category
     */
    fun getWorkoutCountByCategory(category: String): Int {
        return getWorkoutsByCategory(category).size
    }

    /**
     * Get total workouts count
     */
    fun getTotalWorkoutsCount(): Int {
        return _allWorkouts.value?.size ?: 0
    }

    /**
     * Get recommended workouts (random selection)
     */
    fun getRecommendedWorkouts(count: Int = 5): List<Workout> {
        val workouts = _allWorkouts.value ?: return emptyList()
        return workouts.shuffled().take(count)
    }
}
