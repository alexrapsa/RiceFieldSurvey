package com.example.ricefieldsurvey.domain.usecase

import com.example.ricefieldsurvey.domain.repository.AuthRepository

class LoginUseCase(private val auth: AuthRepository) {
    operator fun invoke(username: String, password: String): Boolean =
        auth.logIn(username.trim(), password)
}
