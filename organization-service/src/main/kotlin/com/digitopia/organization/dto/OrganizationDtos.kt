package com.digitopia.organization.dto

import jakarta.validation.constraints.*

data class CreateOrganizationRequest(
    @field:NotBlank(message = "Organization name is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Organization name must be alphanumeric")
    val name: String,

    @field:NotBlank(message = "Registry number is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Registry number must be alphanumeric")
    val registryNumber: String,

    @field:NotBlank(message = "Contact email is required")
    @field:Email(message = "Invalid email format")
    val contactEmail: String,

    @field:NotNull(message = "Company size is required")
    @field:Min(1, message = "Company size must be at least 1")
    val companySize: Int,

    @field:NotNull(message = "Year founded is required")
    @field:Min(1800, message = "Year founded must be after 1800")
    @field:Max(2100, message = "Year founded must be before 2100")
    val yearFounded: Int
)

data class UpdateOrganizationRequest(
    @field:Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Organization name must be alphanumeric")
    val name: String?,

    @field:Email(message = "Invalid email format")
    val contactEmail: String?,

    @field:Min(1, message = "Company size must be at least 1")
    val companySize: Int?
)

data class OrganizationResponse(
    val id: String,
    val name: String,
    val normalizedName: String,
    val registryNumber: String,
    val contactEmail: String,
    val companySize: Int,
    val yearFounded: Int,
    val createdAt: String,
    val updatedAt: String
)