package com.fitgen.app.utils

import com.google.android.material.textfield.TextInputLayout

/**
 * Extension functions for better input validation
 */

/**
 * Validate that TextInputLayout is not empty
 * @param errorMessage Error message to display if empty
 * @return true if valid, false if empty
 */
fun TextInputLayout.validateNotEmpty(errorMessage: String = "This field is required"): Boolean {
    val text = editText?.text?.toString()?.trim() ?: ""
    return if (text.isEmpty()) {
        error = errorMessage
        false
    } else {
        error = null
        true
    }
}

/**
 * Validate that TextInputLayout contains a valid number
 * @param errorMessage Error message to display if invalid
 * @return true if valid number, false otherwise
 */
fun TextInputLayout.validateNumber(errorMessage: String = "Please enter a valid number"): Boolean {
    val text = editText?.text?.toString()?.trim() ?: ""
    val isValid = text.isNotEmpty() && text.toDoubleOrNull() != null
    
    return if (isValid) {
        error = null
        true
    } else {
        error = errorMessage
        false
    }
}

/**
 * Validate that TextInputLayout contains a positive number
 * @param errorMessage Error message to display if invalid
 * @return true if valid positive number, false otherwise
 */
fun TextInputLayout.validatePositiveNumber(
    errorMessage: String = "Please enter a positive number"
): Boolean {
    val text = editText?.text?.toString()?.trim() ?: ""
    val number = text.toDoubleOrNull()
    val isValid = number != null && number > 0
    
    return if (isValid) {
        error = null
        true
    } else {
        error = errorMessage
        false
    }
}

/**
 * Validate that TextInputLayout contains a valid email
 * @param errorMessage Error message to display if invalid
 * @return true if valid email, false otherwise
 */
fun TextInputLayout.validateEmail(errorMessage: String = "Please enter a valid email"): Boolean {
    val text = editText?.text?.toString()?.trim() ?: ""
    val emailPattern = android.util.Patterns.EMAIL_ADDRESS
    val isValid = text.isNotEmpty() && emailPattern.matcher(text).matches()
    
    return if (isValid) {
        error = null
        true
    } else {
        error = errorMessage
        false
    }
}

/**
 * Validate that TextInputLayout meets minimum length requirement
 * @param minLength Minimum required length
 * @param errorMessage Error message to display if invalid
 * @return true if meets minimum length, false otherwise
 */
fun TextInputLayout.validateMinLength(
    minLength: Int,
    errorMessage: String = "Must be at least $minLength characters"
): Boolean {
    val text = editText?.text?.toString()?.trim() ?: ""
    val isValid = text.length >= minLength
    
    return if (isValid) {
        error = null
        true
    } else {
        error = errorMessage
        false
    }
}

/**
 * Validate that two TextInputLayouts match (useful for password confirmation)
 * @param other The other TextInputLayout to compare with
 * @param errorMessage Error message to display if they don't match
 * @return true if they match, false otherwise
 */
fun TextInputLayout.validateMatches(
    other: TextInputLayout,
    errorMessage: String = "Fields do not match"
): Boolean {
    val text1 = editText?.text?.toString()?.trim() ?: ""
    val text2 = other.editText?.text?.toString()?.trim() ?: ""
    val isValid = text1 == text2 && text1.isNotEmpty()
    
    return if (isValid) {
        error = null
        other.error = null
        true
    } else {
        error = errorMessage
        false
    }
}

/**
 * Clear error message from TextInputLayout
 */
fun TextInputLayout.clearError() {
    error = null
}

/**
 * Get trimmed text from TextInputLayout
 * @return Trimmed text or empty string if null
 */
fun TextInputLayout.getTrimmedText(): String {
    return editText?.text?.toString()?.trim() ?: ""
}
