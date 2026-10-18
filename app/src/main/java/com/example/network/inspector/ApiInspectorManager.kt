package com.example.network.inspector

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

object ApiInspectorManager {
    private val _logs = MutableStateFlow<List<ApiLog>>(emptyList())
    val logs: StateFlow<List<ApiLog>> = _logs.asStateFlow()

    private val _currentModalLog = MutableStateFlow<ApiLog?>(null)
    val currentModalLog: StateFlow<ApiLog?> = _currentModalLog.asStateFlow()

    private val _isAutoShowEnabled = MutableStateFlow(false)
    val isAutoShowEnabled: StateFlow<Boolean> = _isAutoShowEnabled.asStateFlow()

    private val _isInspectorOpen = MutableStateFlow(false)
    val isInspectorOpen: StateFlow<Boolean> = _isInspectorOpen.asStateFlow()

    private val activeLogsMap = ConcurrentHashMap<String, ApiLog>()

    fun setAutoShowEnabled(enabled: Boolean) {
        _isAutoShowEnabled.value = enabled
    }

    fun openInspector() {
        if (_currentModalLog.value == null && _logs.value.isNotEmpty()) {
            _currentModalLog.value = _logs.value.first()
        }
        _isInspectorOpen.value = true
    }

    fun onNetworkRequestStart(log: ApiLog) {
        activeLogsMap[log.id] = log
        _logs.value = listOf(log) + _logs.value.take(49)
        if (_isAutoShowEnabled.value) {
            _currentModalLog.value = log
            _isInspectorOpen.value = true
        }
    }

    fun onNetworkResponseReceived(
        logId: String,
        statusCode: Int,
        statusMessage: String,
        responseHeaders: Map<String, String>,
        responseBody: String?,
        durationMs: Long
    ) {
        val existing = activeLogsMap[logId]
        val updatedLog = (existing ?: ApiLog(
            id = logId,
            method = "UNKNOWN",
            url = "",
            requestHeaders = emptyMap(),
            requestBody = null
        )).copy(
            statusCode = statusCode,
            statusMessage = statusMessage,
            responseHeaders = responseHeaders,
            responseBody = responseBody,
            durationMs = durationMs,
            isPending = false,
            error = null
        )

        activeLogsMap[logId] = updatedLog
        _logs.value = _logs.value.map { if (it.id == logId) updatedLog else it }
        if (_currentModalLog.value?.id == logId) {
            _currentModalLog.value = updatedLog
        }
    }

    fun onNetworkError(logId: String, errorMessage: String, durationMs: Long) {
        val existing = activeLogsMap[logId]
        val updatedLog = (existing ?: ApiLog(
            id = logId,
            method = "UNKNOWN",
            url = "",
            requestHeaders = emptyMap(),
            requestBody = null
        )).copy(
            error = errorMessage,
            durationMs = durationMs,
            isPending = false
        )

        activeLogsMap[logId] = updatedLog
        _logs.value = _logs.value.map { if (it.id == logId) updatedLog else it }
        if (_currentModalLog.value?.id == logId) {
            _currentModalLog.value = updatedLog
        }
    }

    fun showLog(log: ApiLog) {
        _currentModalLog.value = log
        _isInspectorOpen.value = true
    }

    fun dismissModal() {
        _currentModalLog.value = null
        _isInspectorOpen.value = false
    }

    fun clearLogs() {
        activeLogsMap.clear()
        _logs.value = emptyList()
        _currentModalLog.value = null
        _isInspectorOpen.value = false
    }
}
