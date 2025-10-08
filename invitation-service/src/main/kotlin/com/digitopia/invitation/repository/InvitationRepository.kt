package com.digitopia.invitation.repository

import com.digitopia.invitation.model.Invitation
import com.digitopia.invitation.model.InvitationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface InvitationRepository : JpaRepository<Invitation, UUID> {
    fun findByUserIdAndOrganizationIdAndStatus(userId: UUID, organizationId: UUID, status: InvitationStatus): Optional<Invitation>
    fun findByUserIdAndOrganizationIdOrderByCreatedAtDesc(userId: UUID, organizationId: UUID): List<Invitation>
    fun findByStatusAndCreatedAtBefore(status: InvitationStatus, before: LocalDateTime): List<Invitation>
    fun findByUserId(userId: UUID): List<Invitation>
    fun findByOrganizationId(organizationId: UUID): List<Invitation>
}