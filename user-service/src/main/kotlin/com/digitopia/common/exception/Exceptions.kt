package com.digitopia.common.exception

import java.time.LocalDateTime
import java.util.UUID

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val traceId: String = UUID.randomUUID().toString()
)

class ResourceNotFoundException(message: String) : RuntimeException(message)
class DuplicateResourceException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)
class UnauthorizedException(message: String) : RuntimeException(message)