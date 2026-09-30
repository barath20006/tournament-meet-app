package com.sportmeet.app.presentation.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sportmeet.app.data.repository.AuthRepository
import com.sportmeet.app.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successRole: UserRole? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(emailOrPhone: String, password: String) {
        if (emailOrPhone.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter email/phone and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.login(emailOrPhone.trim(), password)
            if (result.isSuccess) {
                val auth = result.getOrNull()!!
                val role = try {
                    UserRole.valueOf(auth.role)
                } catch (e: Exception) {
                    UserRole.PLAYER
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successRole = role,
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Login failed"
                )
            }
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        role: UserRole
    ) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all required fields")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(errorMessage = "Passwords do not match")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.register(
                name = name.trim(),
                email = email.trim(),
                phone = phone.trim(),
                password = password,
                role = role
            )

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successRole = role,
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Registration failed"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(successRole = null)
    }
}
