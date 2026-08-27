package com.example.network.viora

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

import com.example.network.inspector.ApiInspectorInterceptor

object VioraNetworkModule {
    private const val BASE_URL = "http://45.195.250.77:3000/api/"

    private var tokenManager: TokenManager? = null

    fun init(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }
    
    fun getTokenManager(): TokenManager? = tokenManager

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        tokenManager?.getAccessToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(TokenAuthenticator(tokenManager!!))
            .addInterceptor(ApiInspectorInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    val api: VioraApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(VioraApiService::class.java)
    }
}
