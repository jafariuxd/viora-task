package com.example.network.viora

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.model.viora.RefreshTokenDto

class TokenAuthenticator(
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.pathSegments.contains("refresh")) {
            return null // Do not retry if refresh failed
        }

        synchronized(this) {
            val refreshToken = tokenManager.getRefreshToken() ?: return null
            
            // Check if token was refreshed by another thread while we were waiting for the lock
            val currentAccessToken = tokenManager.getAccessToken()
            val requestHeader = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (currentAccessToken != null && currentAccessToken != requestHeader) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val retrofit = Retrofit.Builder()
                .baseUrl("http://45.195.250.77:3000/api/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            
            val api = retrofit.create(VioraApiService::class.java)

            val newAccessToken = runBlocking {
                try {
                    val res = api.refreshTokens(RefreshTokenDto(refreshToken))
                    if (res.success && res.data != null) {
                        tokenManager.saveTokens(res.data.accessToken, res.data.refreshToken)
                        res.data.accessToken
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            if (newAccessToken != null) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                tokenManager.clearTokens()
                return null
            }
        }
    }
}
