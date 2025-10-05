package com.digitopia.gateway.filter

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtProcessor: ConfigurableJWTProcessor<SecurityContext>
) : AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config>(Config::class.java) {

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val token = exchange.request.headers["Authorization"]?.firstOrNull()?.removePrefix("Bearer ")
                ?: return@GatewayFilter Mono.error(UnauthorizedException("No token provided"))

            try {
                val claims = jwtProcessor.process(token, null)
                val role = claims.getClaim("custom:role") as? String
                    ?: return@GatewayFilter Mono.error(UnauthorizedException("No role found in token"))

                // Add claims to request headers for downstream services
                val request = exchange.request.mutate()
                    .header("X-User-Id", claims.subject)
                    .header("X-User-Role", role)
                    .build()

                chain.filter(exchange.mutate().request(request).build())
            } catch (e: Exception) {
                Mono.error(UnauthorizedException("Invalid token"))
            }
        }
    }

    class Config
}

class UnauthorizedException(message: String) : RuntimeException(message)