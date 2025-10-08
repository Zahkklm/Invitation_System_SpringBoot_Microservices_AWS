package com.digitopia.invitation.service

import com.digitopia.common.exception.DuplicateResourceException
import com.digitopia.common.exception.ResourceNotFoundException
import com.digitopia.invitation.dto.CreateInvitationRequest
import com.digitopia.invitation.dto.InvitationResponse
import com.digitopia.invitation.dto.UpdateInvitationStatusRequest
import com.digitopia.invitation.model.Invitation
import com.digitopia.invitation.model.InvitationStatus
import com.digitopia.invitation.repository.InvitationRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class InvitationService(private val invitationRepository: InvitationRepository) {

    @Transactional
    fun createInvitation(request: CreateInvitationRequest, creatorId: UUID): InvitationResponse {
        // Only one pending invitation per user per org
        if (invitationRepository.findByUserIdAndOrganizationIdAndStatus(request.userId, request.organizationId, InvitationStatus.PENDING).isPresent) {
            throw DuplicateResourceException("A pending invitation already exists for this user and organization")
        }
        // Cannot reinvite if last invitation was rejected
        val lastInvitations = invitationRepository.findByUserIdAndOrganizationIdOrderByCreatedAtDesc(request.userId, request.organizationId)
        if (lastInvitations.firstOrNull()?.status == InvitationStatus.REJECTED) {
            throw DuplicateResourceException("Cannot reinvite: last invitation was rejected")
        }
        val invitation = Invitation(
            userId = request.userId,
            organizationId = request.organizationId,
            message = request.message,
            status = InvitationStatus.PENDING,
            createdBy = creatorId,
            updatedBy = creatorId
        )
        return invitationRepository.save(invitation).toResponse()
    }

    @Transactional
    fun updateInvitationStatus(id: UUID, request: UpdateInvitationStatusRequest, updaterId: UUID): InvitationResponse {
        val invitation = invitationRepository.findById(id).orElseThrow { ResourceNotFoundException("Invitation not found") }
        invitation.status = request.status
        invitation.updatedBy = updaterId
        invitation.updatedAt = LocalDateTime.now()
        return invitationRepository.save(invitation).toResponse()
    }

    fun getInvitationsByUser(userId: UUID): List<InvitationResponse> =
        invitationRepository.findByUserId(userId).map { it.toResponse() }

    fun getInvitationsByOrganization(organizationId: UUID): List<InvitationResponse> =
        invitationRepository.findByOrganizationId(organizationId).map { it.toResponse() }

    // Scheduled job to expire invitations older than 7 days
    @Scheduled(cron = "0 0 2 * * *") // Every day at 2am
    @Transactional
    fun expireOldInvitations() {
        val expired = invitationRepository.findByStatusAndCreatedAtBefore(
            InvitationStatus.PENDING,
            LocalDateTime.now().minusDays(7)
        )
        expired.forEach {
            it.status = InvitationStatus.EXPIRED
            it.updatedAt = LocalDateTime.now()
        }
        invitationRepository.saveAll(expired)
    }

    private fun Invitation.toResponse() = InvitationResponse(
        id = id.toString(),
        userId = userId.toString(),
        organizationId = organizationId.toString(),
        message = message,
        status = status,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}