package com.digitopia.user.service

import com.digitopia.user.dto.CreateUserRequest
import com.digitopia.user.dto.UpdateUserRequest
import com.digitopia.user.dto.UserResponse
import com.digitopia.user.model.Role
import com.digitopia.user.model.User
import com.digitopia.user.model.UserStatus
import com.digitopia.user.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional
    fun createUser(request: CreateUserRequest, creatorId: UUID): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        val normalizedName = normalizeFullName(request.fullName)
        
        val status = when (request.role) {
            Role.ADMIN -> UserStatus.ACTIVE
            Role.MANAGER -> UserStatus.PENDING
            Role.USER -> UserStatus.PENDING
        }

        val user = User(
            email = request.email,
            fullName = request.fullName,
            normalizedName = normalizedName,
            status = status,
            role = request.role,
            createdBy = creatorId,
            updatedBy = creatorId
        )

        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun updateUser(id: UUID, request: UpdateUserRequest, updaterId: UUID): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("User not found") }

        request.fullName?.let {
            user.fullName = it
            user.normalizedName = normalizeFullName(it)
        }

        request.status?.let {
            user.status = it
        }

        user.updatedAt = LocalDateTime.now()
        user.updatedBy = updaterId

        return userRepository.save(user).toResponse()
    }

    fun searchByNormalizedName(name: String, pageable: Pageable): Page<UserResponse> {
        return userRepository.findByNormalizedNameContaining(
            normalizeFullName(name),
            pageable
        ).map { it.toResponse() }
    }

    fun findByEmail(email: String): UserResponse {
        return userRepository.findByEmail(email)
            .orElseThrow { EntityNotFoundException("User not found") }
            .toResponse()
    }

    private fun normalizeFullName(fullName: String): String {
        return fullName.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .trim()
            .replace(Regex("\\s+"), "-")
    }

    private fun User.toResponse() = UserResponse(
        id = id.toString(),
        email = email,
        fullName = fullName,
        normalizedName = normalizedName,
        status = status,
        role = role,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}