package com.fitgen.app.models

data class Workout(
    val id: String = "",
    val name: String = "",
    val category: String = "", // "abs", "biceps", "chest", "legs", "shoulders", "back", "triceps", "cardio"
    val difficulty: String = "", // "beginner", "intermediate", "advanced"
    val duration: Int = 0, // in minutes
    val caloriesBurn: Int = 0,
    val instructions: String = "",
    val imageResource: Int = 0,
    val sets: Int = 3,
    val reps: String = "10-12"
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "category" to category,
            "difficulty" to difficulty,
            "duration" to duration,
            "caloriesBurn" to caloriesBurn,
            "instructions" to instructions,
            "imageResource" to imageResource,
            "sets" to sets,
            "reps" to reps
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Workout {
            return Workout(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                category = map["category"] as? String ?: "",
                difficulty = map["difficulty"] as? String ?: "",
                duration = (map["duration"] as? Long)?.toInt() ?: 0,
                caloriesBurn = (map["caloriesBurn"] as? Long)?.toInt() ?: 0,
                instructions = map["instructions"] as? String ?: "",
                imageResource = (map["imageResource"] as? Long)?.toInt() ?: 0,
                sets = (map["sets"] as? Long)?.toInt() ?: 3,
                reps = map["reps"] as? String ?: "10-12"
            )
        }
    }
}
