package com.example.network.inspector

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import java.io.EOFException
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
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            ApiInspectorManager.onNetworkError(
                logId = apiLog.id,
                errorMessage = e.message ?: e.javaClass.simpleName,
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

        if (body.isOneShot()) {
            return "(One-shot request body omitted)"
        }
        if (body.isDuplex()) {
            return "(Duplex request body omitted)"
        }

        val mediaType = body.contentType()
        if (mediaType != null) {
            val type = mediaType.type.lowercase()
            val subtype = mediaType.subtype.lowercase()
            if (type == "multipart" || subtype.contains("octet-stream") || type == "image" || type == "video" || type == "audio") {
                val length = try { body.contentLength() } catch (_: Exception) { -1L }
                return "($type/$subtype binary/multipart data, ${if (length >= 0) "$length bytes" else "size unknown"})"
            }
        }

        return try {
            val buffer = Buffer()
            body.writeTo(buffer)

            val maxBytes = 128 * 1024L // 128 KB max to prevent OOM
            val byteCount = minOf(buffer.size, maxBytes)
            val isTruncated = buffer.size > maxBytes

            if (isPlaintext(buffer)) {
                val charset: Charset = mediaType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                val content = buffer.readString(byteCount, charset)
                if (isTruncated) "$content\n\n... (truncated, total body exceeds 128KB)" else content
            } else {
                "(Binary ${buffer.size}-byte request body omitted)"
            }
        } catch (e: Exception) {
            "Unable to read request body: ${e.message}"
        }
    }

    private fun getResponseBodyString(response: Response): String? {
        val responseBody = response.body ?: return null
        val mediaType = responseBody.contentType()

        if (mediaType != null) {
            val type = mediaType.type.lowercase()
            val subtype = mediaType.subtype.lowercase()
            if (subtype.contains("octet-stream") || type == "image" || type == "video" || type == "audio") {
                return "($type/$subtype binary data omitted)"
            }
        }

        return try {
            val source = responseBody.source()
            val maxBytes = 128 * 1024L // 128 KB max
            source.request(maxBytes)
            val buffer = source.buffer.clone()

            if (isPlaintext(buffer)) {
                val byteCount = minOf(buffer.size, maxBytes)
                val isTruncated = source.buffer.size > maxBytes || !source.exhausted()
                val charset: Charset = mediaType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                val content = buffer.readString(byteCount, charset)
                if (isTruncated) "$content\n\n... (truncated, total body exceeds 128KB)" else content
            } else {
                "(Binary ${buffer.size}-byte response body omitted)"
            }
        } catch (e: Exception) {
            "Unable to read response body: ${e.message}"
        }
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        return try {
            val prefix = Buffer()
            val byteCount = minOf(buffer.size, 64L)
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0 until 16) {
                if (prefix.exhausted()) break
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            true
        } catch (_: EOFException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun formatJsonIfPossible(rawString: String?): String? {
        if (rawString.isNullOrEmpty()) return rawString
        if (rawString.length > 30_000) return rawString
        val trimmed = rawString.trim()
        return try {
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                JSONObject(trimmed).toString(2)
            } else if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                JSONArray(trimmed).toString(2)
            } else {
                rawString
            }
        } catch (_: Exception) {
            rawString
        }
    }
}
