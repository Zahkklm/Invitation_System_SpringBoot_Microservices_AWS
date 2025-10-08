package com.digitopia.invitation.model

import com.digitopia.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "invitations",
    indexes = [
        Index(name = "idx_invitations_user_id", columnList = "user_id"),
        Index(name = "idx_invitations_organization_id", columnList = "organization_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_invitation_user_org_pending", columnNames = ["user_id", "organization_id", "status"] )
    ]
)
class Invitation(
    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID,

    @Column(nullable = false)
    var message: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: InvitationStatus,

    createdBy: UUID,
    updatedBy: UUID
) : BaseEntity(createdBy = createdBy, updatedBy = updatedBy) {
    fun isExpired(): Boolean = status == InvitationStatus.PENDING && createdAt.isBefore(LocalDateTime.now().minusDays(7))
}

enum class InvitationStatus {
    ACCEPTED, REJECTED, PENDING, EXPIRED
}