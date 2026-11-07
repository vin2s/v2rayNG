package com.v2ray.ang.core

import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.OutboundTrafficStat

internal data class FailoverCandidate(
    val guid: String,
    val delayMillis: Long,
    val isComplex: Boolean,
)

internal fun nextFailoverServerGuid(
    candidates: List<FailoverCandidate>,
    currentGuid: String,
    failedGuids: Set<String>,
): String? {
    val usable = candidates.filter { candidate ->
        candidate.guid != currentGuid && !candidate.isComplex
    }
    usable
        .filter { it.delayMillis > 0 && it.guid !in failedGuids }
        .minByOrNull { it.delayMillis }
        ?.guid
        ?.let { return it }
    usable.firstOrNull { it.delayMillis == 0L && it.guid !in failedGuids }?.guid?.let { return it }
    return usable.firstOrNull { it.guid !in failedGuids }?.guid
}

internal fun proxyTrafficBytes(stats: Iterable<OutboundTrafficStat>): Long {
    var total = 0L
    stats.forEach { stat ->
        if (stat.tag != AppConfig.TAG_DIRECT && stat.tag != AppConfig.TAG_BLOCKED) {
            total += stat.value
        }
    }
    return total
}
