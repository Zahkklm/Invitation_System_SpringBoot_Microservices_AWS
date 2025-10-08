package com.digitopia.organization.model

import com.digitopia.common.entity.BaseEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "organizations",
    indexes = [
        Index(name = "idx_organization_normalized_name", columnList = "normalized_name"),
        Index(name = "idx_organization_year_founded", columnList = "year_founded"),
        Index(name = "idx_organization_company_size", columnList = "company_size")
    ]
)
class Organization(
    @Column(nullable = false)
    var name: String,

    @Column(name = "normalized_name", nullable = false)
    var normalizedName: String,

    @Column(name = "registry_number", nullable = false, unique = true)
    var registryNumber: String,

    @Column(name = "contact_email", nullable = false)
    var contactEmail: String,

    @Column(name = "company_size", nullable = false)
    var companySize: Int,

    @Column(name = "year_founded", nullable = false)
    var yearFounded: Int,

    createdBy: UUID,
    updatedBy: UUID
) : BaseEntity(createdBy = createdBy, updatedBy = updatedBy)