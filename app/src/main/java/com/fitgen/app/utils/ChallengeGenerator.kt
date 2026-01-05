package com.fitgen.app.utils

import com.fitgen.app.models.ChallengeType
import com.fitgen.app.models.DailyChallenge
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

object ChallengeGenerator {

    private val challengeTemplates = mapOf(
        ChallengeType.PUSHUPS to listOf(10, 20, 30, 50),
        ChallengeType.SITUPS to listOf(15, 25, 40, 60),
        ChallengeType.SQUATS to listOf(20, 30, 50, 75),
        ChallengeType.PLANK to listOf(30, 60, 90, 120), // seconds
        ChallengeType.RUNNING to listOf(2, 3, 5, 7), // km
        ChallengeType.JUMPING_JACKS to listOf(30, 50, 75, 100),
        ChallengeType.BURPEES to listOf(10, 15, 20, 30),
        ChallengeType.LUNGES to listOf(20, 30, 40, 60),
        ChallengeType.MOUNTAIN_CLIMBERS to listOf(30, 50, 75, 100),
        ChallengeType.CRUNCHES to listOf(20, 30, 50, 75)
    )

    private val unitMap = mapOf(
        ChallengeType.PUSHUPS to "reps",
        ChallengeType.SITUPS to "reps",
        ChallengeType.SQUATS to "reps",
        ChallengeType.PLANK to "seconds",
        ChallengeType.RUNNING to "km",
        ChallengeType.JUMPING_JACKS to "reps",
        ChallengeType.BURPEES to "reps",
        ChallengeType.LUNGES to "reps",
        ChallengeType.MOUNTAIN_CLIMBERS to "reps",
        ChallengeType.CRUNCHES to "reps"
    )

    /**
     * Generate a daily challenge for a user
     * Uses date and userId as seed for deterministic generation (same challenge for same day)
     */
    fun generateDailyChallenge(userId: String, date: String): DailyChallenge {
        // Create a deterministic seed based on userId and date
        val seed = (userId + date).hashCode().toLong()
        val random = Random(seed)

        // Select random challenge type
        val challengeTypes = ChallengeType.values()
        val challengeType = challengeTypes[random.nextInt(challengeTypes.size)]

        // Select random difficulty level
        val targets = challengeTemplates[challengeType] ?: listOf(10)
        val target = targets[random.nextInt(targets.size)]

        // Get unit for this challenge type
        val unit = unitMap[challengeType] ?: "reps"

        // Generate description
        val description = generateDescription(challengeType, target, unit)

        return DailyChallenge(
            userId = userId,
            challengeType = challengeType,
            description = description,
            target = target,
            unit = unit,
            date = date,
            isCompleted = false,
            completedAt = 0,
            generatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Generate description for a challenge
     */
    private fun generateDescription(type: ChallengeType, target: Int, unit: String): String {
        return when (type) {
            ChallengeType.PUSHUPS -> "Do $target pushups"
            ChallengeType.SITUPS -> "Do $target situps"
            ChallengeType.SQUATS -> "Do $target squats"
            ChallengeType.PLANK -> "Hold plank for $target seconds"
            ChallengeType.RUNNING -> "Run $target km"
            ChallengeType.JUMPING_JACKS -> "Do $target jumping jacks"
            ChallengeType.BURPEES -> "Do $target burpees"
            ChallengeType.LUNGES -> "Do $target lunges"
            ChallengeType.MOUNTAIN_CLIMBERS -> "Do $target mountain climbers"
            ChallengeType.CRUNCHES -> "Do $target crunches"
        }
    }

    /**
     * Get current date in yyyy-MM-dd format
     */
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Parse date string to Date object
     */
    fun parseDate(dateString: String): Date? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}
