package com.fitgen.app.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.fitgen.app.models.CompletedWorkout
import com.fitgen.app.models.ProgressEntry
import com.fitgen.app.models.User
import com.fitgen.app.utils.Constants
import com.fitgen.app.utils.UsernameValidator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UserRepository(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Get current user from SharedPreferences
     */
    fun getCurrentUserFromPrefs(): User? {
        val uid = prefs.getString(Constants.PREF_USER_UID, null) ?: return null
        val email = prefs.getString(Constants.PREF_USER_EMAIL, "") ?: ""
        val username = prefs.getString(Constants.PREF_USER_USERNAME, "") ?: ""
        val friendCode = prefs.getString(Constants.PREF_USER_FRIEND_CODE, "") ?: ""
        val age = prefs.getInt(Constants.PREF_USER_AGE, 0)
        val height = prefs.getInt(Constants.PREF_USER_HEIGHT, 0)
        val weight = prefs.getString(Constants.PREF_USER_WEIGHT, "0")?.toDoubleOrNull() ?: 0.0
        val goal = prefs.getString(Constants.PREF_USER_GOAL, "") ?: ""
        val activityLevel = prefs.getString(Constants.PREF_USER_ACTIVITY_LEVEL, "") ?: ""
        val gender = prefs.getString(Constants.PREF_USER_GENDER, Constants.GENDER_MALE) ?: Constants.GENDER_MALE
        val workoutDaysPerWeek = prefs.getInt(Constants.PREF_USER_WORKOUT_DAYS_PER_WEEK, 3)

        return User(
            uid = uid,
            email = email,
            username = username,
            friendCode = friendCode,
            age = age,
            height = height,
            weight = weight,
            goal = goal,
            activityLevel = activityLevel,
            gender = gender,
            workoutDaysPerWeek = workoutDaysPerWeek
        )
    }

    /**
     * Save user to SharedPreferences
     */
    fun saveUserToPrefs(user: User) {
        Log.d("UserRepository", "Saving user to SharedPreferences: ${user.uid}")
        
        prefs.edit().apply {
            putString(Constants.PREF_USER_UID, user.uid)
            putString(Constants.PREF_USER_EMAIL, user.email)
            putString(Constants.PREF_USER_USERNAME, user.username)
            putString(Constants.PREF_USER_FRIEND_CODE, user.friendCode)
            putInt(Constants.PREF_USER_AGE, user.age)
            putInt(Constants.PREF_USER_HEIGHT, user.height)
            putString(Constants.PREF_USER_WEIGHT, user.weight.toString())
            putString(Constants.PREF_USER_GOAL, user.goal)
            putString(Constants.PREF_USER_ACTIVITY_LEVEL, user.activityLevel)
            putString(Constants.PREF_USER_GENDER, user.gender)
            putInt(Constants.PREF_USER_WORKOUT_DAYS_PER_WEEK, user.workoutDaysPerWeek)
            putBoolean(Constants.PREF_IS_LOGGED_IN, true)
            
            // NEW: If user has username, onboarding is complete
            if (user.username.isNotBlank()) {
                putBoolean(Constants.PREF_HAS_COMPLETED_ONBOARDING, true)
            }
            
            apply()
        }
        
        Log.d("UserRepository", "✅ User saved to SharedPreferences")
        
        // Verify
        val savedUid = prefs.getString(Constants.PREF_USER_UID, null)
        val isLoggedIn = prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false)
        Log.d("UserRepository", "Verification - UID: $savedUid, isLoggedIn: $isLoggedIn")
    }

    /**
     * Get user from Firestore with improved error handling
     */
    suspend fun getUserFromFirestore(uid: String): User? {
        return try {
            if (uid.isBlank()) {
                return null
            }
            
            val document = firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data
                if (data != null) {
                    User.fromMap(data)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save user to Firestore
     */
    suspend fun saveUserToFirestore(user: User): Boolean {
        return try {
            Log.d("UserRepository", "Saving user to Firestore: ${user.uid}")
            firestore.collection(Constants.COLLECTION_USERS)
                .document(user.uid)
                .set(user.toMap())
                .await()
            Log.d("UserRepository", "✅ User saved to Firestore successfully")
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Failed to save user to Firestore", e)
            e.printStackTrace()
            false
        }
    }

    /**
     * Update user in Firestore and SharedPreferences
     */
    suspend fun updateUser(user: User): Boolean {
        val success = saveUserToFirestore(user)
        if (success) {
            saveUserToPrefs(user)
        }
        return success
    }

    /**
     * Add progress entry with validation
     */
    suspend fun addProgressEntry(entry: ProgressEntry): Boolean {
        return try {
            // Validate entry
            if (entry.userId.isBlank() || entry.weight <= 0) {
                return false
            }
            
            val docRef = firestore.collection(Constants.COLLECTION_PROGRESS)
                .document()
            
            val entryWithId = entry.copy(id = docRef.id)
            
            docRef.set(entryWithId.toMap()).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get progress entries for a user
     */
    suspend fun getProgressEntries(userId: String): List<ProgressEntry> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_PROGRESS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { ProgressEntry.fromMap(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Clear user data from SharedPreferences
     */
    fun clearUserPrefs() {
        Log.d("UserRepository", "Clearing all SharedPreferences...")
        
        prefs.edit().clear().apply()
        
        Log.d("UserRepository", "✅ SharedPreferences cleared")
        
        // Verify cleared
        val uid = prefs.getString(Constants.PREF_USER_UID, null)
        val isLoggedIn = prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false)
        val hasOnboarding = prefs.getBoolean(Constants.PREF_HAS_COMPLETED_ONBOARDING, false)
        
        Log.d("UserRepository", "Verification - UID: $uid, isLoggedIn: $isLoggedIn, hasOnboarding: $hasOnboarding")
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        val authUser = auth.currentUser
        val prefsLoggedIn = prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false)
        val isLoggedIn = prefsLoggedIn && authUser != null
        
        Log.d("UserRepository", "Auth user: ${authUser?.uid}, Prefs: $prefsLoggedIn, Result: $isLoggedIn")
        return isLoggedIn
    }

    /**
     * Set onboarding completed
     */
    fun setOnboardingCompleted(completed: Boolean) {
        Log.d("UserRepository", "Setting onboarding completed = $completed")
        prefs.edit().putBoolean(Constants.PREF_HAS_COMPLETED_ONBOARDING, completed).apply()
        
        // Verify
        val saved = prefs.getBoolean(Constants.PREF_HAS_COMPLETED_ONBOARDING, false)
        Log.d("UserRepository", "Verification - onboarding flag saved: $saved")
    }

    /**
     * Check if onboarding is completed
     */
    fun hasCompletedOnboarding(): Boolean {
        // Check explicit flag first
        val hasFlag = prefs.getBoolean(Constants.PREF_HAS_COMPLETED_ONBOARDING, false)
        
        // Fallback: If user has a username, onboarding is complete
        // This handles migration for existing users
        val username = prefs.getString(Constants.PREF_USER_USERNAME, "") ?: ""
        val hasUsername = username.isNotBlank()
        
        val result = hasFlag || hasUsername
        
        Log.d("UserRepository", "hasCompletedOnboarding - flag: $hasFlag, username: $hasUsername, result: $result")
        
        // If username exists but flag is missing, set the flag (one-time migration)
        if (hasUsername && !hasFlag) {
            Log.d("UserRepository", "Migrating: Setting onboarding flag for existing user")
            prefs.edit().putBoolean(Constants.PREF_HAS_COMPLETED_ONBOARDING, true).apply()
        }
        
        return result
    }

    /**
     * Get current date in yyyy-MM-dd format
     */
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Add completed workout entry with validation
     */
    suspend fun addCompletedWorkout(completedWorkout: CompletedWorkout): Boolean {
        return try {
            // Validate workout
            if (completedWorkout.userId.isBlank() || completedWorkout.workoutId.isBlank()) {
                return false
            }
            
            val docRef = firestore.collection(Constants.COLLECTION_COMPLETED_WORKOUTS)
                .document()
            
            val workoutWithId = completedWorkout.copy(id = docRef.id)
            
            docRef.set(workoutWithId.toMap()).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get completed workouts for a user
     */
    suspend fun getCompletedWorkouts(userId: String): List<CompletedWorkout> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_COMPLETED_WORKOUTS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { CompletedWorkout.fromMap(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get completed workouts count for a user
     */
    suspend fun getCompletedWorkoutsCount(userId: String): Int {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_COMPLETED_WORKOUTS)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Check if username is available (not taken)
     */
    suspend fun isUsernameAvailable(username: String): Boolean {
        return try {
            Log.d("UserRepository", "==== USERNAME AVAILABILITY CHECK ====")
            Log.d("UserRepository", "Input username: '$username'")
            
            if (username.isBlank()) {
                Log.e("UserRepository", "Username is blank!")
                return false
            }
            
            // Check if user is authenticated
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("UserRepository", "User is not authenticated!")
                return true // Allow username to proceed, will fail later if there's an issue
            }
            
            Log.d("UserRepository", "User authenticated: ${currentUser.uid}")
            
            val formattedUsername = UsernameValidator.formatUsername(username)
            Log.d("UserRepository", "Formatted username: '$formattedUsername'")
            
            Log.d("UserRepository", "Querying Firestore for username: '$formattedUsername'")
            
            val snapshot = firestore.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("username", formattedUsername)
                .limit(1)
                .get()
                .await()
            
            val documentCount = snapshot.documents.size
            Log.d("UserRepository", "Query returned $documentCount documents")
            
            val isAvailable = snapshot.isEmpty
            Log.d("UserRepository", "Username '$formattedUsername' is available: $isAvailable")
            
            if (!isAvailable) {
                snapshot.documents.forEach { doc ->
                    Log.d("UserRepository", "Existing user found with document ID: ${doc.id}")
                }
            }
            
            Log.d("UserRepository", "==== END CHECK ====")
            
            isAvailable
        } catch (e: Exception) {
            Log.e("UserRepository", "==== ERROR IN USERNAME CHECK ====")
            Log.e("UserRepository", "Exception type: ${e.javaClass.simpleName}")
            Log.e("UserRepository", "Error message: ${e.message}")
            Log.e("UserRepository", "Stack trace:", e)
            Log.e("UserRepository", "==== END ERROR ====")
            
            // IMPORTANT: Return true on error to allow registration
            // This prevents blocking users when there's a network/permission issue
            // The actual save will fail if there's a real problem
            true
        }
    }

    /**
     * Search user by username
     */
    suspend fun searchUserByUsername(username: String): User? {
        return try {
            val formattedUsername = UsernameValidator.formatUsername(username)
            val snapshot = firestore.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("username", formattedUsername)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.data?.let { User.fromMap(it) }
        } catch (e: Exception) {
            null
        }
    }
}
