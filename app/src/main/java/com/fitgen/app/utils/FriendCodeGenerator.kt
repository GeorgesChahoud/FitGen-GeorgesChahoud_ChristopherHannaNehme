package com.fitgen.app.utils

import com.fitgen.app.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

object FriendCodeGenerator {
    
    private const val CODE_CHARS_PER_PART = 4
    private const val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    
    /**
     * Generate a unique friend code in format: XXXX-YYYY
     * Example: F4K9-T2X7
     */
    suspend fun generateUniqueFriendCode(): String {
        val firestore = FirebaseFirestore.getInstance()
        var attempts = 0
        val maxAttempts = 20  // Increased attempts
        
        while (attempts < maxAttempts) {
            val code = generateRandomCode()
            
            // Check if code already exists
            val exists = firestore.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("friendCode", code)
                .limit(1)
                .get()
                .await()
                .isEmpty.not()
            
            if (!exists) {
                return code
            }
            
            attempts++
        }
        
        // If all attempts fail (extremely unlikely with 36^8 = 2.8 trillion combinations)
        // throw an exception rather than risk returning a duplicate
        throw Exception("Failed to generate unique friend code after $maxAttempts attempts")
    }
    
    /**
     * Generate random code in format XXXX-YYYY
     */
    private fun generateRandomCode(): String {
        val part1 = generateRandomString(CODE_CHARS_PER_PART)
        val part2 = generateRandomString(CODE_CHARS_PER_PART)
        return "$part1-$part2"
    }
    
    /**
     * Generate random alphanumeric string
     */
    private fun generateRandomString(length: Int): String {
        return (1..length)
            .map { CHARS[Random.nextInt(CHARS.length)] }
            .joinToString("")
    }
    
    /**
     * Validate friend code format
     */
    fun isValidFormat(code: String): Boolean {
        val regex = Regex("^[A-Z0-9]{4}-[A-Z0-9]{4}$")
        return code.matches(regex)
    }
}
