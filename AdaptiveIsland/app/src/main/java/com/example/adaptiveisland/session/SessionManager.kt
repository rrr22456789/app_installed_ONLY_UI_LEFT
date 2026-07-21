package com.example.adaptiveisland.session

import android.content.Context
import com.example.adaptiveisland.tracker.ForegroundAppMonitor
import com.example.adaptiveisland.tracker.UsageTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Coordinates high-level application tracking sessions by bridging system monitors,
 * device screen status changes, and core usage tracking components.
 */
class SessionManager(
    private val context: Context,
    private val appMonitor: ForegroundAppMonitor,
    private val usageTracker: UsageTracker
) {
    private var trackingJob: Job? = null
    private var isScreenOn = true

    /**
     * Connects data flow lines across monitoring streams under a managed lifecycle scope.
     */
    fun start(scope: CoroutineScope) {
        if (trackingJob?.isActive == true) return

        trackingJob = scope.launch(Dispatchers.Default) {
            appMonitor.foregroundPackage.collectLatest { packageName ->
                if (isScreenOn && packageName.isNotEmpty()) {
                    usageTracker.handlePackageChange(packageName, this)
                }
            }
        }
    }

    /**
     * Cleans up tracking pipelines safely during component teardowns.
     */
    fun stop() {
        trackingJob?.cancel()
        trackingJob = null
    }

    /**
     * Evaluates screen interaction toggles, suspending data collection loops off-screen.
     */
    fun handleScreenStateChanged(isOn: Boolean, scope: CoroutineScope) {
        isScreenOn = isOn
        scope.launch(Dispatchers.Default) {
            if (!isOn) {
                usageTracker.closeCurrentSession()
            } else {
                val currentPkg = appMonitor.foregroundPackage.value
                if (currentPkg.isNotEmpty()) {
                    usageTracker.handlePackageChange(currentPkg, this)
                }
            }
        }
    }

    /**
     * Forces operational bounds synchronization when handling systemic date resets.
     */
    fun handleMidnightCrossover(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            if (isScreenOn) {
                usageTracker.handleMidnightCrossover(this)
            }
        }
    }
}