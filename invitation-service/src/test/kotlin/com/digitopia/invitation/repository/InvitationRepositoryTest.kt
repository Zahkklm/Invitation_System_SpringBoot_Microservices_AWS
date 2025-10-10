package com.digitopia.invitation.repository

import com.digitopia.invitation.model.Invitation
import com.digitopia.invitation.model.InvitationStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.time.LocalDateTime
import java.util.*

@DataJpaTest
class InvitationRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var invitationRepository: InvitationRepository

    private lateinit var testInvitation: Invitation
    private val testUserId = UUID.randomUUID()
    private val testOrgId = UUID.randomUUID()
    private val testCreatorId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        testInvitation = Invitation(
            userId = testUserId,
            organizationId = testOrgId,
            message = "Join our organization!",
            status = InvitationStatus.PENDING,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )
    }

    @Test
    fun `should save and find invitation by id`() {
        // Given
        val savedInvitation = entityManager.persistAndFlush(testInvitation)

        // When
        val foundInvitation = invitationRepository.findById(savedInvitation.id!!)

        // Then
        assertThat(foundInvitation).isPresent
        assertThat(foundInvitation.get().message).isEqualTo("Join our organization!")
        assertThat(foundInvitation.get().status).isEqualTo(InvitationStatus.PENDING)
    }

    @Test
    fun `should find invitation by user id and organization id and status`() {
        // Given
        entityManager.persistAndFlush(testInvitation)

        // When
        val foundInvitation = invitationRepository.findByUserIdAndOrganizationIdAndStatus(
            testUserId,
            testOrgId,
            InvitationStatus.PENDING
        )

        // Then
        assertThat(foundInvitation).isPresent
        assertThat(foundInvitation.get().message).isEqualTo("Join our organization!")
    }

    @Test
    fun `should find invitations by user id`() {
        // Given
        entityManager.persistAndFlush(testInvitation)

        val anotherInvitation = Invitation(
            userId = testUserId,
            organizationId = UUID.randomUUID(),
            message = "Another invitation",
            status = InvitationStatus.ACCEPTED,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )
        entityManager.persist(anotherInvitation)
        entityManager.flush()

        // When
        val invitations = invitationRepository.findByUserId(testUserId)

        // Then
        assertThat(invitations).hasSize(2)
        assertThat(invitations.map { it.userId }).allMatch { it == testUserId }
    }

    @Test
    fun `should find invitations by organization id`() {
        // Given
        entityManager.persistAndFlush(testInvitation)

        // When
        val invitations = invitationRepository.findByOrganizationId(testOrgId)

        // Then
        assertThat(invitations).hasSize(1)
        assertThat(invitations[0].organizationId).isEqualTo(testOrgId)
    }

    @Test
    fun `should find invitations by user and organization ordered by created at desc`() {
        // Given
        val oldInvitation = Invitation(
            userId = testUserId,
            organizationId = testOrgId,
            message = "Old invitation",
            status = InvitationStatus.EXPIRED,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )
        entityManager.persist(oldInvitation)
        Thread.sleep(100) // Ensure different timestamps

        val newInvitation = Invitation(
            userId = testUserId,
            organizationId = testOrgId,
            message = "New invitation",
            status = InvitationStatus.ACCEPTED,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )
        entityManager.persist(newInvitation)
        entityManager.flush()

        // When
        val invitations = invitationRepository.findByUserIdAndOrganizationIdOrderByCreatedAtDesc(
            testUserId,
            testOrgId
        )

        // Then
        assertThat(invitations).hasSize(2)
        assertThat(invitations[0].status).isEqualTo(InvitationStatus.ACCEPTED)
        assertThat(invitations[1].status).isEqualTo(InvitationStatus.EXPIRED)
    }

    @Test
    fun `should find pending invitations created before specific date`() {
        // Given
        val oldInvitation = Invitation(
            userId = testUserId,
            organizationId = testOrgId,
            message = "Old invitation",
            status = InvitationStatus.PENDING,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )
        oldInvitation.createdAt = LocalDateTime.now().minusDays(10)
        entityManager.persist(oldInvitation)

        val recentInvitation = Invitation(
            userId = UUID.randomUUID(),
            organizationId = testOrgId,
            message = "Recent invitation",
            status = InvitationStatus.PENDING,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )
        recentInvitation.createdAt = LocalDateTime.now().minusDays(3)
        entityManager.persist(recentInvitation)
        entityManager.flush()

        // When
        val expiredInvitations = invitationRepository.findByStatusAndCreatedAtBefore(
            InvitationStatus.PENDING,
            LocalDateTime.now().minusDays(7)
        )

        // Then
        assertThat(expiredInvitations).hasSize(1)
        assertThat(expiredInvitations[0].createdAt).isBefore(LocalDateTime.now().minusDays(7))
    }

    @Test
    fun `should enforce unique constraint for pending invitations`() {
        // Given
        entityManager.persistAndFlush(testInvitation)

        val duplicateInvitation = Invitation(
            userId = testUserId,
            organizationId = testOrgId,
            message = "Another invitation",
            status = InvitationStatus.PENDING, // Same status
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        // When & Then
        org.junit.jupiter.api.assertThrows<Exception> {
            entityManager.persistAndFlush(duplicateInvitation)
        }
    }

    @Test
    fun `should allow multiple invitations with different statuses`() {
        // Given
        entityManager.persistAndFlush(testInvitation)

        val acceptedInvitation = Invitation(
            userId = testUserId,
            organizationId = testOrgId,
            message = "Another invitation",
            status = InvitationStatus.ACCEPTED, // Different status
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        // When & Then - Should not throw exception
        val saved = entityManager.persistAndFlush(acceptedInvitation)
        assertThat(saved.id).isNotNull
    }

    @Test
    fun `should check if invitation is expired`() {
        // Given
        val oldInvitation = Invitation(
            userId = testUserId,
            organizationId = testOrgId,
            message = "Old invitation",
            status = InvitationStatus.PENDING,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )
        oldInvitation.createdAt = LocalDateTime.now().minusDays(8)
        val saved = entityManager.persistAndFlush(oldInvitation)

        // When
        val isExpired = saved.isExpired()

        // Then
        assertThat(isExpired).isTrue
    }

    @Test
    fun `should check if invitation is not expired`() {
        // Given
        val recentInvitation = Invitation(
            userId = testUserId,
            organizationId = testOrgId,
            message = "Recent invitation",
            status = InvitationStatus.PENDING,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )
        recentInvitation.createdAt = LocalDateTime.now().minusDays(5)
        val saved = entityManager.persistAndFlush(recentInvitation)

        // When
        val isExpired = saved.isExpired()

        // Then
        assertThat(isExpired).isFalse
    }
}
