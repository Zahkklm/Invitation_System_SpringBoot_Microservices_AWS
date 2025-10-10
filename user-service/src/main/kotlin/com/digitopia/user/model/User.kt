package com.digitopia.user.model

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_email", columnList = "email", unique = true),
        Index(name = "idx_user_normalized_name", columnList = "normalizedName"),
        Index(name = "idx_user_cognito_sub", columnList = "cognitoSub", unique = true)
    ]
)
class User(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = true, unique = true)
    var cognitoSub: String? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: UserStatus,

    @Column(nullable = false)
    var fullName: String,

    @Column(nullable = false)
    var normalizedName: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var role: Role,

    @ElementCollection
    @CollectionTable(name = "user_organizations", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "organization_id")
    var organizationIds: MutableSet<UUID> = mutableSetOf(),

    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: UUID,
    var updatedBy: UUID
)

enum class UserStatus {
    ACTIVE, PENDING, DEACTIVATED, DELETED
}

enum class Role {
    ADMIN, MANAGER, USER
}