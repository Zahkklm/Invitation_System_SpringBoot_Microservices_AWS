package com.digitopia.common.util

object TextSanitizer {
    /**
     * Sanitizes free text by removing HTML tags and normalizing whitespace
     * Used for invitation messages and other user-generated content
     */
    fun sanitize(text: String): String {
        return text.trim()
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
    }
    
    /**
     * Normalizes text for search operations
     * Converts to lowercase, removes special characters, keeps only alphanumeric
     */
    fun normalizeForSearch(text: String): String {
        return text.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "") // Remove special characters
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
    }
    
    /**
     * Sanitizes email addresses
     * Trims whitespace and converts to lowercase
     */
    fun sanitizeEmail(email: String): String {
        return email.trim().lowercase()
    }
}
