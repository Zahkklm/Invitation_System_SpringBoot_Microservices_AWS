package com.digitopia.user.event

import com.digitopia.common.events.InvitationAcceptedEvent
import com.digitopia.user.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Event listener for User Service
 * Listens to invitation events and updates user-organization relationships
 * 
 * This implements Event-Driven Architecture for cross-service communication
 */
@Component
class UserEventListener(
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper,
    private val eventPublisher: UserEventPublisher
) {
    private val logger = LoggerFactory.getLogger(UserEventListener::class.java)

    /**
     * When an invitation is accepted, add the organization to user's organization list
     * This ensures eventual consistency between Invitation and User services
     */
    @KafkaListener(
        topics = ["invitation-events"],
        groupId = "user-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    fun handleInvitationAccepted(message: String) {
        try {
            val event = objectMapper.readValue(message, InvitationAcceptedEvent::class.java)
            
            logger.info("Received InvitationAcceptedEvent: userId=${event.userId}, orgId=${event.organizationId}")
            
            val user = userRepository.findById(event.userId)
            if (user.isPresent) {
                val userEntity = user.get()
                
                // Add organization to user's organization set if not already present
                if (userEntity.organizationIds.add(event.organizationId)) {
                    userEntity.updatedBy = event.acceptedBy
                    userRepository.save(userEntity)
                    
                    logger.info("Added organization ${event.organizationId} to user ${event.userId}")
                    
                    // Publish event for audit trail
                    eventPublisher.publishUserOrganizationAdded(
                        com.digitopia.common.events.UserOrganizationAddedEvent(
                            userId = event.userId,
                            organizationId = event.organizationId,
                            addedBy = event.acceptedBy
                        )
                    )
                } else {
                    logger.info("User ${event.userId} already belongs to organization ${event.organizationId}")
                }
            } else {
                logger.warn("User not found: userId=${event.userId}")
            }
        } catch (e: Exception) {
            logger.error("Failed to process InvitationAcceptedEvent", e)
            // In production, you might want to send this to a dead-letter queue
        }
    }
}
