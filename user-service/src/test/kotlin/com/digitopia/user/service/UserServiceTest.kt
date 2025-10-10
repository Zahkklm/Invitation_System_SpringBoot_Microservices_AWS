package com.digitopia.user.service

import com.digitopia.user.dto.CreateUserRequest
import com.digitopia.user.dto.UpdateUserRequest
import com.digitopia.user.event.UserEventPublisher
import com.digitopia.user.model.Role
import com.digitopia.user.model.User
import com.digitopia.user.model.UserStatus
import com.digitopia.user.repository.UserRepository
import io.mockk.*
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var eventPublisher: UserEventPublisher
    private lateinit var userService: UserService

    private val testUserId = UUID.randomUUID()
    private val testCreatorId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        eventPublisher = mockk(relaxed = true)
        userService = UserService(userRepository, eventPublisher)
    }

    @Test
    fun `should create user successfully`() {
        // Given
        val request = CreateUserRequest(
            email = "test@example.com",
            fullName = "Test User",
            role = Role.USER,
            cognitoSub = "cognito-123"
        )

        val savedUser = User(
            id = testUserId,
            cognitoSub = "cognito-123",
            email = "test@example.com",
            fullName = "Test User",
            normalizedName = "test-user",
            status = UserStatus.PENDING,
            role = Role.USER,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        every { userRepository.existsByEmail(any()) } returns false
        every { userRepository.existsByCognitoSub(any()) } returns false
        every { userRepository.save(any()) } returns savedUser

        // When
        val result = userService.createUser(request, testCreatorId)

        // Then
        assertThat(result.email).isEqualTo("test@example.com")
        assertThat(result.fullName).isEqualTo("Test User")
        assertThat(result.status).isEqualTo(UserStatus.PENDING)
        verify { userRepository.save(any()) }
        verify { eventPublisher.publishUserCreated(any()) }
    }

    @Test
    fun `should throw exception when email already exists`() {
        // Given
        val request = CreateUserRequest(
            email = "test@example.com",
            fullName = "Test User",
            role = Role.USER
        )

        every { userRepository.existsByEmail("test@example.com") } returns true

        // When & Then
        assertThrows<IllegalArgumentException> {
            userService.createUser(request, testCreatorId)
        }

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should create ACTIVE user for ADMIN role`() {
        // Given
        val request = CreateUserRequest(
            email = "admin@example.com",
            fullName = "Admin User",
            role = Role.ADMIN
        )

        val savedUser = User(
            id = testUserId,
            cognitoSub = null,
            email = "admin@example.com",
            fullName = "Admin User",
            normalizedName = "admin-user",
            status = UserStatus.ACTIVE,
            role = Role.ADMIN,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        every { userRepository.existsByEmail(any()) } returns false
        every { userRepository.save(any()) } returns savedUser

        // When
        val result = userService.createUser(request, testCreatorId)

        // Then
        assertThat(result.status).isEqualTo(UserStatus.ACTIVE)
        assertThat(result.role).isEqualTo(Role.ADMIN)
    }

    @Test
    fun `should update user successfully`() {
        // Given
        val existingUser = User(
            id = testUserId,
            cognitoSub = null,
            email = "test@example.com",
            fullName = "Old Name",
            normalizedName = "old-name",
            status = UserStatus.ACTIVE,
            role = Role.USER,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        val updateRequest = UpdateUserRequest(
            fullName = "New Name",
            status = UserStatus.DEACTIVATED
        )

        every { userRepository.findById(testUserId) } returns Optional.of(existingUser)
        every { userRepository.save(any()) } returns existingUser

        // When
        val result = userService.updateUser(testUserId, updateRequest, testCreatorId)

        // Then
        assertThat(result.fullName).isEqualTo("New Name")
        verify { userRepository.save(any()) }
        verify { eventPublisher.publishUserUpdated(any()) }
    }

    @Test
    fun `should throw exception when updating non-existent user`() {
        // Given
        val updateRequest = UpdateUserRequest(
            fullName = "New Name",
            status = null
        )
        every { userRepository.findById(testUserId) } returns Optional.empty()

        // When & Then
        assertThrows<EntityNotFoundException> {
            userService.updateUser(testUserId, updateRequest, testCreatorId)
        }
    }

    @Test
    fun `should find user by id`() {
        // Given
        val user = User(
            id = testUserId,
            cognitoSub = null,
            email = "test@example.com",
            fullName = "Test User",
            normalizedName = "test-user",
            status = UserStatus.ACTIVE,
            role = Role.USER,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        every { userRepository.findById(testUserId) } returns Optional.of(user)

        // When
        val result = userService.findById(testUserId)

        // Then
        assertThat(result.id).isEqualTo(testUserId.toString())
        assertThat(result.email).isEqualTo("test@example.com")
    }

    @Test
    fun `should find user by email`() {
        // Given
        val user = User(
            id = testUserId,
            cognitoSub = null,
            email = "test@example.com",
            fullName = "Test User",
            normalizedName = "test-user",
            status = UserStatus.ACTIVE,
            role = Role.USER,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        every { userRepository.findByEmail("test@example.com") } returns Optional.of(user)

        // When
        val result = userService.findByEmail("test@example.com")

        // Then
        assertThat(result.email).isEqualTo("test@example.com")
    }

    @Test
    fun `should search users by normalized name`() {
        // Given
        val users = listOf(
            User(
                id = UUID.randomUUID(),
                cognitoSub = null,
                email = "john1@example.com",
                fullName = "John Doe",
                normalizedName = "john-doe",
                status = UserStatus.ACTIVE,
                role = Role.USER,
                createdBy = testCreatorId,
                updatedBy = testCreatorId
            )
        )

        val page = PageImpl(users)
        every { userRepository.findByNormalizedNameContaining(any(), any()) } returns page

        // When
        val result = userService.searchByNormalizedName("john", PageRequest.of(0, 10))

        // Then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].normalizedName).contains("john")
    }

    @Test
    fun `should get user organizations`() {
        // Given
        val orgId1 = UUID.randomUUID()
        val orgId2 = UUID.randomUUID()
        val user = User(
            id = testUserId,
            cognitoSub = null,
            email = "test@example.com",
            fullName = "Test User",
            normalizedName = "test-user",
            status = UserStatus.ACTIVE,
            role = Role.USER,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )
        user.organizationIds.add(orgId1)
        user.organizationIds.add(orgId2)

        every { userRepository.findById(testUserId) } returns Optional.of(user)

        // When
        val result = userService.getUserOrganizations(testUserId)

        // Then
        assertThat(result).hasSize(2)
        assertThat(result).containsExactlyInAnyOrder(orgId1, orgId2)
    }

    @Test
    fun `should soft delete user`() {
        // Given
        val user = User(
            id = testUserId,
            cognitoSub = null,
            email = "test@example.com",
            fullName = "Test User",
            normalizedName = "test-user",
            status = UserStatus.ACTIVE,
            role = Role.USER,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        every { userRepository.findById(testUserId) } returns Optional.of(user)
        every { userRepository.save(any()) } returns user

        // When
        userService.deleteUser(testUserId, testCreatorId)

        // Then
        verify { userRepository.save(match { it.status == UserStatus.DELETED }) }
    }

    @Test
    fun `should find users by organization id`() {
        // Given
        val orgId = UUID.randomUUID()
        val users = listOf(
            User(
                id = testUserId,
                cognitoSub = null,
                email = "test@example.com",
                fullName = "Test User",
                normalizedName = "test-user",
                status = UserStatus.ACTIVE,
                role = Role.USER,
                createdBy = testCreatorId,
                updatedBy = testCreatorId
            )
        )

        every { userRepository.findByOrganizationId(orgId) } returns users

        // When
        val result = userService.findByOrganizationId(orgId)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].email).isEqualTo("test@example.com")
    }
}
