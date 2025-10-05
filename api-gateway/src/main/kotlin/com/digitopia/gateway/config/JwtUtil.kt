package com.digitopia.gateway.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtUtil {
    @Value("\${jwt.secret}")
    private lateinit var secret: String

    fun validateToken(token: String): Boolean = !isTokenExpired(token)

    fun extractUsername(token: String): String = extractClaim(token) { obj: Claims -> obj.subject }

    fun extractRole(token: String): String = extractAllClaims(token).get("role", String::class.java)

    private fun extractExpiration(token: String): Date = extractClaim(token) { obj: Claims -> obj.expiration }

    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body

    private fun isTokenExpired(token: String): Boolean =
        extractExpiration(token).before(Date())

    private fun getSigningKey(): Key =
        Keys.hmacShaKeyFor(secret.toByteArray())
}