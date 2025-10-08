package com.digitopia.user.controller

import com.digitopia.user.dto.CreateUserRequest
import com.digitopia.user.dto.UpdateUserRequest
import com.digitopia.user.dto.UserResponse
import com.digitopia.user.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Endpoints for managing users")
class UserController(private val userService: UserService) {

    @Operation(
        summary = "Create a new user",
        description = "Creates a new user with the provided details"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "User created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
        ApiResponse(responseCode = "409", description = "User with email already exists")
    ])
    @PostMapping
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest,
        @RequestHeader("X-User-Id") creatorId: UUID
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.createUser(request, creatorId))
    }

    @Operation(
        summary = "Update user details",
        description = "Updates an existing user's information"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "User updated successfully"),
        ApiResponse(responseCode = "404", description = "User not found")
    ])
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserRequest,
        @RequestHeader("X-User-Id") updaterId: UUID
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.updateUser(id, request, updaterId))
    }

    @Operation(
        summary = "Search users by name",
        description = "Search users by their normalized name with pagination"
    )
    @GetMapping("/search")
    fun searchUsers(
        @Parameter(description = "Name to search for")
        @RequestParam name: String,
        @Parameter(description = "Pagination parameters")
        pageable: Pageable
    ): ResponseEntity<Page<UserResponse>> {
        return ResponseEntity.ok(userService.searchByNormalizedName(name, pageable))
    }

    @Operation(
        summary = "Find user by email",
        description = "Retrieves a user by their email address"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(responseCode = "404", description = "User not found")
    ])
    @GetMapping("/email/{email}")
    fun getUserByEmail(
        @Parameter(description = "Email address of the user")
        @PathVariable email: String
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.findByEmail(email))
    }

    @Operation(
        summary = "Health check endpoint",
        description = "Returns the health status of the User Service"
    )
    @GetMapping("/healtz")
    fun healthCheck() = ResponseEntity.ok(mapOf("status" to "UP"))
}