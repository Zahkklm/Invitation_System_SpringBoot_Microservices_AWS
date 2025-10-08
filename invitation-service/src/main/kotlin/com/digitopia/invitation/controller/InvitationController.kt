package com.digitopia.invitation.controller

import com.digitopia.invitation.dto.CreateInvitationRequest
import com.digitopia.invitation.dto.InvitationResponse
import com.digitopia.invitation.dto.UpdateInvitationStatusRequest
import com.digitopia.invitation.service.InvitationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/invitations")
@Tag(name = "Invitation Management", description = "Endpoints for managing invitations")
class InvitationController(private val invitationService: InvitationService) {

    @Operation(summary = "Create a new invitation")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Invitation created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input or business rule violation")
    ])
    @PostMapping
    fun createInvitation(
        @Valid @RequestBody request: CreateInvitationRequest,
        @RequestHeader("X-User-Id") creatorId: UUID
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.ok(invitationService.createInvitation(request, creatorId))
    }

    @Operation(summary = "Update invitation status")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Invitation status updated successfully"),
        ApiResponse(responseCode = "404", description = "Invitation not found")
    ])
    @PutMapping("/{id}/status")
    fun updateInvitationStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateInvitationStatusRequest,
        @RequestHeader("X-User-Id") updaterId: UUID
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.ok(invitationService.updateInvitationStatus(id, request, updaterId))
    }

    @Operation(summary = "Get invitations by user")
    @GetMapping("/user/{userId}")
    fun getInvitationsByUser(
        @PathVariable userId: UUID
    ): ResponseEntity<List<InvitationResponse>> {
        return ResponseEntity.ok(invitationService.getInvitationsByUser(userId))
    }

    @Operation(summary = "Get invitations by organization")
    @GetMapping("/organization/{organizationId}")
    fun getInvitationsByOrganization(
        @PathVariable organizationId: UUID
    ): ResponseEntity<List<InvitationResponse>> {
        return ResponseEntity.ok(invitationService.getInvitationsByOrganization(organizationId))
    }

    @Operation(summary = "Health check endpoint")
    @GetMapping("/healtz")
    fun healthCheck() = ResponseEntity.ok(mapOf("status" to "UP"))
}