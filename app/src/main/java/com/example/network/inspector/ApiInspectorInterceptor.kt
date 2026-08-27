package com.example.network.inspector

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class ApiInspectorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()

        val reqHeaders = headersToMap(request.headers)
        val reqBody = getRequestBodyString(request)

        val apiLog = ApiLog(
            method = request.method,
            url = request.url.toString(),
            requestHeaders = reqHeaders,
            requestBody = formatJsonIfPossible(reqBody)
        )

        ApiInspectorManager.onNetworkRequestStart(apiLog)

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            val duration = System.currentTimeMillis() - startTime
            ApiInspectorManager.onNetworkError(
                logId = apiLog.id,
                errorMessage = e.message ?: "Network error / timeout",
                durationMs = duration
            )
            throw e
        }

        val duration = System.currentTimeMillis() - startTime
        val resHeaders = headersToMap(response.headers)
        val resBody = getResponseBodyString(response)

        ApiInspectorManager.onNetworkResponseReceived(
            logId = apiLog.id,
            statusCode = response.code,
            statusMessage = response.message,
            responseHeaders = resHeaders,
            responseBody = formatJsonIfPossible(resBody),
            durationMs = duration
        )

        return response
    }

    private fun headersToMap(headers: Headers): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (i in 0 until headers.size) {
            map[headers.name(i)] = headers.value(i)
        }
        return map
    }

    private fun getRequestBodyString(request: Request): String? {
        val body = request.body ?: return null
        return try {
            val buffer = Buffer()
            body.writeTo(buffer)
            val contentType = body.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            buffer.readString(charset)
        } catch (e: Exception) {
            "Unable to read request body: ${e.message}"
        }
    }

    private fun getResponseBodyString(response: Response): String? {
        val responseBody = response.body ?: return null
        return try {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer whole body
            val buffer = source.buffer.clone()
            val contentType = responseBody.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            buffer.readString(charset)
        } catch (e: Exception) {
            "Unable to read response body: ${e.message}"
        }
    }

    private fun formatJsonIfPossible(rawString: String?): String? {
        if (rawString.isNullOrEmpty()) return rawString
        val trimmed = rawString.trim()
        return try {
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                JSONObject(trimmed).toString(2)
            } else if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                JSONArray(trimmed).toString(2)
            } else {
                rawString
            }
        } catch (e: Exception) {
            rawString
        }
    }
}
