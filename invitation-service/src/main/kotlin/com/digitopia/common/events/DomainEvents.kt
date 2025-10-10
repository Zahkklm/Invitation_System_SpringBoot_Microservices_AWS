package com.digitopia.common.events

import java.time.LocalDateTime
import java.util.UUID

/**
 * Base event class for all domain events in the system
 */
abstract class DomainEvent(
    open val eventId: UUID = UUID.randomUUID(),
    open val timestamp: LocalDateTime = LocalDateTime.now(),
    open val eventType: String
)

/**
 * Invitation Events
 */
data class InvitationCreatedEvent(
    val invitationId: UUID,
    val userId: UUID,
    val organizationId: UUID,
    val message: String,
    val createdBy: UUID,
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : DomainEvent(eventId, timestamp, "InvitationCreated")

data class InvitationAcceptedEvent(
    val invitationId: UUID,
    val userId: UUID,
    val organizationId: UUID,
    val acceptedBy: UUID,
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : DomainEvent(eventId, timestamp, "InvitationAccepted")

data class InvitationRejectedEvent(
    val invitationId: UUID,
    val userId: UUID,
    val organizationId: UUID,
    val rejectedBy: UUID,
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : DomainEvent(eventId, timestamp, "InvitationRejected")

data class InvitationExpiredEvent(
    val invitationId: UUID,
    val userId: UUID,
    val organizationId: UUID,
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : DomainEvent(eventId, timestamp, "InvitationExpired")
