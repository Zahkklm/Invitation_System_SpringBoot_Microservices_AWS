package com.digitopia.organization.service

import com.digitopia.common.exception.DuplicateResourceException
import com.digitopia.common.exception.ResourceNotFoundException
import com.digitopia.common.util.TextSanitizer
import com.digitopia.organization.client.UserResponse
import com.digitopia.organization.client.UserServiceClient
import com.digitopia.organization.dto.CreateOrganizationRequest
import com.digitopia.organization.dto.OrganizationResponse
import com.digitopia.organization.dto.UpdateOrganizationRequest
import com.digitopia.organization.model.Organization
import com.digitopia.organization.repository.OrganizationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val userServiceClient: UserServiceClient
) {

    @Transactional
    fun createOrganization(request: CreateOrganizationRequest, creatorId: UUID): OrganizationResponse {
        if (organizationRepository.existsByRegistryNumber(request.registryNumber)) {
            throw DuplicateResourceException("Organization with registry number ${request.registryNumber} already exists")
        }

        val organization = Organization(
            name = request.name,
            normalizedName = TextSanitizer.normalizeForSearch(request.name),
            registryNumber = request.registryNumber,
            contactEmail = TextSanitizer.sanitizeEmail(request.contactEmail),
            companySize = request.companySize,
            yearFounded = request.yearFounded,
            createdBy = creatorId,
            updatedBy = creatorId
        )

        return organizationRepository.save(organization).toResponse()
    }

    @Transactional
    fun updateOrganization(id: UUID, request: UpdateOrganizationRequest, updaterId: UUID): OrganizationResponse {
        val organization = organizationRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Organization not found") }

        request.name?.let {
            organization.name = it
            organization.normalizedName = TextSanitizer.normalizeForSearch(it)
        }

        request.contactEmail?.let {
            organization.contactEmail = TextSanitizer.sanitizeEmail(it)
        }

        request.companySize?.let {
            organization.companySize = it
        }

        organization.updatedBy = updaterId
        organization.updatedAt = LocalDateTime.now()

        return organizationRepository.save(organization).toResponse()
    }

    fun searchOrganizations(
        name: String?,
        yearFounded: Int?,
        companySize: Int?,
        pageable: Pageable
    ): Page<OrganizationResponse> {
        val normalizedName = name?.let { TextSanitizer.normalizeForSearch(it) }
        return organizationRepository.searchOrganizations(normalizedName, yearFounded, companySize, pageable)
            .map { it.toResponse() }
    }

    fun findByRegistryNumber(registryNumber: String): OrganizationResponse {
        return organizationRepository.findByRegistryNumber(registryNumber)
            .orElseThrow { ResourceNotFoundException("Organization not found") }
            .toResponse()
    }

    fun getUsersByOrganizationId(organizationId: UUID): List<UserResponse> {
        // Verify organization exists
        organizationRepository.findById(organizationId)
            .orElseThrow { ResourceNotFoundException("Organization not found") }
        
        // Fetch users from User Service
        return userServiceClient.getUsersByOrganizationId(organizationId)
    }

    private fun Organization.toResponse() = OrganizationResponse(
        id = id.toString(),
        name = name,
        normalizedName = normalizedName,
        registryNumber = registryNumber,
        contactEmail = contactEmail,
        companySize = companySize,
        yearFounded = yearFounded,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}