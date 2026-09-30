package com.sportmeet.app.data.remote.dto

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String
)

data class LoginRequest(
    val emailOrPhone: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)
