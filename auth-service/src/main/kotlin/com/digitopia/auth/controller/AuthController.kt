package com.digitopia.auth.controller

import com.digitopia.auth.model.AuthResponse
import com.digitopia.auth.model.SignInRequest
import com.digitopia.auth.model.SignUpRequest
import com.digitopia.auth.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/signup")
    fun signUp(@RequestBody request: SignUpRequest): ResponseEntity<Map<String, String>> {
        val userId = authService.signUp(request)
        return ResponseEntity.ok(mapOf("userId" to userId))
    }

    @PostMapping("/signin")
    fun signIn(@RequestBody request: SignInRequest): ResponseEntity<AuthResponse> {
        val response = authService.signIn(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/healtz")
    fun healthCheck() = ResponseEntity.ok(mapOf("status" to "UP"))
}