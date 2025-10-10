package com.digitopia.gateway.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.JWSKeySelector
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URL

@Configuration
class JwtProcessorConfig {

    @Value("\${aws.cognito.jwk-set-uri:https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json}")
    private lateinit var jwkSetUri: String

    @Bean
    fun jwtProcessor(): ConfigurableJWTProcessor<SecurityContext> {
        val jwtProcessor = DefaultJWTProcessor<SecurityContext>()
        
        // Configure the JWT processor with the JWK source
        val keySource: JWKSource<SecurityContext> = RemoteJWKSet(URL(jwkSetUri))
        val keySelector: JWSKeySelector<SecurityContext> = 
            JWSVerificationKeySelector(JWSAlgorithm.RS256, keySource)
        
        jwtProcessor.jwsKeySelector = keySelector
        
        return jwtProcessor
    }
}
