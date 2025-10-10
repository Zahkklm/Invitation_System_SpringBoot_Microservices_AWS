package com.digitopia.organization.controller

import com.digitopia.organization.client.UserResponse
import com.digitopia.organization.dto.CreateOrganizationRequest
import com.digitopia.organization.dto.OrganizationResponse
import com.digitopia.organization.dto.UpdateOrganizationRequest
import com.digitopia.organization.service.OrganizationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organization Management", description = "Endpoints for managing organizations")
class OrganizationController(private val organizationService: OrganizationService) {

    @Operation(summary = "Create a new organization")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Organization created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
        ApiResponse(responseCode = "409", description = "Organization with registry number already exists")
    ])
    @PostMapping
    fun createOrganization(
        @Valid @RequestBody request: CreateOrganizationRequest,
        @RequestHeader("X-User-Id") creatorId: UUID
    ): ResponseEntity<OrganizationResponse> {
        return ResponseEntity.ok(organizationService.createOrganization(request, creatorId))
    }

    @Operation(summary = "Update an organization")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Organization updated successfully"),
        ApiResponse(responseCode = "404", description = "Organization not found")
    ])
    @PutMapping("/{id}")
    fun updateOrganization(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateOrganizationRequest,
        @RequestHeader("X-User-Id") updaterId: UUID
    ): ResponseEntity<OrganizationResponse> {
        return ResponseEntity.ok(organizationService.updateOrganization(id, request, updaterId))
    }

    @Operation(summary = "Search organizations")
    @GetMapping("/search")
    fun searchOrganizations(
        @Parameter(description = "Organization name to search for")
        @RequestParam(required = false) name: String?,
        
        @Parameter(description = "Year the organization was founded")
        @RequestParam(required = false) yearFounded: Int?,
        
        @Parameter(description = "Size of the company")
        @RequestParam(required = false) companySize: Int?,
        
        pageable: Pageable
    ): ResponseEntity<Page<OrganizationResponse>> {
        return ResponseEntity.ok(
            organizationService.searchOrganizations(name, yearFounded, companySize, pageable)
        )
    }

    @Operation(summary = "Find organization by registry number")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Organization found"),
        ApiResponse(responseCode = "404", description = "Organization not found")
    ])
    @GetMapping("/registry/{registryNumber}")
    fun getByRegistryNumber(
        @Parameter(description = "Registry number of the organization")
        @PathVariable registryNumber: String
    ): ResponseEntity<OrganizationResponse> {
        return ResponseEntity.ok(organizationService.findByRegistryNumber(registryNumber))
    }

    @Operation(summary = "Get all users belonging to an organization")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        ApiResponse(responseCode = "404", description = "Organization not found")
    ])
    @GetMapping("/{id}/users")
    fun getUsersByOrganizationId(
        @Parameter(description = "Organization ID")
        @PathVariable id: UUID
    ): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(organizationService.getUsersByOrganizationId(id))
    }
}