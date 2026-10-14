package com.example.ricefieldsurvey.domain.repository

interface AuthRepository {
    fun currentUserId(): String?
    fun logIn(username: String, password: String): Boolean
    fun logOut()
}
