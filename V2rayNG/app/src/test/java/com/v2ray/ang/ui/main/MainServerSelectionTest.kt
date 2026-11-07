package com.v2ray.ang.ui.main

import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.dto.entities.ServersCache
import com.v2ray.ang.enums.EConfigType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MainServerSelectionTest {

    @Test
    fun fastestWorkingServerIgnoresFailedUntestedAndGroups() {
        val servers = listOf(
            server("fail", EConfigType.VLESS, delay = -1),
            server("untested", EConfigType.VLESS, delay = 0),
            server("slow", EConfigType.VMESS, delay = 400),
            server("fast", EConfigType.VLESS, delay = 80),
            server("group", EConfigType.POLICYGROUP, delay = 10),
        )
        assertEquals("fast", fastestWorkingServerGuid(servers))
    }

    @Test
    fun fastestWorkingServerReturnsNullWhenNoneWork() {
        val servers = listOf(
            server("fail", EConfigType.VLESS, delay = -1),
            server("untested", EConfigType.VLESS, delay = 0),
        )
        assertNull(fastestWorkingServerGuid(servers))
    }

    @Test
    fun liveTestOrderMovesWorkingServersToFrontByDelay() {
        val servers = listOf(
            server("untested", EConfigType.VLESS, delay = 0),
            server("slow", EConfigType.VMESS, delay = 400),
            server("fail", EConfigType.VLESS, delay = -1),
            server("fast", EConfigType.VLESS, delay = 80),
        )
        assertEquals(
            listOf("fast", "slow", "untested", "fail"),
            liveTestOrder(servers) { it.testDelayMillis }.map { it.guid },
        )
    }

    @Test
    fun liveTestOrderLeavesListUnchangedWhenNobodyWorksYet() {
        val servers = listOf(
            server("a", EConfigType.VLESS, delay = 0),
            server("b", EConfigType.VLESS, delay = -1),
        )
        assertEquals(servers, liveTestOrder(servers) { it.testDelayMillis })
    }

    private fun server(guid: String, type: EConfigType, delay: Long): ServersCache =
        ServersCache(guid, ProfileItem.create(type), delay)
}
