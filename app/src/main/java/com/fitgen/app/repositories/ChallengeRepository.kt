package com.fitgen.app.repositories

import android.util.Log
import com.fitgen.app.models.DailyChallenge
import com.fitgen.app.models.LeaderboardEntry
import com.fitgen.app.models.UserStreak
import com.fitgen.app.utils.ChallengeGenerator
import com.fitgen.app.utils.Constants
import com.fitgen.app.utils.StreakManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChallengeRepository {
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Get today's challenge for a user (real-time)
     */
    fun getTodayChallenge(userId: String): Flow<DailyChallenge?> = callbackFlow {
        val currentDate = ChallengeGenerator.getCurrentDate()
        
        val listener = firestore.collection(Constants.COLLECTION_DAILY_CHALLENGES)
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", currentDate)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val challenge = snapshot?.documents?.firstOrNull()?.data?.let {
                    DailyChallenge.fromMap(it)
                }

                trySend(challenge)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Generate a challenge if one doesn't exist for today
     */
    suspend fun generateChallengeIfNeeded(userId: String): DailyChallenge {
        val currentDate = ChallengeGenerator.getCurrentDate()
        
        // Check if challenge already exists for today
        val existing = firestore.collection(Constants.COLLECTION_DAILY_CHALLENGES)
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", currentDate)
            .limit(1)
            .get()
            .await()

        if (!existing.isEmpty) {
            return DailyChallenge.fromMap(existing.documents.first().data ?: emptyMap())
        }

        // Generate new challenge
        val challenge = ChallengeGenerator.generateDailyChallenge(userId, currentDate)
        
        // Save to Firestore
        val docRef = firestore.collection(Constants.COLLECTION_DAILY_CHALLENGES).document()
        val challengeWithId = challenge.copy(id = docRef.id)
        docRef.set(challengeWithId.toMap()).await()

        return challengeWithId
    }

    /**
     * Complete a challenge and update streak
     */
    suspend fun completeChallenge(challengeId: String, userId: String): Result<UserStreak> {
        return try {
            val currentDate = ChallengeGenerator.getCurrentDate()

            // Update challenge
            firestore.collection(Constants.COLLECTION_DAILY_CHALLENGES)
                .document(challengeId)
                .update(
                    mapOf(
                        "isCompleted" to true,
                        "completedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            // Get current streak
            val streakDoc = firestore.collection(Constants.COLLECTION_USER_STREAKS)
                .document(userId)
                .get()
                .await()

            val currentStreak = if (streakDoc.exists()) {
                UserStreak.fromMap(streakDoc.data ?: emptyMap())
            } else {
                UserStreak(userId = userId)
            }

            // Update streak
            val updatedStreak = StreakManager.updateStreak(currentStreak, true, currentDate)

            // Save updated streak
            firestore.collection(Constants.COLLECTION_USER_STREAKS)
                .document(userId)
                .set(updatedStreak.toMap())
                .await()

            // Update friend's streak display
            updateFriendStreaks(userId, updatedStreak.currentStreak)

            Result.success(updatedStreak)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user streak (real-time)
     */
    fun getUserStreak(userId: String): Flow<UserStreak> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_USER_STREAKS)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val streak = if (snapshot?.exists() == true) {
                    UserStreak.fromMap(snapshot.data ?: emptyMap())
                } else {
                    UserStreak(userId = userId)
                }

                trySend(streak)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get leaderboard with friends (real-time)
     */
    fun getLeaderboard(userId: String): Flow<List<LeaderboardEntry>> = callbackFlow {
        // First get user's friends
        val friendsSnapshot = firestore.collection(Constants.COLLECTION_FRIENDS)
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val friendIds = friendsSnapshot.documents.mapNotNull { it.getString("friendId") }
        val allUserIds = friendIds + userId

        if (allUserIds.isEmpty()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }

        // Listen to streaks for all users (user + friends)
        val listener = firestore.collection(Constants.COLLECTION_USER_STREAKS)
            .whereIn("userId", allUserIds)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val streaks = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { UserStreak.fromMap(it) }
                } ?: emptyList()

                // Launch coroutine to fetch user data synchronously
                launch {
                    val entries = mutableListOf<LeaderboardEntry>()
                    
                    for (streak in streaks) {
                        try {
                            val userDoc = firestore.collection(Constants.COLLECTION_USERS)
                                .document(streak.userId)
                                .get()
                                .await()  // Wait for completion
                            
                            if (userDoc.exists()) {
                                val username = userDoc.getString("username") ?: ""
                                entries.add(
                                    LeaderboardEntry(
                                        userId = streak.userId,
                                        username = username,
                                        userName = username,
                                        currentStreak = streak.currentStreak,
                                        longestStreak = streak.longestStreak,
                                        totalChallengesCompleted = streak.totalChallengesCompleted,
                                        rank = 0,
                                        isCurrentUser = streak.userId == userId
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("ChallengeRepository", "Error fetching user data", e)
                        }
                    }

                    // Sort and assign ranks
                    val sortedEntries = entries
                        .sortedByDescending { it.currentStreak }
                        .mapIndexed { index, entry -> entry.copy(rank = index + 1) }

                    trySend(sortedEntries)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Check if user missed yesterday's challenge and reset streak if needed
     */
    suspend fun checkMissedChallenges(userId: String): Result<Boolean> {
        return try {
            val currentDate = ChallengeGenerator.getCurrentDate()

            // Get user's streak
            val streakDoc = firestore.collection(Constants.COLLECTION_USER_STREAKS)
                .document(userId)
                .get()
                .await()

            if (!streakDoc.exists()) {
                return Result.success(true)
            }

            val currentStreak = UserStreak.fromMap(streakDoc.data ?: emptyMap())
            
            // Reset streak if needed
            val updatedStreak = StreakManager.resetStreakIfNeeded(currentStreak, currentDate)

            if (updatedStreak.currentStreak != currentStreak.currentStreak) {
                // Streak was reset, save it
                firestore.collection(Constants.COLLECTION_USER_STREAKS)
                    .document(userId)
                    .set(updatedStreak.toMap())
                    .await()

                // Update friend's streak display
                updateFriendStreaks(userId, 0)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update the currentStreak field in all friend relationships
     */
    private suspend fun updateFriendStreaks(userId: String, newStreak: Int) {
        try {
            // Update all friend documents where this user is the friend
            val friendDocs = firestore.collection(Constants.COLLECTION_FRIENDS)
                .whereEqualTo("friendId", userId)
                .get()
                .await()

            friendDocs.documents.forEach { doc ->
                doc.reference.update("currentStreak", newStreak).await()
            }
        } catch (e: Exception) {
            // Don't fail the operation if friend streak update fails
            e.printStackTrace()
        }
    }
}
