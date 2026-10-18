package com.example.network.inspector

data class ApiLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val method: String,
    val url: String,
    val requestHeaders: Map<String, String>,
    val requestBody: String?,
    val statusCode: Int? = null,
    val statusMessage: String? = null,
    val responseHeaders: Map<String, String>? = null,
    val responseBody: String? = null,
    val durationMs: Long? = null,
    val error: String? = null,
    val isPending: Boolean = true
)
