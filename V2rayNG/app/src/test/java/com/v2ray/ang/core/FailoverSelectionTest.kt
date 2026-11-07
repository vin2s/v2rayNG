package com.v2ray.ang.core

import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.OutboundTrafficStat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FailoverSelectionTest {

    @Test
    fun picksFastestWorkingNotCurrentOrFailed() {
        val candidates = listOf(
            FailoverCandidate("current", 50, isComplex = false),
            FailoverCandidate("dead", -1, isComplex = false),
            FailoverCandidate("slow", 400, isComplex = false),
            FailoverCandidate("fast", 80, isComplex = false),
            FailoverCandidate("group", 10, isComplex = true),
        )
        assertEquals(
            "fast",
            nextFailoverServerGuid(candidates, currentGuid = "current", failedGuids = setOf("dead")),
        )
    }

    @Test
    fun fallsBackToUntestedWhenNoneHavePositiveDelay() {
        val candidates = listOf(
            FailoverCandidate("current", -1, isComplex = false),
            FailoverCandidate("untested", 0, isComplex = false),
            FailoverCandidate("failed", -1, isComplex = false),
        )
        assertEquals(
            "untested",
            nextFailoverServerGuid(candidates, "current", setOf("failed")),
        )
    }

    @Test
    fun returnsNullWhenOnlyCurrentOrFailedRemain() {
        val candidates = listOf(
            FailoverCandidate("current", 20, isComplex = false),
            FailoverCandidate("failed", 10, isComplex = false),
            FailoverCandidate("group", 5, isComplex = true),
        )
        assertNull(nextFailoverServerGuid(candidates, "current", setOf("failed")))
    }

    @Test
    fun countsProxyTrafficAndIgnoresDirectAndBlocked() {
        val stats = listOf(
            OutboundTrafficStat(AppConfig.TAG_PROXY, AppConfig.DOWNLINK, 40),
            OutboundTrafficStat("custom-out", AppConfig.UPLINK, 10),
            OutboundTrafficStat(AppConfig.TAG_DIRECT, AppConfig.DOWNLINK, 999),
            OutboundTrafficStat(AppConfig.TAG_BLOCKED, AppConfig.UPLINK, 999),
        )
        assertEquals(50L, proxyTrafficBytes(stats))
    }
}
