package com.digitopia.common.config

import org.slf4j.MDC
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID

@Configuration
class LoggingConfig {
    class RequestLoggingFilter : OncePerRequestFilter() {
        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
        ) {
            try {
                val correlationId = request.getHeader("X-Correlation-ID") ?: UUID.randomUUID().toString()
                MDC.put("correlationId", correlationId)
                MDC.put("requestUri", request.requestURI)
                MDC.put("method", request.method)
                
                response.addHeader("X-Correlation-ID", correlationId)
                
                filterChain.doFilter(request, response)
            } finally {
                MDC.clear()
            }
        }
    }
}