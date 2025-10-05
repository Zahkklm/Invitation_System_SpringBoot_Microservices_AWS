package com.digitopia.auth.service

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.model.*
import com.digitopia.auth.config.CognitoConfig
import com.digitopia.auth.model.AuthResponse
import com.digitopia.auth.model.Role
import com.digitopia.auth.model.SignInRequest
import com.digitopia.auth.model.SignUpRequest
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.util.Base64

@Service
class AuthService(
    private val cognitoClient: AWSCognitoIdentityProvider,
    private val cognitoConfig: CognitoConfig
) {
    fun signUp(request: SignUpRequest): String {
        val userAttributes = listOf(
            AttributeType()
                .withName("email")
                .withValue(request.email),
            AttributeType()
                .withName("custom:full_name")
                .withValue(request.fullName),
            AttributeType()
                .withName("custom:role")
                .withValue(request.role.name)
        )

        val signUpRequest = SignUpRequest()
            .withClientId(cognitoConfig.getClientId())
            .withSecretHash(calculateSecretHash(request.email))
            .withUsername(request.email)
            .withPassword(request.password)
            .withUserAttributes(userAttributes)

        val result = cognitoClient.signUp(signUpRequest)
        return result.userSub
    }

    fun signIn(request: SignInRequest): AuthResponse {
        val authParams = mapOf(
            "USERNAME" to request.email,
            "PASSWORD" to request.password,
            "SECRET_HASH" to calculateSecretHash(request.email)
        )

        val initiateAuthRequest = InitiateAuthRequest()
            .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
            .withClientId(cognitoConfig.getClientId())
            .withAuthParameters(authParams)

        val result = cognitoClient.initiateAuth(initiateAuthRequest)
        val authResult = result.authenticationResult

        return AuthResponse(
            accessToken = authResult.accessToken,
            refreshToken = authResult.refreshToken,
            expiresIn = authResult.expiresIn
        )
    }

    private fun calculateSecretHash(username: String): String {
        val message = username + cognitoConfig.getClientId()
        val secretKey = SecretKeySpec(
            cognitoConfig.getClientSecret().toByteArray(StandardCharsets.UTF_8),
            "HmacSHA256"
        )
        
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)
        val rawHmac = mac.doFinal(message.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(rawHmac)
    }
}