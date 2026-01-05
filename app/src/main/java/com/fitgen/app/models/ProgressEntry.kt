package com.fitgen.app.models

data class ProgressEntry(
    val id: String = "",
    val userId: String = "",
    val weight: Double = 0.0, // in kg
    val date: String = "", // yyyy-MM-dd format
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "weight" to weight,
            "date" to date,
            "timestamp" to timestamp,
            "notes" to notes
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): ProgressEntry {
            return ProgressEntry(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                weight = (map["weight"] as? Number)?.toDouble() ?: 0.0,
                date = map["date"] as? String ?: "",
                timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis(),
                notes = map["notes"] as? String ?: ""
            )
        }
    }
}
