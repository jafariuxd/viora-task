package com.example.network.viora

import android.content.Context
import android.content.SharedPreferences

class TokenManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("viora_tokens", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)

    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    fun clearTokens() {
        prefs.edit().clear().apply()
        // Also clear user prefs so the app requires re-login
        val authPrefs = context.getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        authPrefs.edit().putBoolean("is_registered", false).apply()
    }
    
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}
