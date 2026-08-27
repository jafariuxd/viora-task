package com.example.model

data class MessageEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val isError: Boolean = false
)
