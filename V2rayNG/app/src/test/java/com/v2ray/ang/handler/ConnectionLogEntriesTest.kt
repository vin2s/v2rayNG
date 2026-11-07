package com.v2ray.ang.handler

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConnectionLogEntriesTest {

    @Test
    fun parseBlankOrInvalidJsonAsEmpty() {
        assertTrue(ConnectionLogEntries.parse(null).isEmpty())
        assertTrue(ConnectionLogEntries.parse("").isEmpty())
        assertTrue(ConnectionLogEntries.parse("{").isEmpty())
        assertTrue(ConnectionLogEntries.parse("[{\"id\":\"\",\"type\":\"start\",\"atMillis\":1}]").isEmpty())
    }

    @Test
    fun parseKeepsValidEntries() {
        val json = """[{"id":"a","atMillis":10,"type":"start","fromName":"","toName":"one"}]"""
        val parsed = ConnectionLogEntries.parse(json)
        assertEquals(1, parsed.size)
        assertEquals("a", parsed[0].id)
        assertEquals(ConnectionLogEntries.TYPE_START, parsed[0].type)
        assertEquals("one", parsed[0].toName)
    }

    @Test
    fun appendPutsNewestFirstAndCapsSize() {
        val first = ConnectionLogEntries.start("one", 1L)
        val second = ConnectionLogEntries.failover("one", "two", 2L)
        val appended = ConnectionLogEntries.append(listOf(first), second, maxSize = 2)
        assertEquals(listOf(second.id, first.id), appended.map { it.id })

        val third = ConnectionLogEntries.stop("two", 3L)
        val capped = ConnectionLogEntries.append(appended, third, maxSize = 2)
        assertEquals(listOf(third.id, second.id), capped.map { it.id })
    }

    @Test
    fun appendIgnoresInvalidEntry() {
        val existing = listOf(ConnectionLogEntries.start("one", 1L))
        val ignored = ConnectionLogEntries.append(existing, ConnectionLogEntries.start("two", 0L))
        assertEquals(existing, ignored)
    }
}
