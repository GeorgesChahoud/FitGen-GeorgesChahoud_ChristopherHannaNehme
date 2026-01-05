package com.fitgen.app.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitgen.app.models.DailyChallenge
import com.fitgen.app.models.LeaderboardEntry
import com.fitgen.app.models.UserStreak
import com.fitgen.app.repositories.ChallengeRepository
import com.fitgen.app.utils.ChallengeGenerator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ChallengeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChallengeRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _todayChallenge = MutableLiveData<DailyChallenge?>()
    val todayChallenge: LiveData<DailyChallenge?> = _todayChallenge

    private val _userStreak = MutableLiveData<UserStreak>()
    val userStreak: LiveData<UserStreak> = _userStreak

    private val _leaderboard = MutableLiveData<List<LeaderboardEntry>>()
    val leaderboard: LiveData<List<LeaderboardEntry>> = _leaderboard

    private val _timeUntilMidnight = MutableLiveData<Long>()
    val timeUntilMidnight: LiveData<Long> = _timeUntilMidnight

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _completionSuccess = MutableLiveData<Boolean>()
    val completionSuccess: LiveData<Boolean> = _completionSuccess

    private var countdownJob: Job? = null
    private var leaderboardJob: Job? = null
    private var leaderboardListenerActive = false

    /**
     * Load today's challenge for the current user
     */
    fun loadTodayChallenge() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // First generate challenge if needed
                repository.generateChallengeIfNeeded(userId)
                
                // Then listen for changes
                repository.getTodayChallenge(userId)
                    .catch { e ->
                        _errorMessage.value = e.message
                    }
                    .collect { challenge ->
                        _todayChallenge.value = challenge
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Load user streak
     */
    fun loadUserStreak() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                repository.getUserStreak(userId)
                    .catch { e ->
                        _errorMessage.value = e.message
                    }
                    .collect { streak ->
                        _userStreak.value = streak
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Load leaderboard - only starts listener once
     */
    fun loadLeaderboard() {
        val userId = auth.currentUser?.uid ?: return

        // Re-emit cached data if listener already active
        if (leaderboardListenerActive) {
            Log.d("ChallengeViewModel", "Leaderboard listener active, re-emitting cached data")
            _leaderboard.value = _leaderboard.value
            return
        }

        Log.d("ChallengeViewModel", "Starting leaderboard listener for user: $userId")
        leaderboardListenerActive = true

        leaderboardJob = viewModelScope.launch {
            repository.getLeaderboard(userId)
                .catch { e ->
                    if (e !is CancellationException) {
                        Log.e("ChallengeViewModel", "Leaderboard error", e)
                        _errorMessage.value = e.message
                    }
                }
                .collect { entries ->
                    Log.d("ChallengeViewModel", "Leaderboard updated: ${entries.size} entries")
                    _leaderboard.value = entries
                }
        }
    }

    /**
     * Complete today's challenge
     */
    fun completeChallenge() {
        val userId = auth.currentUser?.uid ?: return
        val challenge = _todayChallenge.value ?: return

        if (challenge.isCompleted) {
            _errorMessage.value = "Challenge already completed"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.completeChallenge(challenge.id, userId)
                
                if (result.isSuccess) {
                    _completionSuccess.value = true
                    _userStreak.value = result.getOrNull()
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to complete challenge"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Check for missed challenges and reset streak if needed
     */
    fun checkMissedChallenges() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                repository.checkMissedChallenges(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Start countdown timer to midnight
     */
    fun startMidnightCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val timeToMidnight = getMillisUntilMidnight()
                _timeUntilMidnight.value = timeToMidnight
                delay(1000) // Update every second
            }
        }
    }

    /**
     * Stop countdown timer
     */
    fun stopMidnightCountdown() {
        countdownJob?.cancel()
    }

    /**
     * Get milliseconds until midnight
     */
    private fun getMillisUntilMidnight(): Long {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return midnight.timeInMillis - now.timeInMillis
    }

    /**
     * Format time in HH:MM:SS
     */
    fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        stopMidnightCountdown()
        leaderboardJob?.cancel()
        leaderboardListenerActive = false
    }
}
