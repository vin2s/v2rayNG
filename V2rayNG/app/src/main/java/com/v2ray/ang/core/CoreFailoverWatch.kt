package com.v2ray.ang.core

import com.v2ray.ang.AppConfig
import com.v2ray.ang.extension.delay
import com.v2ray.ang.extension.isComplexType
import com.v2ray.ang.handler.ConnectionLog
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.NotificationManager
import com.v2ray.ang.helper.MessageHelper
import com.v2ray.ang.util.LogUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal object CoreFailoverWatch {
    private const val POLL_MS = 3_000L
    private const val GRACE_MS = 20_000L
    private const val IDLE_PROBE_MS = 45_000L
    private const val HEALTH_PROBE_MS = 120_000L

    private val failedGuids = linkedSetOf<String>()
    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        job?.cancel()
        job = scope.launch {
            try {
                runLoop()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                LogUtil.e(AppConfig.TAG, "Failover watch failed", error)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        failedGuids.clear()
    }

    fun markCurrentHealthy(guid: String) {
        failedGuids.remove(guid)
    }

    suspend fun switchIfUnreachable() {
        switchAwayFromCurrent()
    }

    private suspend fun runLoop() {
        val startedAt = System.currentTimeMillis()
        var idleSince = startedAt
        var lastProbeAt = 0L
        while (currentCoroutineContext().isActive) {
            if (!CoreServiceManager.isRunning() || CoreServiceManager.isReloadingCore()) {
                delay(POLL_MS)
                continue
            }
            val stats = CoreServiceManager.queryAllOutboundTrafficStats()
            NotificationManager.onTrafficSample(stats, POLL_MS)
            val now = System.currentTimeMillis()
            if (proxyTrafficBytes(stats) > 0L) {
                idleSince = now
            }
            if (now - startedAt >= GRACE_MS) {
                val sinceProbe = now - lastProbeAt
                val idleFor = now - idleSince
                val probeDue = lastProbeAt == 0L ||
                    idleFor >= IDLE_PROBE_MS ||
                    sinceProbe >= HEALTH_PROBE_MS
                if (probeDue && (lastProbeAt == 0L || sinceProbe >= POLL_MS)) {
                    lastProbeAt = now
                    if (shouldWatchSelectedProfile() && !probeCurrentHealthy()) {
                        switchAwayFromCurrent()
                        idleSince = System.currentTimeMillis()
                        lastProbeAt = idleSince
                    } else {
                        idleSince = now
                    }
                }
            }
            delay(POLL_MS)
        }
    }

    private fun shouldWatchSelectedProfile(): Boolean {
        val guid = MmkvManager.getSelectServer() ?: return false
        val profile = MmkvManager.decodeServerConfig(guid) ?: return false
        return !profile.configType.isComplexType()
    }

    private suspend fun probeCurrentHealthy(): Boolean {
        val delayMillis = CoreServiceManager.probeCurrentDelayMillis()
        currentCoroutineContext().ensureActive()
        val guid = MmkvManager.getSelectServer()
        return if (delayMillis >= 0L) {
            if (guid != null) markCurrentHealthy(guid)
            true
        } else {
            false
        }
    }

    private suspend fun switchAwayFromCurrent() {
        currentCoroutineContext().ensureActive()
        if (!CoreServiceManager.isRunning() || CoreServiceManager.isReloadingCore()) return
        val current = MmkvManager.getSelectServer() ?: return
        val profile = MmkvManager.decodeServerConfig(current) ?: return
        if (profile.configType.isComplexType()) return

        failedGuids.add(current)
        MmkvManager.encodeServerTestDelayMillis(current, -1L)
        val next = nextFailoverServerGuid(
            candidates = failoverCandidates(profile.subscriptionId),
            currentGuid = current,
            failedGuids = failedGuids,
        )
        if (next == null) {
            LogUtil.w(
                AppConfig.TAG,
                "Failover: no candidate after current profile $current"
            )
            return
        }

        val nextProfile = MmkvManager.decodeServerConfig(next)
        LogUtil.i(AppConfig.TAG, "Failover: switching from $current to $next")
        ConnectionLog.recordFailover(
            fromName = profile.remarks,
            toName = nextProfile?.remarks.orEmpty(),
        )
        MmkvManager.setSelectServer(next)
        CoreServiceManager.getServiceContext()?.let { service ->
            MessageHelper.sendMsg2UI(service, AppConfig.MSG_STATE_SELECTED_SERVER, next)
        }
        CoreServiceManager.reloadCoreAfterFailover()
    }

    private fun failoverCandidates(subscriptionId: String): List<FailoverCandidate> =
        MmkvManager.decodeServerList(subscriptionId).mapNotNull { guid ->
            val item = MmkvManager.decodeServerConfig(guid) ?: return@mapNotNull null
            FailoverCandidate(
                guid = guid,
                delayMillis = MmkvManager.decodeServerAffiliationInfo(guid)?.testDelayMillis ?: 0L,
                isComplex = item.configType.isComplexType(),
            )
        }
}
