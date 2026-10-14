package com.example.ricefieldsurvey.data.repository

import android.content.Context
import com.example.ricefieldsurvey.domain.repository.AuthRepository

/** Demo only. No backend, cryptographic password protection or secure identity is implied. */
class LocalAuthRepository(context: Context) : AuthRepository {
    private val preferences = context.getSharedPreferences("survey_demo_session", Context.MODE_PRIVATE)
    private val accounts = mapOf(
        "enumerator1" to "Field@123",
        "enumerator2" to "Survey@123",
    )

    override fun currentUserId(): String? = preferences.getString("user_id", null)
        ?.takeIf { it in accounts }

    override fun logIn(username: String, password: String): Boolean {
        if (accounts[username] != password) return false
        preferences.edit().putString("user_id", username).apply()
        return true
    }

    override fun logOut() {
        preferences.edit().remove("user_id").apply()
    }
}
