package com.digitopia.auth.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CognitoConfig {
    @Value("\${aws.cognito.region}")
    private lateinit var region: String

    @Value("\${aws.cognito.userPoolId}")
    private lateinit var userPoolId: String

    @Value("\${aws.cognito.clientId}")
    private lateinit var clientId: String

    @Value("\${aws.cognito.clientSecret}")
    private lateinit var clientSecret: String

    @Value("\${aws.cognito.jwk}")
    private lateinit var jwkUrl: String

    @Bean
    fun cognitoClient(): AWSCognitoIdentityProvider {
        return AWSCognitoIdentityProviderClientBuilder.standard()
            .withRegion(region)
            .build()
    }

    // Required getters for other components
    fun getUserPoolId() = userPoolId
    fun getClientId() = clientId
    fun getClientSecret() = clientSecret
    fun getJwkUrl() = jwkUrl
}