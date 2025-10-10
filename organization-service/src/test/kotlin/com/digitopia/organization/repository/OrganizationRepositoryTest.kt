package com.digitopia.organization.repository

import com.digitopia.organization.model.Organization
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import java.util.*

@DataJpaTest
class OrganizationRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var organizationRepository: OrganizationRepository

    private lateinit var testOrganization: Organization
    private val testCreatorId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        testOrganization = Organization(
            name = "Test Organization",
            normalizedName = "test-organization",
            registryNumber = "REG123",
            contactEmail = "contact@testorg.com",
            companySize = 50,
            yearFounded = 2020,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )
    }

    @Test
    fun `should save and find organization by id`() {
        // Given
        val savedOrg = entityManager.persistAndFlush(testOrganization)

        // When
        val foundOrg = organizationRepository.findById(savedOrg.id!!)

        // Then
        assertThat(foundOrg).isPresent
        assertThat(foundOrg.get().name).isEqualTo("Test Organization")
        assertThat(foundOrg.get().registryNumber).isEqualTo("REG123")
    }

    @Test
    fun `should find organization by registry number`() {
        // Given
        entityManager.persistAndFlush(testOrganization)

        // When
        val foundOrg = organizationRepository.findByRegistryNumber("REG123")

        // Then
        assertThat(foundOrg).isPresent
        assertThat(foundOrg.get().name).isEqualTo("Test Organization")
    }

    @Test
    fun `should check if registry number exists`() {
        // Given
        entityManager.persistAndFlush(testOrganization)

        // When & Then
        assertThat(organizationRepository.existsByRegistryNumber("REG123")).isTrue
        assertThat(organizationRepository.existsByRegistryNumber("NONEXISTENT")).isFalse
    }

    @Test
    fun `should find organizations by normalized name containing`() {
        // Given
        val org1 = Organization(
            name = "Tech Company",
            normalizedName = "tech-company",
            registryNumber = "TECH001",
            contactEmail = "tech@company.com",
            companySize = 100,
            yearFounded = 2020,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        val org2 = Organization(
            name = "Tech Solutions",
            normalizedName = "tech-solutions",
            registryNumber = "TECH002",
            contactEmail = "info@techsolutions.com",
            companySize = 150,
            yearFounded = 2021,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        entityManager.persist(org1)
        entityManager.persist(org2)
        entityManager.flush()

        // When
        val result = organizationRepository.findByNormalizedNameContaining("tech", PageRequest.of(0, 10))

        // Then
        assertThat(result.content).hasSize(2)
        assertThat(result.content.map { it.normalizedName }).allMatch { it.contains("tech") }
    }

    @Test
    fun `should search organizations by name, year, and company size`() {
        // Given
        val org1 = Organization(
            name = "Startup Inc",
            normalizedName = "startup-inc",
            registryNumber = "START001",
            contactEmail = "info@startup.com",
            companySize = 10,
            yearFounded = 2022,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        val org2 = Organization(
            name = "Big Corp",
            normalizedName = "big-corp",
            registryNumber = "BIG001",
            contactEmail = "contact@bigcorp.com",
            companySize = 500,
            yearFounded = 2022,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        entityManager.persist(org1)
        entityManager.persist(org2)
        entityManager.flush()

        // When - search by year founded
        val resultByYear = organizationRepository.searchOrganizations(null, 2022, null, PageRequest.of(0, 10))

        // Then
        assertThat(resultByYear.content).hasSize(2)
        assertThat(resultByYear.content.map { it.yearFounded }).allMatch { it == 2022 }
    }

    @Test
    fun `should enforce unique registry number constraint`() {
        // Given
        entityManager.persistAndFlush(testOrganization)

        val duplicateOrg = Organization(
            name = "Another Organization",
            normalizedName = "another-organization",
            registryNumber = "REG123", // Same registry number
            contactEmail = "another@org.com",
            companySize = 100,
            yearFounded = 2021,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        // When & Then
        org.junit.jupiter.api.assertThrows<Exception> {
            entityManager.persistAndFlush(duplicateOrg)
        }
    }

    @Test
    fun `should search organizations with multiple criteria`() {
        // Given
        val org1 = Organization(
            name = "Tech Startup",
            normalizedName = "tech-startup",
            registryNumber = "TECH001",
            contactEmail = "contact@techstartup.com",
            companySize = 25,
            yearFounded = 2020,
            createdBy = testCreatorId,
            updatedBy = testCreatorId
        )

        entityManager.persistAndFlush(org1)

        // When - search by name and year
        val result = organizationRepository.searchOrganizations("tech", 2020, null, PageRequest.of(0, 10))

        // Then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].normalizedName).contains("tech")
        assertThat(result.content[0].yearFounded).isEqualTo(2020)
    }

    @Test
    fun `should delete organization`() {
        // Given
        val savedOrg = entityManager.persistAndFlush(testOrganization)
        val orgId = savedOrg.id!!

        // When
        organizationRepository.deleteById(orgId)
        entityManager.flush()

        // Then
        val foundOrg = organizationRepository.findById(orgId)
        assertThat(foundOrg).isEmpty
    }
}
