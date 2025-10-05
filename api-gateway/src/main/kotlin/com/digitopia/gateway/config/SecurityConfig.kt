package com.digitopia.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        authenticationFilter: AuthenticationFilter
    ): SecurityWebFilterChain = http
        .csrf { it.disable() }
        .addFilterAt(authenticationFilter, SecurityWebFilterChain::class.java)
        .authorizeExchange {
            it.pathMatchers("/api/v1/auth/**", "/healtz").permitAll()
            it.anyExchange().authenticated()
        }
        .build()
}