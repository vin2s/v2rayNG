package com.v2ray.ang.handler

import com.v2ray.ang.dto.ConnectionLogEntry

object ConnectionLog {
    fun recordStart(serverName: String) {
        MmkvManager.appendConnectionLog(ConnectionLogEntries.start(serverName, System.currentTimeMillis()))
    }

    fun recordStop(serverName: String) {
        MmkvManager.appendConnectionLog(ConnectionLogEntries.stop(serverName, System.currentTimeMillis()))
    }

    fun recordFailover(fromName: String, toName: String) {
        MmkvManager.appendConnectionLog(
            ConnectionLogEntries.failover(fromName, toName, System.currentTimeMillis())
        )
    }

    fun recordAutoSelect(fromName: String, toName: String) {
        MmkvManager.appendConnectionLog(
            ConnectionLogEntries.autoSelect(fromName, toName, System.currentTimeMillis())
        )
    }

    fun read(): List<ConnectionLogEntry> = MmkvManager.decodeConnectionLog()

    fun clear() {
        MmkvManager.clearConnectionLog()
    }
}
