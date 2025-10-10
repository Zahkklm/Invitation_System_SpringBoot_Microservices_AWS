package com.digitopia.common.util

object TextSanitizer {
    fun sanitize(text: String): String {
        return text.trim()
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
    }
    
    fun normalizeForSearch(text: String): String {
        return text.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "") // Remove special characters
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
    }
    
    fun sanitizeEmail(email: String): String {
        return email.trim().lowercase()
    }
}
