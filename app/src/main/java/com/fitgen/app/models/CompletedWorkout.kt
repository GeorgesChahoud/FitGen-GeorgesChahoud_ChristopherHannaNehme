package com.fitgen.app.models

/**
 * Model for tracking completed workouts
 */
data class CompletedWorkout(
    val id: String = "",
    val userId: String = "",
    val workoutId: String = "",
    val workoutName: String = "",
    val workoutCategory: String = "",
    val duration: Int = 0, // in minutes
    val caloriesBurned: Int = 0,
    val completedDate: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "workoutId" to workoutId,
            "workoutName" to workoutName,
            "workoutCategory" to workoutCategory,
            "duration" to duration,
            "caloriesBurned" to caloriesBurned,
            "completedDate" to completedDate,
            "timestamp" to timestamp,
            "notes" to notes
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): CompletedWorkout {
            return CompletedWorkout(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                workoutId = map["workoutId"] as? String ?: "",
                workoutName = map["workoutName"] as? String ?: "",
                workoutCategory = map["workoutCategory"] as? String ?: "",
                duration = (map["duration"] as? Long)?.toInt() ?: 0,
                caloriesBurned = (map["caloriesBurned"] as? Long)?.toInt() ?: 0,
                completedDate = map["completedDate"] as? String ?: "",
                timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis(),
                notes = map["notes"] as? String ?: ""
            )
        }
    }
}
