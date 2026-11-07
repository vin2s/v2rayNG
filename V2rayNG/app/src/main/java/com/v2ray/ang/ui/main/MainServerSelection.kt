package com.v2ray.ang.ui.main

import com.v2ray.ang.dto.entities.ServersCache
import com.v2ray.ang.extension.isComplexType

internal fun fastestWorkingServerGuid(servers: List<ServersCache>): String? =
    servers
        .asSequence()
        .filter { server ->
            server.testDelayMillis > 0 && !server.profile.configType.isComplexType()
        }
        .minByOrNull { it.testDelayMillis }
        ?.guid

/** Working servers (delay > 0) first by delay; untested and failed keep their relative order. */
internal fun <T> liveTestOrder(items: List<T>, delayMillis: (T) -> Long): List<T> {
    if (items.none { delayMillis(it) > 0L }) return items
    val working = items.filter { delayMillis(it) > 0L }.sortedBy(delayMillis)
    val rest = items.filter { delayMillis(it) <= 0L }
    return working + rest
}
