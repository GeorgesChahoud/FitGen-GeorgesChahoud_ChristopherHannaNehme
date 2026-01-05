package com.fitgen.app.models

data class FriendRequest(
    val id: String = "",
    val fromUserId: String = "",
    val fromUsername: String = "",
    val toUserId: String = "",
    val toUsername: String = "",
    val status: FriendRequestStatus = FriendRequestStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "fromUserId" to fromUserId,
            "fromUsername" to fromUsername,
            "toUserId" to toUserId,
            "toUsername" to toUsername,
            "status" to status.name,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): FriendRequest {
            return FriendRequest(
                id = map["id"] as? String ?: "",
                fromUserId = map["fromUserId"] as? String ?: "",
                fromUsername = map["fromUsername"] as? String ?: map["fromUserEmail"] as? String ?: "",
                toUserId = map["toUserId"] as? String ?: "",
                toUsername = map["toUsername"] as? String ?: map["toUserEmail"] as? String ?: "",
                status = try {
                    FriendRequestStatus.valueOf(map["status"] as? String ?: "PENDING")
                } catch (e: Exception) {
                    FriendRequestStatus.PENDING
                },
                timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}

enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
