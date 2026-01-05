package com.fitgen.app.models

data class Friend(
    val id: String = "",
    val userId: String = "",
    val friendId: String = "",
    val friendUsername: String = "",
    val friendName: String = "",
    val currentStreak: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "friendId" to friendId,
            "friendUsername" to friendUsername,
            "friendName" to friendName,
            "currentStreak" to currentStreak,
            "addedAt" to addedAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Friend {
            return Friend(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                friendId = map["friendId"] as? String ?: "",
                friendUsername = map["friendUsername"] as? String ?: map["friendEmail"] as? String ?: "",
                friendName = map["friendName"] as? String ?: "",
                currentStreak = (map["currentStreak"] as? Long)?.toInt() ?: 0,
                addedAt = map["addedAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}
