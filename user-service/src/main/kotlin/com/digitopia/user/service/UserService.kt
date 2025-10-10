package com.digitopia.user.service

import com.digitopia.common.events.UserCreatedEvent
import com.digitopia.common.events.UserUpdatedEvent
import com.digitopia.user.dto.CreateUserRequest
import com.digitopia.user.dto.UpdateUserRequest
import com.digitopia.user.dto.UserResponse
import com.digitopia.user.event.UserEventPublisher
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
class UserService(
    private val userRepository: UserRepository,
    private val eventPublisher: UserEventPublisher
) {

    @Transactional
    fun createUser(request: CreateUserRequest, creatorId: UUID): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        // Check if cognitoSub already exists (if provided)
        if (request.cognitoSub != null && userRepository.existsByCognitoSub(request.cognitoSub)) {
            throw IllegalArgumentException("User with this Cognito ID already exists")
        }

        val normalizedName = normalizeFullName(request.fullName)
        
        val status = when (request.role) {
            Role.ADMIN -> UserStatus.ACTIVE
            Role.MANAGER -> UserStatus.PENDING
            Role.USER -> UserStatus.PENDING
        }

        val user = User(
            cognitoSub = request.cognitoSub,
            email = request.email,
            fullName = request.fullName,
            normalizedName = normalizedName,
            status = status,
            role = request.role,
            createdBy = creatorId,
            updatedBy = creatorId
        )

        val savedUser = userRepository.save(user)
        
        // Publish event for audit trail and other services
        eventPublisher.publishUserCreated(
            UserCreatedEvent(
                userId = savedUser.id,
                email = savedUser.email,
                fullName = savedUser.fullName,
                role = savedUser.role.name,
                createdBy = creatorId
            )
        )

        return savedUser.toResponse()
    }

    @Transactional
    fun updateUser(id: UUID, request: UpdateUserRequest, updaterId: UUID): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("User not found") }

        val updatedFields = mutableMapOf<String, Any>()

        request.fullName?.let {
            user.fullName = it
            user.normalizedName = normalizeFullName(it)
            updatedFields["fullName"] = it
            updatedFields["normalizedName"] = user.normalizedName
        }

        request.status?.let {
            user.status = it
            updatedFields["status"] = it.name
        }

        user.updatedAt = LocalDateTime.now()
        user.updatedBy = updaterId

        val savedUser = userRepository.save(user)
        
        // Publish event if there were changes
        if (updatedFields.isNotEmpty()) {
            eventPublisher.publishUserUpdated(
                UserUpdatedEvent(
                    userId = id,
                    updatedFields = updatedFields,
                    updatedBy = updaterId
                )
            )
        }

        return savedUser.toResponse()
    }

    fun getUserOrganizations(userId: UUID): Set<UUID> {
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User not found") }
        return user.organizationIds
    }

    fun findById(id: UUID): UserResponse {
        return userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("User not found") }
            .toResponse()
    }

    fun findByOrganizationId(organizationId: UUID): List<UserResponse> {
        return userRepository.findByOrganizationId(organizationId)
            .map { it.toResponse() }
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
        cognitoSub = cognitoSub,
        email = email,
        fullName = fullName,
        normalizedName = normalizedName,
        status = status,
        role = role,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}