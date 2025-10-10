package com.digitopia.user.repository

import com.digitopia.user.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
    fun findByNormalizedNameContaining(normalizedName: String, pageable: Pageable): Page<User>
    fun existsByEmail(email: String): Boolean
    fun existsByCognitoSub(cognitoSub: String): Boolean
    fun findByCognitoSub(cognitoSub: String): Optional<User>
}