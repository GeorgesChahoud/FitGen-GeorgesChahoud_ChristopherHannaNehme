package com.fitgen.app.models

data class WeeklyWorkoutPlan(
    val userId: String = "",
    val weekStartDate: String = "",  // Format: "2026-01-06" (Monday)
    val dailyPlans: List<DailyWorkoutPlan> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "weekStartDate" to weekStartDate,
            "dailyPlans" to dailyPlans.map { it.toMap() },
            "generatedAt" to generatedAt
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>): WeeklyWorkoutPlan {
            return WeeklyWorkoutPlan(
                userId = map["userId"] as? String ?: "",
                weekStartDate = map["weekStartDate"] as? String ?: "",
                dailyPlans = (map["dailyPlans"] as? List<Map<String, Any>>)
                    ?.map { DailyWorkoutPlan.fromMap(it) } ?: emptyList(),
                generatedAt = map["generatedAt"] as? Long ?: 0L
            )
        }
    }
}

data class DailyWorkoutPlan(
    val dayOfWeek: String = "",  // "Monday", "Tuesday", etc.
    val dayName: String = "",  // e.g., "Upper Body Day", "Cardio & Core"
    val description: String = "",  // Brief description
    val workoutIds: List<String> = emptyList(),  // Store IDs only
    val isRestDay: Boolean = false,
    val totalDuration: Int = 0,  // Total minutes
    val totalCalories: Int = 0   // Estimated calories
) {
    // Transient field - not stored in Firestore, populated when loading
    @Transient
    var workouts: List<Workout> = emptyList()
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "dayOfWeek" to dayOfWeek,
            "dayName" to dayName,
            "description" to description,
            "workoutIds" to workoutIds,
            "isRestDay" to isRestDay,
            "totalDuration" to totalDuration,
            "totalCalories" to totalCalories
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>): DailyWorkoutPlan {
            // Safely extract workoutIds list with validation
            val rawWorkoutIds = map["workoutIds"] as? List<*>
            val workoutIds = rawWorkoutIds?.mapNotNull { it as? String } ?: emptyList()
            
            return DailyWorkoutPlan(
                dayOfWeek = map["dayOfWeek"] as? String ?: "",
                dayName = map["dayName"] as? String ?: "",
                description = map["description"] as? String ?: "",
                workoutIds = workoutIds,
                isRestDay = map["isRestDay"] as? Boolean ?: false,
                totalDuration = (map["totalDuration"] as? Long)?.toInt() ?: 0,
                totalCalories = (map["totalCalories"] as? Long)?.toInt() ?: 0
            )
        }
    }
}
