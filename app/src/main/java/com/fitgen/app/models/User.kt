package com.fitgen.app.models

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val friendCode: String = "",  // Friend code in format: XXXX-YYYY
    val age: Int = 0,
    val height: Int = 0, // in cm
    val weight: Double = 0.0, // in kg
    val goal: String = "", // "lose_weight", "maintain", "gain_muscle"
    val activityLevel: String = "", // "sedentary", "lightly_active", "moderately_active", "very_active"
    val gender: String = "male", // "male" or "female"
    val workoutDaysPerWeek: Int = 3, // 2-7 days
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "username" to username,
            "friendCode" to friendCode,
            "age" to age,
            "height" to height,
            "weight" to weight,
            "goal" to goal,
            "activityLevel" to activityLevel,
            "gender" to gender,
            "workoutDaysPerWeek" to workoutDaysPerWeek,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): User {
            return User(
                uid = map["uid"] as? String ?: "",
                email = map["email"] as? String ?: "",
                username = map["username"] as? String ?: "",
                friendCode = map["friendCode"] as? String ?: "",
                age = (map["age"] as? Long)?.toInt() ?: 0,
                height = (map["height"] as? Long)?.toInt() ?: 0,
                weight = (map["weight"] as? Number)?.toDouble() ?: 0.0,
                goal = map["goal"] as? String ?: "",
                activityLevel = map["activityLevel"] as? String ?: "",
                gender = map["gender"] as? String ?: "male",
                workoutDaysPerWeek = (map["workoutDaysPerWeek"] as? Long)?.toInt() ?: 3,
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}
