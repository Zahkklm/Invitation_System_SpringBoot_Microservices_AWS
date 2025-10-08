package com.digitopia.organization.repository

import com.digitopia.organization.model.Organization
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrganizationRepository : JpaRepository<Organization, UUID> {
    fun findByRegistryNumber(registryNumber: String): Optional<Organization>
    
    fun findByNormalizedNameContaining(normalizedName: String, pageable: Pageable): Page<Organization>
    
    @Query("SELECT o FROM Organization o WHERE " +
           "(:normalizedName IS NULL OR o.normalizedName LIKE %:normalizedName%) AND " +
           "(:yearFounded IS NULL OR o.yearFounded = :yearFounded) AND " +
           "(:companySize IS NULL OR o.companySize = :companySize)")
    fun searchOrganizations(
        normalizedName: String?,
        yearFounded: Int?,
        companySize: Int?,
        pageable: Pageable
    ): Page<Organization>

    fun existsByRegistryNumber(registryNumber: String): Boolean
}