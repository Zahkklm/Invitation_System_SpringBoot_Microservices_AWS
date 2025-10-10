package com.digitopia.user.repository

import com.digitopia.user.model.Role
import com.digitopia.user.model.User
import com.digitopia.user.model.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import java.util.*

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User
    private val testCreatorId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        testUser = User(
            cognitoSub = "cognito-test-123",
            email = "test@example.com",
            fullName = "Test User",
            normalizedName = "test-user",
            status = UserStatus.ACTIVE,
            role = Role.USER,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )
    }

    @Test
    fun `should save and find user by id`() {
        // Given
        val savedUser = entityManager.persistAndFlush(testUser)

        // When
        val foundUser = userRepository.findById(savedUser.id)

        // Then
        assertThat(foundUser).isPresent
        assertThat(foundUser.get().email).isEqualTo("test@example.com")
        assertThat(foundUser.get().fullName).isEqualTo("Test User")
    }

    @Test
    fun `should find user by email`() {
        // Given
        entityManager.persistAndFlush(testUser)

        // When
        val foundUser = userRepository.findByEmail("test@example.com")

        // Then
        assertThat(foundUser).isPresent
        assertThat(foundUser.get().fullName).isEqualTo("Test User")
    }

    @Test
    fun `should return empty when user email not found`() {
        // When
        val foundUser = userRepository.findByEmail("nonexistent@example.com")

        // Then
        assertThat(foundUser).isEmpty
    }

    @Test
    fun `should find user by cognito sub`() {
        // Given
        entityManager.persistAndFlush(testUser)

        // When
        val foundUser = userRepository.findByCognitoSub("cognito-test-123")

        // Then
        assertThat(foundUser).isPresent
        assertThat(foundUser.get().email).isEqualTo("test@example.com")
    }

    @Test
    fun `should check if email exists`() {
        // Given
        entityManager.persistAndFlush(testUser)

        // When & Then
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse
    }

    @Test
    fun `should check if cognito sub exists`() {
        // Given
        entityManager.persistAndFlush(testUser)

        // When & Then
        assertThat(userRepository.existsByCognitoSub("cognito-test-123")).isTrue
        assertThat(userRepository.existsByCognitoSub("nonexistent-sub")).isFalse
    }

    @Test
    fun `should find users by normalized name containing`() {
        // Given
        val user1 = User(
            cognitoSub = "cognito-john-doe",
            email = "john@example.com",
            fullName = "John Doe",
            normalizedName = "john-doe",
            status = UserStatus.ACTIVE,
            role = Role.USER,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        val user2 = User(
            cognitoSub = "cognito-john-smith",
            email = "johnsmith@example.com",
            fullName = "John Smith",
            normalizedName = "john-smith",
            status = UserStatus.ACTIVE,
            role = Role.USER,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        entityManager.persist(user1)
        entityManager.persist(user2)
        entityManager.flush()

        // When
        val result = userRepository.findByNormalizedNameContaining("john", PageRequest.of(0, 10))

        // Then
        assertThat(result.content).hasSize(2)
        assertThat(result.content.map { it.normalizedName }).containsExactlyInAnyOrder("john-doe", "john-smith")
    }

    @Test
    fun `should find users by organization id`() {
        // Given
        val orgId = UUID.randomUUID()
        testUser.organizationIds.add(orgId)
        entityManager.persistAndFlush(testUser)

        // When
        val users = userRepository.findByOrganizationId(orgId)

        // Then
        assertThat(users).hasSize(1)
        assertThat(users[0].email).isEqualTo("test@example.com")
        assertThat(users[0].organizationIds).contains(orgId)
    }

    @Test
    fun `should enforce unique email constraint`() {
        // Given
        entityManager.persistAndFlush(testUser)

        val duplicateUser = User(
            cognitoSub = "different-cognito-id",
            email = "test@example.com", // Same email
            fullName = "Another User",
            normalizedName = "another-user",
            status = UserStatus.ACTIVE,
            role = Role.USER,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        // When & Then
        org.junit.jupiter.api.assertThrows<Exception> {
            entityManager.persistAndFlush(duplicateUser)
        }
    }

    @Test
    fun `should enforce unique cognito sub constraint`() {
        // Given
        entityManager.persistAndFlush(testUser)

        val duplicateUser = User(
            cognitoSub = "cognito-test-123", // Same cognito sub
            email = "different@example.com",
            fullName = "Another User",
            normalizedName = "another-user",
            status = UserStatus.ACTIVE,
            role = Role.USER,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        // When & Then
        org.junit.jupiter.api.assertThrows<Exception> {
            entityManager.persistAndFlush(duplicateUser)
        }
    }
}
