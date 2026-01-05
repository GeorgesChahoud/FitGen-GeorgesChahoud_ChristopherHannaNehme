package com.fitgen.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fitgen.app.repositories.ChallengeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StreakCheckerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = ChallengeRepository()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure()

            // Check for missed challenges and reset streak if needed
            val result = repository.checkMissedChallenges(userId)

            if (result.isSuccess) {
                // Generate new challenge for today if needed
                repository.generateChallengeIfNeeded(userId)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
