package com.example.network.inspector

data class ApiLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val method: String,
    val url: String,
    val requestHeaders: Map<String, String>,
    val requestBody: String?,
    var statusCode: Int? = null,
    var statusMessage: String? = null,
    var responseHeaders: Map<String, String>? = null,
    var responseBody: String? = null,
    var durationMs: Long? = null,
    var error: String? = null,
    var isPending: Boolean = true
)
