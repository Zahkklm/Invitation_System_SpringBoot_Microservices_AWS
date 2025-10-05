package com.digitopia.gateway.config

import com.digitopia.gateway.filter.JwtAuthenticationFilter
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod

@Configuration
class GatewayConfig(private val jwtAuthFilter: JwtAuthenticationFilter) {

    @Bean
    fun routeLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            // Public routes
            .route("auth-service") {
                it.path("/api/v1/auth/**")
                    .uri("lb://auth-service")
            }
            .route("health-check") {
                it.path("/healtz")
                    .uri("lb://api-gateway")
            }
            
            // Protected routes with role-based access
            .route("user-service") {
                it.path("/api/v1/users/**")
                    .and()
                    .method(HttpMethod.DELETE)
                    .and()
                    .header("X-User-Role", "ADMIN")
                    .filters(jwtAuthFilter.apply(JwtAuthenticationFilter.Config()))
                    .uri("lb://user-service")
            }
            .route("user-service-manager") {
                it.path("/api/v1/users/create")
                    .and()
                    .method(HttpMethod.POST)
                    .and()
                    .header("X-User-Role", listOf("ADMIN", "MANAGER"))
                    .filters(jwtAuthFilter.apply(JwtAuthenticationFilter.Config()))
                    .uri("lb://user-service")
            }
            .route("user-service-basic") {
                it.path("/api/v1/users/**")
                    .filters(jwtAuthFilter.apply(JwtAuthenticationFilter.Config()))
                    .uri("lb://user-service")
            }
            // Add similar routes for organization and invitation services
            .build()
    }
}