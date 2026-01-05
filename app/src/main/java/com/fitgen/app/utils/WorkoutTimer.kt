package com.fitgen.app.utils

import android.os.CountDownTimer

/**
 * Utility class for managing workout timers
 * Supports countdown timers for workouts and rest periods
 */
class WorkoutTimer(
    private val durationMillis: Long,
    private val tickIntervalMillis: Long = 1000L,
    private val onTick: (millisUntilFinished: Long) -> Unit,
    private val onFinish: () -> Unit
) {
    private var timer: CountDownTimer? = null
    private var isRunning = false
    private var remainingTime: Long = durationMillis

    /**
     * Start the timer
     */
    fun start() {
        if (isRunning) return
        
        timer = object : CountDownTimer(remainingTime, tickIntervalMillis) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                onTick(millisUntilFinished)
            }

            override fun onFinish() {
                isRunning = false
                remainingTime = durationMillis
                onFinish()
            }
        }.start()
        
        isRunning = true
    }

    /**
     * Pause the timer
     */
    fun pause() {
        timer?.cancel()
        isRunning = false
    }

    /**
     * Resume the timer from where it was paused
     */
    fun resume() {
        if (isRunning) return
        start()
    }

    /**
     * Stop and reset the timer
     */
    fun stop() {
        timer?.cancel()
        isRunning = false
        remainingTime = durationMillis
    }

    /**
     * Cancel the timer
     */
    fun cancel() {
        timer?.cancel()
        isRunning = false
    }

    /**
     * Check if timer is currently running
     */
    fun isRunning(): Boolean = isRunning

    /**
     * Get remaining time in milliseconds
     */
    fun getRemainingTime(): Long = remainingTime

    /**
     * Format milliseconds to MM:SS string
     */
    companion object {
        fun formatTime(millis: Long): String {
            val seconds = (millis / 1000) % 60
            val minutes = (millis / 1000) / 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        /**
         * Format milliseconds to HH:MM:SS string
         */
        fun formatTimeWithHours(millis: Long): String {
            val seconds = (millis / 1000) % 60
            val minutes = (millis / 1000 / 60) % 60
            val hours = (millis / 1000 / 60 / 60)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}
