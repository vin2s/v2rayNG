package com.v2ray.ang.handler

import com.v2ray.ang.dto.ConnectionLogEntry
import com.v2ray.ang.util.JsonUtil
import java.util.UUID

internal object ConnectionLogEntries {
    const val MAX_SIZE = 200
    const val TYPE_START = "start"
    const val TYPE_STOP = "stop"
    const val TYPE_FAILOVER = "failover"
    const val TYPE_AUTO_SELECT = "auto_select"

    fun start(serverName: String, atMillis: Long): ConnectionLogEntry =
        ConnectionLogEntry(
            id = UUID.randomUUID().toString(),
            atMillis = atMillis,
            type = TYPE_START,
            toName = serverName,
        )

    fun stop(serverName: String, atMillis: Long): ConnectionLogEntry =
        ConnectionLogEntry(
            id = UUID.randomUUID().toString(),
            atMillis = atMillis,
            type = TYPE_STOP,
            fromName = serverName,
        )

    fun failover(fromName: String, toName: String, atMillis: Long): ConnectionLogEntry =
        ConnectionLogEntry(
            id = UUID.randomUUID().toString(),
            atMillis = atMillis,
            type = TYPE_FAILOVER,
            fromName = fromName,
            toName = toName,
        )

    fun autoSelect(fromName: String, toName: String, atMillis: Long): ConnectionLogEntry =
        ConnectionLogEntry(
            id = UUID.randomUUID().toString(),
            atMillis = atMillis,
            type = TYPE_AUTO_SELECT,
            fromName = fromName,
            toName = toName,
        )

    fun parse(json: String?): List<ConnectionLogEntry> {
        if (json.isNullOrBlank()) return emptyList()
        val parsed = try {
            JsonUtil.fromJson(json, Array<ConnectionLogEntry>::class.java)
        } catch (_: Exception) {
            null
        }
        return parsed
            ?.toList()
            ?.filter { entry -> entry.id.isNotBlank() && entry.type.isNotBlank() && entry.atMillis > 0L }
            ?: emptyList()
    }

    fun append(
        existing: List<ConnectionLogEntry>,
        entry: ConnectionLogEntry,
        maxSize: Int = MAX_SIZE,
    ): List<ConnectionLogEntry> {
        if (entry.id.isBlank() || entry.type.isBlank() || entry.atMillis <= 0L) return existing
        return (listOf(entry) + existing).take(maxSize.coerceAtLeast(1))
    }
}
