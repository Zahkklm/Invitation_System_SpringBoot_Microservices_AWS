package com.digitopia.invitation.dto

import com.digitopia.invitation.model.InvitationStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

// For creating a new invitation

data class CreateInvitationRequest(
    @field:NotNull(message = "User ID is required")
    val userId: UUID,
    @field:NotNull(message = "Organization ID is required")
    val organizationId: UUID,
    @field:NotBlank(message = "Invitation message is required")
    val message: String
)

data class UpdateInvitationStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: InvitationStatus
)

data class InvitationResponse(
    val id: String,
    val userId: String,
    val organizationId: String,
    val message: String,
    val status: InvitationStatus,
    val createdAt: String,
    val updatedAt: String
)