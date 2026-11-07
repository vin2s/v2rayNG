package com.v2ray.ang.dto

data class ConnectionLogEntry(
    val id: String = "",
    val atMillis: Long = 0L,
    val type: String = "",
    val fromName: String = "",
    val toName: String = "",
)
