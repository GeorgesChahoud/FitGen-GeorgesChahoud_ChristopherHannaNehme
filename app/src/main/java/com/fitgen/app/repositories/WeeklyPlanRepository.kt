package com.fitgen.app.repositories

import android.util.Log
import com.fitgen.app.models.WeeklyWorkoutPlan
import com.fitgen.app.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class WeeklyPlanRepository {
    private val firestore = FirebaseFirestore.getInstance()
    
    /**
     * Get the current week's plan from Firestore
     * Returns null if no plan exists or if plan is expired
     */
    suspend fun getCurrentWeekPlan(userId: String): WeeklyWorkoutPlan? {
        return try {
            val currentWeekStart = getCurrentWeekStart()
            
            val snapshot = firestore.collection(Constants.COLLECTION_WEEKLY_PLANS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("weekStartDate", currentWeekStart)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                Log.d("WeeklyPlanRepository", "No plan found for week $currentWeekStart")
                null
            } else {
                val plan = snapshot.documents.first().data?.let { 
                    WeeklyWorkoutPlan.fromMap(it) 
                }
                Log.d("WeeklyPlanRepository", "Loaded plan for week $currentWeekStart")
                plan
            }
        } catch (e: Exception) {
            Log.e("WeeklyPlanRepository", "Error loading plan", e)
            null
        }
    }
    
    /**
     * Save a new weekly plan to Firestore
     */
    suspend fun saveWeeklyPlan(plan: WeeklyWorkoutPlan): Result<Boolean> {
        return try {
            // Delete old plans for this user
            deleteOldPlans(plan.userId)
            
            // Save new plan
            firestore.collection(Constants.COLLECTION_WEEKLY_PLANS)
                .add(plan.toMap())
                .await()
            
            Log.d("WeeklyPlanRepository", "Saved new plan for week ${plan.weekStartDate}")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("WeeklyPlanRepository", "Error saving plan", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete plans older than current week
     */
    private suspend fun deleteOldPlans(userId: String) {
        try {
            val currentWeekStart = getCurrentWeekStart()
            // Use server-side filtering for better performance
            val oldPlans = firestore.collection(Constants.COLLECTION_WEEKLY_PLANS)
                .whereEqualTo("userId", userId)
                .whereLessThan("weekStartDate", currentWeekStart)
                .get()
                .await()
            
            // Delete all old plans
            oldPlans.documents.forEach { doc ->
                doc.reference.delete().await()
                Log.d("WeeklyPlanRepository", "Deleted old plan for week ${doc.getString("weekStartDate")}")
            }
        } catch (e: Exception) {
            Log.e("WeeklyPlanRepository", "Error deleting old plans", e)
        }
    }
    
    /**
     * Get current week's Monday in yyyy-MM-dd format
     */
    private fun getCurrentWeekStart(): String {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}
