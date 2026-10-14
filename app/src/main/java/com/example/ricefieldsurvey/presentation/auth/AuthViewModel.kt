package com.example.ricefieldsurvey.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ricefieldsurvey.domain.repository.AuthRepository
import com.example.ricefieldsurvey.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUiState(val userId: String? = null, val error: String? = null)

class AuthViewModel(
    private val auth: AuthRepository,
    private val loginUseCase: LoginUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState(userId = auth.currentUserId()))
    val uiState = _uiState.asStateFlow()

    fun logIn(username: String, password: String) {
        if (loginUseCase(username, password)) {
            _uiState.value = AuthUiState(userId = username.trim())
        } else {
            _uiState.value = AuthUiState(error = "Invalid username or password.")
        }
    }

    fun logOut() {
        auth.logOut()
        _uiState.value = AuthUiState()
    }

    class Factory(
        private val auth: AuthRepository,
        private val login: LoginUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(auth, login) as T
    }
}
