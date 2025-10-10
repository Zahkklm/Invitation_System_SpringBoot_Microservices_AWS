package com.digitopia.gateway.config

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired

@Component
class JwtUtil @Autowired constructor(
    private val jwtProcessor: ConfigurableJWTProcessor<SecurityContext>
) {
    fun validateToken(token: String): Boolean = try {
        val claims = getClaims(token)
        val exp = claims.expirationTime
        exp == null || exp.after(java.util.Date())
    } catch (ex: Exception) {
        false
    }

    fun extractUsername(token: String): String? = try {
        getClaims(token).subject
    } catch (ex: Exception) {
        null
    }

    fun extractRole(token: String): String? = try {
        val claims = getClaims(token)
        // Cognito roles/claims may be in 'cognito:groups', 'custom:role', or similar
        // Adjust as needed for your token structure
        claims.getStringClaim("custom:role")
            ?: claims.getStringClaim("role")
            ?: claims.getStringArrayClaim("cognito:groups")?.firstOrNull()
    } catch (ex: Exception) {
        null
    }

    fun extractEmail(token: String): String? = try {
        getClaims(token).getStringClaim("email")
    } catch (ex: Exception) {
        null
    }

    fun getClaims(token: String): JWTClaimsSet {
        val signedJWT = SignedJWT.parse(token)
        return jwtProcessor.process(signedJWT, null)
    }
}