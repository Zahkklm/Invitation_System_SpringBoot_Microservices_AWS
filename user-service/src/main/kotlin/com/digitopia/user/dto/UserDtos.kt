package com.digitopia.user.dto

import com.digitopia.user.model.Role
import com.digitopia.user.model.UserStatus
import jakarta.validation.constraints.*

data class CreateUserRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Full name must contain only letters and spaces")
    @field:NotBlank(message = "Full name is required")
    val fullName: String,

    @field:NotNull(message = "Role is required")
    val role: Role
)

data class UpdateUserRequest(
    @field:Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Full name must contain only letters and spaces")
    val fullName: String?,

    val status: UserStatus?
)

data class UserResponse(
    val id: String,
    val email: String,
    val fullName: String,
    val normalizedName: String,
    val status: UserStatus,
    val role: Role,
    val createdAt: String,
    val updatedAt: String
)