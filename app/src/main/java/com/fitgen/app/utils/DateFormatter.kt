package com.fitgen.app.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility object for date formatting and manipulation
 */
object DateFormatter {
    
    private const val DATE_FORMAT_DISPLAY = "MMM dd, yyyy"
    private const val DATE_FORMAT_STORAGE = "yyyy-MM-dd"
    private const val DATE_TIME_FORMAT = "MMM dd, yyyy HH:mm"
    
    /**
     * Format timestamp to display date string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string (e.g., "Jan 15, 2024")
     */
    fun formatDisplayDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Invalid date"
        }
    }
    
    /**
     * Format timestamp to storage date string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string (e.g., "2024-01-15")
     */
    fun formatStorageDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT_STORAGE, Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Invalid date"
        }
    }
    
    /**
     * Format storage date string to display format
     * @param storageDate Date string in storage format (yyyy-MM-dd)
     * @return Formatted date string for display
     */
    fun storageToDisplay(storageDate: String): String {
        return try {
            val storageSdf = SimpleDateFormat(DATE_FORMAT_STORAGE, Locale.getDefault())
            val displaySdf = SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault())
            val date = storageSdf.parse(storageDate)
            date?.let { displaySdf.format(it) } ?: storageDate
        } catch (e: Exception) {
            storageDate
        }
    }
    
    /**
     * Format timestamp to date and time string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date time string (e.g., "Jan 15, 2024 14:30")
     */
    fun formatDateTime(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Invalid date"
        }
    }
    
    /**
     * Get current date in storage format
     * @return Current date string (yyyy-MM-dd)
     */
    fun getCurrentDate(): String {
        return formatStorageDate(System.currentTimeMillis())
    }
    
    /**
     * Get current timestamp
     * @return Current timestamp in milliseconds
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
    
    /**
     * Calculate days between two timestamps
     * @param startTimestamp Start timestamp in milliseconds
     * @param endTimestamp End timestamp in milliseconds
     * @return Number of days between the two timestamps
     */
    fun daysBetween(startTimestamp: Long, endTimestamp: Long): Int {
        val diff = endTimestamp - startTimestamp
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
    
    /**
     * Check if a timestamp is today
     * @param timestamp Timestamp to check
     * @return true if timestamp is today, false otherwise
     */
    fun isToday(timestamp: Long): Boolean {
        val today = formatStorageDate(System.currentTimeMillis())
        val dateToCheck = formatStorageDate(timestamp)
        return today == dateToCheck
    }
    
    /**
     * Get relative time string (e.g., "Today", "Yesterday", "2 days ago")
     * @param timestamp Timestamp in milliseconds
     * @return Relative time string
     */
    fun getRelativeTimeString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val days = daysBetween(timestamp, now)
        
        return when {
            days == 0 -> "Today"
            days == 1 -> "Yesterday"
            days < 7 -> "$days days ago"
            days < 30 -> "${days / 7} week${if (days / 7 > 1) "s" else ""} ago"
            days < 365 -> "${days / 30} month${if (days / 30 > 1) "s" else ""} ago"
            else -> "${days / 365} year${if (days / 365 > 1) "s" else ""} ago"
        }
    }
}
