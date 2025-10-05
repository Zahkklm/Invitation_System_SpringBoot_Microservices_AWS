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
class UserController(private val userService: UserService) {

    @PostMapping
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest,
        @RequestHeader("X-User-Id") creatorId: UUID
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.createUser(request, creatorId))
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserRequest,
        @RequestHeader("X-User-Id") updaterId: UUID
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.updateUser(id, request, updaterId))
    }

    @GetMapping("/search")
    fun searchUsers(
        @RequestParam name: String,
        pageable: Pageable
    ): ResponseEntity<Page<UserResponse>> {
        return ResponseEntity.ok(userService.searchByNormalizedName(name, pageable))
    }

    @GetMapping("/email/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.findByEmail(email))
    }

    @GetMapping("/healtz")
    fun healthCheck() = ResponseEntity.ok(mapOf("status" to "UP"))
}