package com.fitgen.app.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitgen.app.models.ProgressEntry
import com.fitgen.app.models.User
import com.fitgen.app.repositories.UserRepository
import com.fitgen.app.utils.CalorieCalculator
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(application.applicationContext)
    
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    private val _progressEntries = MutableLiveData<List<ProgressEntry>>()
    val progressEntries: LiveData<List<ProgressEntry>> = _progressEntries
    
    private val _dailyCalorieTarget = MutableLiveData<Int>()
    val dailyCalorieTarget: LiveData<Int> = _dailyCalorieTarget
    
    private val _bmr = MutableLiveData<Double>()
    val bmr: LiveData<Double> = _bmr
    
    private val _tdee = MutableLiveData<Double>()
    val tdee: LiveData<Double> = _tdee
    
    private val _bmi = MutableLiveData<Double>()
    val bmi: LiveData<Double> = _bmi
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadCurrentUser()
    }

    /**
     * Load current user from SharedPreferences
     */
    fun loadCurrentUser() {
        val user = repository.getCurrentUserFromPrefs()
        _currentUser.value = user
        user?.let {
            calculateMetrics(it)
        }
    }

    /**
     * Load user from Firestore and save to SharedPreferences
     */
    fun loadUserFromFirestore(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val user = repository.getUserFromFirestore(uid)
                if (user != null) {
                    _currentUser.value = user
                    repository.saveUserToPrefs(user)  // Save to SharedPreferences
                    calculateMetrics(user)
                } else {
                    // User not in Firestore - might need onboarding
                    // Try to create a basic user from Firebase Auth
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null) {
                        _error.value = "User profile not found. Please complete onboarding."
                    } else {
                        _error.value = "User not found"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sync user data - tries SharedPreferences first, then Firestore
     */
    fun syncUserData() {
        // First try SharedPreferences
        val localUser = repository.getCurrentUserFromPrefs()
        if (localUser != null && localUser.uid.isNotBlank()) {
            _currentUser.value = localUser
            calculateMetrics(localUser)
            return
        }
        
        // If no local data, try Firestore
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            loadUserFromFirestore(firebaseUser.uid)
        }
    }

    /**
     * Save user data
     */
    fun saveUser(user: User) {
        Log.d("UserViewModel", "Saving user: ${user.uid}")
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Save to Firestore
                val firestoreSuccess = repository.saveUserToFirestore(user)
                Log.d("UserViewModel", "Firestore save: $firestoreSuccess")
                
                if (firestoreSuccess) {
                    // Save to SharedPreferences
                    repository.saveUserToPrefs(user)
                    Log.d("UserViewModel", "User saved to SharedPreferences")
                    
                    // Update LiveData
                    _currentUser.value = user
                    calculateMetrics(user)
                    Log.d("UserViewModel", "✅ User saved successfully")
                } else {
                    Log.e("UserViewModel", "❌ Failed to save user to Firestore")
                    _error.value = "Failed to save user data"
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ Error saving user", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update user profile
     */
    fun updateProfile(age: Int, height: Int, weight: Double, goal: String, activityLevel: String) {
        val currentUser = _currentUser.value ?: return
        val updatedUser = currentUser.copy(
            age = age,
            height = height,
            weight = weight,
            goal = goal,
            activityLevel = activityLevel
        )
        saveUser(updatedUser)
    }

    /**
     * Add progress entry
     */
    fun addProgressEntry(weight: Double, notes: String = "") {
        val user = _currentUser.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val entry = ProgressEntry(
                    userId = user.uid,
                    weight = weight,
                    date = repository.getCurrentDate(),
                    notes = notes
                )
                
                val success = repository.addProgressEntry(entry)
                if (success) {
                    // Update current user weight
                    val updatedUser = user.copy(weight = weight)
                    saveUser(updatedUser)
                    // Reload progress entries
                    loadProgressEntries()
                } else {
                    _error.value = "Failed to add progress entry"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load progress entries
     */
    fun loadProgressEntries() {
        val user = _currentUser.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val entries = repository.getProgressEntries(user.uid)
                _progressEntries.value = entries
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Calculate all metrics (BMR, TDEE, calories, BMI)
     */
    private fun calculateMetrics(user: User) {
        _bmr.value = CalorieCalculator.calculateBMR(user)
        _tdee.value = CalorieCalculator.calculateTDEE(user)
        _dailyCalorieTarget.value = CalorieCalculator.calculateDailyCalorieTarget(user)
        _bmi.value = CalorieCalculator.calculateBMI(user.weight, user.height)
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        val loggedIn = repository.isLoggedIn()
        Log.d("UserViewModel", "isLoggedIn: $loggedIn")
        return loggedIn
    }

    /**
     * Logout user - delegates to clearUserData()
     * Use this method for backward compatibility
     */
    fun logout() {
        clearUserData()
    }

    /**
     * Clear all user data (for logout)
     * 
     * This method performs a complete logout by:
     * - Clearing all SharedPreferences (including login state and onboarding completion)
     * - Resetting the current user LiveData to null
     * 
     * Note: This does NOT sign out from Firebase Auth - caller should handle that separately
     * 
     * @see logout For a convenience method that delegates to this
     */
    fun clearUserData() {
        Log.d("UserViewModel", "==== CLEARING USER DATA ====")
        
        // Clear SharedPreferences
        repository.clearUserPrefs()
        Log.d("UserViewModel", "✅ Cleared SharedPreferences")
        
        // Clear LiveData
        _currentUser.value = null
        Log.d("UserViewModel", "✅ Cleared LiveData")
        
        // Verify cleared
        val isLoggedIn = repository.isLoggedIn()
        val hasOnboarding = repository.hasCompletedOnboarding()
        Log.d("UserViewModel", "Verification - isLoggedIn: $isLoggedIn, hasOnboarding: $hasOnboarding")
    }

    /**
     * Set onboarding completed
     */
    fun setOnboardingCompleted() {
        Log.d("UserViewModel", "Setting onboarding completed = true")
        repository.setOnboardingCompleted(true)
        
        // Verify it was set
        val isCompleted = repository.hasCompletedOnboarding()
        Log.d("UserViewModel", "Onboarding completed status: $isCompleted")
    }

    /**
     * Check if onboarding is completed
     */
    fun hasCompletedOnboarding(): Boolean {
        val completed = repository.hasCompletedOnboarding()
        Log.d("UserViewModel", "hasCompletedOnboarding: $completed")
        return completed
    }

    /**
     * Get user from Firestore
     */
    suspend fun getUserFromFirestore(userId: String): User? {
        Log.d("UserViewModel", "Getting user from Firestore: $userId")
        return repository.getUserFromFirestore(userId)
    }

    /**
     * Save user to SharedPreferences only
     */
    fun saveUserToPrefs(user: User) {
        Log.d("UserViewModel", "Saving user to SharedPreferences: ${user.uid}")
        repository.saveUserToPrefs(user)
        _currentUser.value = user
    }

    /**
     * Get current user from SharedPreferences
     * 
     * This method retrieves user data directly from SharedPreferences without
     * making any network calls. Use this method when you need to:
     * - Check if valid local user data exists during app initialization
     * - Validate authentication state before auto-navigation
     * 
     * For loading user data with LiveData updates, use loadCurrentUser() or syncUserData() instead.
     * 
     * @return User object from SharedPreferences, or null if no user data exists
     */
    fun getCurrentUserFromPrefs(): User? {
        return repository.getCurrentUserFromPrefs()
    }
}
