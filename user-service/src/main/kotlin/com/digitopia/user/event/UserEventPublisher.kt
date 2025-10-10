package com.digitopia.user.event

import com.digitopia.common.events.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

/**
 * Event publisher for User Service domain events
 * Publishes events to Kafka topics for event-driven communication
 */
@Component
class UserEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(UserEventPublisher::class.java)

    companion object {
        const val USER_EVENTS_TOPIC = "user-events"
        const val INVITATION_EVENTS_TOPIC = "invitation-events"
    }

    fun publishUserCreated(event: UserCreatedEvent) {
        try {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(USER_EVENTS_TOPIC, event.userId.toString(), message)
            logger.info("Published UserCreatedEvent: userId=${event.userId}")
        } catch (e: Exception) {
            logger.error("Failed to publish UserCreatedEvent: userId=${event.userId}", e)
        }
    }

    fun publishUserUpdated(event: UserUpdatedEvent) {
        try {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(USER_EVENTS_TOPIC, event.userId.toString(), message)
            logger.info("Published UserUpdatedEvent: userId=${event.userId}")
        } catch (e: Exception) {
            logger.error("Failed to publish UserUpdatedEvent: userId=${event.userId}", e)
        }
    }

    fun publishUserOrganizationAdded(event: UserOrganizationAddedEvent) {
        try {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(USER_EVENTS_TOPIC, event.userId.toString(), message)
            logger.info("Published UserOrganizationAddedEvent: userId=${event.userId}, orgId=${event.organizationId}")
        } catch (e: Exception) {
            logger.error("Failed to publish UserOrganizationAddedEvent", e)
        }
    }
}
