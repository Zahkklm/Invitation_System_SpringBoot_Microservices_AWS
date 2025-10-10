package com.digitopia.invitation.event

import com.digitopia.common.events.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

/**
 * Event publisher for Invitation Service domain events
 * Publishes events to Kafka topics for event-driven communication
 */
@Component
class InvitationEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(InvitationEventPublisher::class.java)

    companion object {
        const val INVITATION_EVENTS_TOPIC = "invitation-events"
    }

    fun publishInvitationCreated(event: InvitationCreatedEvent) {
        try {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(INVITATION_EVENTS_TOPIC, event.invitationId.toString(), message)
            logger.info("Published InvitationCreatedEvent: invitationId=${event.invitationId}")
        } catch (e: Exception) {
            logger.error("Failed to publish InvitationCreatedEvent", e)
        }
    }

    fun publishInvitationAccepted(event: InvitationAcceptedEvent) {
        try {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(INVITATION_EVENTS_TOPIC, event.invitationId.toString(), message)
            logger.info("Published InvitationAcceptedEvent: invitationId=${event.invitationId}, userId=${event.userId}, orgId=${event.organizationId}")
        } catch (e: Exception) {
            logger.error("Failed to publish InvitationAcceptedEvent", e)
        }
    }

    fun publishInvitationRejected(event: InvitationRejectedEvent) {
        try {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(INVITATION_EVENTS_TOPIC, event.invitationId.toString(), message)
            logger.info("Published InvitationRejectedEvent: invitationId=${event.invitationId}")
        } catch (e: Exception) {
            logger.error("Failed to publish InvitationRejectedEvent", e)
        }
    }

    fun publishInvitationExpired(event: InvitationExpiredEvent) {
        try {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(INVITATION_EVENTS_TOPIC, event.invitationId.toString(), message)
            logger.info("Published InvitationExpiredEvent: invitationId=${event.invitationId}")
        } catch (e: Exception) {
            logger.error("Failed to publish InvitationExpiredEvent", e)
        }
    }
}
