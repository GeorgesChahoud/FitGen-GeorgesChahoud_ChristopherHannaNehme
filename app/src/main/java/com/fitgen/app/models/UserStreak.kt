package com.fitgen.app.models

data class UserStreak(
    val userId: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String = "", // yyyy-MM-dd
    val totalChallengesCompleted: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "currentStreak" to currentStreak,
            "longestStreak" to longestStreak,
            "lastCompletedDate" to lastCompletedDate,
            "totalChallengesCompleted" to totalChallengesCompleted,
            "lastUpdated" to lastUpdated
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): UserStreak {
            return UserStreak(
                userId = map["userId"] as? String ?: "",
                currentStreak = (map["currentStreak"] as? Long)?.toInt() ?: 0,
                longestStreak = (map["longestStreak"] as? Long)?.toInt() ?: 0,
                lastCompletedDate = map["lastCompletedDate"] as? String ?: "",
                totalChallengesCompleted = (map["totalChallengesCompleted"] as? Long)?.toInt() ?: 0,
                lastUpdated = map["lastUpdated"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}
