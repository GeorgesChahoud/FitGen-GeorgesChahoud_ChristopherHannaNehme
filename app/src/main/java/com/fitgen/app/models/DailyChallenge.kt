package com.fitgen.app.models

data class DailyChallenge(
    val id: String = "",
    val userId: String = "",
    val challengeType: ChallengeType = ChallengeType.PUSHUPS,
    val description: String = "",
    val target: Int = 0, // e.g., 20 pushups, 5 km
    val unit: String = "", // "reps", "km", "minutes", "seconds"
    val date: String = "", // yyyy-MM-dd format
    val isCompleted: Boolean = false,
    val completedAt: Long = 0,
    val generatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "challengeType" to challengeType.name,
            "description" to description,
            "target" to target,
            "unit" to unit,
            "date" to date,
            "isCompleted" to isCompleted,
            "completedAt" to completedAt,
            "generatedAt" to generatedAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): DailyChallenge {
            return DailyChallenge(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                challengeType = try {
                    ChallengeType.valueOf(map["challengeType"] as? String ?: "PUSHUPS")
                } catch (e: Exception) {
                    ChallengeType.PUSHUPS
                },
                description = map["description"] as? String ?: "",
                target = (map["target"] as? Long)?.toInt() ?: 0,
                unit = map["unit"] as? String ?: "",
                date = map["date"] as? String ?: "",
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                completedAt = map["completedAt"] as? Long ?: 0,
                generatedAt = map["generatedAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}

enum class ChallengeType {
    PUSHUPS,
    SITUPS,
    SQUATS,
    PLANK,
    RUNNING,
    JUMPING_JACKS,
    BURPEES,
    LUNGES,
    MOUNTAIN_CLIMBERS,
    CRUNCHES
}
