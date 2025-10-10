package com.digitopia.organization.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.*

@Component
class UserServiceClient(
    private val restTemplate: RestTemplate,
    @Value("\${user-service.url:http://user-service:8084}") private val userServiceUrl: String
) {
    fun getUsersByOrganizationId(organizationId: UUID): List<UserResponse> {
        val url = "$userServiceUrl/api/v1/users/organization/$organizationId"
        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<UserResponse>>() {}
        )
        return response.body ?: emptyList()
    }
}

data class UserResponse(
    val id: String,
    val email: String,
    val fullName: String,
    val normalizedName: String,
    val status: String,
    val role: String,
    val createdAt: String,
    val updatedAt: String
)
