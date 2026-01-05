package com.fitgen.app.utils

import android.util.Log

object UsernameValidator {
    
    private const val MIN_LENGTH = 3
    private const val MAX_LENGTH = 20
    private val USERNAME_REGEX = "^[a-zA-Z0-9_]+$".toRegex()
    
    /**
     * Validate username format
     * Rules:
     * - 3-20 characters
     * - Only letters, numbers, and underscores
     * - No spaces or special characters
     */
    fun isValidFormat(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult(false, "Username cannot be empty")
        }
        
        if (username.length < MIN_LENGTH) {
            return ValidationResult(false, "Username must be at least $MIN_LENGTH characters")
        }
        
        if (username.length > MAX_LENGTH) {
            return ValidationResult(false, "Username must be at most $MAX_LENGTH characters")
        }
        
        if (!username.matches(USERNAME_REGEX)) {
            return ValidationResult(false, "Username can only contain letters, numbers, and underscores")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Format username (remove @ prefix if present, lowercase)
     */
    fun formatUsername(username: String): String {
        return try {
            username.trim()
                .removePrefix("@")
                .lowercase()
        } catch (e: Exception) {
            Log.e("UsernameValidator", "Error formatting username: ${e.message}", e)
            "" // Return empty string on any error
        }
    }
    
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String
    )
}
