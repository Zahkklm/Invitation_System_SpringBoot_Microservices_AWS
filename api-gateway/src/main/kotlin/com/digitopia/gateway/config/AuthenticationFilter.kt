package com.digitopia.gateway.config

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class AuthenticationFilter(
    private val jwtUtil: JwtUtil
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.path.value()

        // Allow public endpoints (actuator health check)
        if (path.startsWith("/actuator/health")) {
            return chain.filter(exchange)
        }

        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        if (authHeader?.startsWith("Bearer ") == true) {
            val token = authHeader.substring(7)

            if (jwtUtil.validateToken(token)) {
                val username = jwtUtil.extractUsername(token)
                val role = jwtUtil.extractRole(token)
                val email = jwtUtil.extractEmail(token)

                val auth = UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    if (role != null) listOf(SimpleGrantedAuthority("ROLE_$role")) else emptyList()
                )

                // Add user claims as custom headers for downstream services
                val mutatedRequest = exchange.request.mutate().apply {
                    if (username != null) header("X-User-Name", username)
                    if (role != null) header("X-User-Role", role)
                    if (email != null) header("X-User-Email", email)
                }.build()
                val mutatedExchange = exchange.mutate().request(mutatedRequest).build()

                return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
            }
        }

        return chain.filter(exchange)
    }
}