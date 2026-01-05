package com.fitgen.app.models

data class LeaderboardEntry(
    val userId: String = "",
    val username: String = "",
    val userName: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalChallengesCompleted: Int = 0,
    val rank: Int = 0,
    val isCurrentUser: Boolean = false
)
