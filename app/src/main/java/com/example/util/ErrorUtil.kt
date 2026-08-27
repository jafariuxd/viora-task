package com.example.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types
import retrofit2.HttpException

object ErrorUtil {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)

    fun getErrorMessage(e: Exception): String {
        if (e is HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                if (errorBody != null) {
                    val adapter = moshi.adapter<Map<String, Any>>(mapType)
                    val map = adapter.fromJson(errorBody)
                    val message = map?.get("message") as? String
                    if (message != null) {
                        return message
                    }
                }
            } catch (ex: Exception) {
                // ignore and fallback
            }
            return "Server Error (${e.code()})"
        }
        return e.localizedMessage ?: e.message ?: "Network Error"
    }
}
