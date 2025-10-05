package com.digitopia.auth.model

enum class Role {
    ADMIN,
    MANAGER,
    USER
}

data class SignUpRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val role: Role = Role.USER
)

data class SignInRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val tokenType: String = "Bearer"
)