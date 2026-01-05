package com.fitgen.app.utils

import com.fitgen.app.models.UserStreak
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object StreakManager {

    /**
     * Update user streak based on challenge completion
     */
    fun updateStreak(currentStreak: UserStreak, completed: Boolean, currentDate: String): UserStreak {
        if (!completed) {
            // If not completing today, don't update
            return currentStreak
        }

        val lastDate = currentStreak.lastCompletedDate
        val newStreakCount: Int
        val newLongestStreak: Int

        if (lastDate.isEmpty()) {
            // First time completing a challenge
            newStreakCount = 1
            newLongestStreak = 1
        } else {
            val daysSinceLastCompletion = getDaysBetween(lastDate, currentDate)
            
            when (daysSinceLastCompletion) {
                0L -> {
                    // Same day, no change to streak
                    return currentStreak
                }
                1L -> {
                    // Consecutive day, increment streak
                    newStreakCount = currentStreak.currentStreak + 1
                    newLongestStreak = maxOf(newStreakCount, currentStreak.longestStreak)
                }
                else -> {
                    // Missed days, reset streak to 1
                    newStreakCount = 1
                    newLongestStreak = currentStreak.longestStreak
                }
            }
        }

        return UserStreak(
            userId = currentStreak.userId,
            currentStreak = newStreakCount,
            longestStreak = newLongestStreak,
            lastCompletedDate = currentDate,
            totalChallengesCompleted = currentStreak.totalChallengesCompleted + 1,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Check if streak should be reset based on last completed date
     * Returns true if streak is still valid (completed yesterday or today)
     */
    fun calculateStreakStatus(lastCompletedDate: String, currentDate: String = getCurrentDate()): Boolean {
        if (lastCompletedDate.isEmpty()) {
            return false
        }

        val daysSinceLastCompletion = getDaysBetween(lastCompletedDate, currentDate)
        
        // Streak is valid if completed today (0 days) or yesterday (1 day)
        return daysSinceLastCompletion <= 1
    }

    /**
     * Reset streak to 0 if user missed a day
     */
    fun resetStreakIfNeeded(currentStreak: UserStreak, currentDate: String = getCurrentDate()): UserStreak {
        if (currentStreak.lastCompletedDate.isEmpty()) {
            return currentStreak
        }

        val isValid = calculateStreakStatus(currentStreak.lastCompletedDate, currentDate)
        
        return if (!isValid && currentStreak.currentStreak > 0) {
            // Streak is broken, reset to 0
            currentStreak.copy(
                currentStreak = 0,
                lastUpdated = System.currentTimeMillis()
            )
        } else {
            currentStreak
        }
    }

    /**
     * Get days between two dates (yyyy-MM-dd format)
     */
    private fun getDaysBetween(date1: String, date2: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d1 = sdf.parse(date1) ?: return Long.MAX_VALUE
            val d2 = sdf.parse(date2) ?: return Long.MAX_VALUE
            
            val diffInMillis = d2.time - d1.time
            TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }

    /**
     * Get current date in yyyy-MM-dd format
     */
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
