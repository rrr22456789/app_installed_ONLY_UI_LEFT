package com.example.adaptiveisland.tracker

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

/**
 * High-precision polling module querying system usage events to extract
 * the active target foreground package state without holding window references.
 */
class ForegroundAppMonitor(context: Context) {

    private val appContext = context.applicationContext
    private val usageStatsManager = appContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    
    private val _foregroundPackage = MutableStateFlow<String>("")
    val foregroundPackage: StateFlow<String> = _foregroundPackage.asStateFlow()

    private var monitorJob: Job? = null
    private val homePackages = getHomePackages()

    /**
     * Spawns an asynchronous background thread polling query stream loop.
     */
    fun startMonitoring(scope: CoroutineScope) {
        if (monitorJob?.isActive == true) return

        monitorJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val currentApp = queryForegroundPackage()
                if (currentApp.isNotEmpty() && currentApp != _foregroundPackage.value && !homePackages.contains(currentApp)) {
                    _foregroundPackage.value = currentApp
                }
                delay(500L) // Precision rule: poll exactly every 500 milliseconds
            }
        }
    }

    /**
     * Halts background polling threads cleanly, securing coroutine contexts.
     */
    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    /**
     * Inspects active system usage log partitions within a rolling history slice.
     */
    private fun queryForegroundPackage(): String {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 5000L
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var lastForegroundApp = ""

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastForegroundApp = event.packageName
            }
        }
        return lastForegroundApp
    }

    /**
     * Resolves default system launcher packages to prevent tracking core home screens.
     */
    private fun getHomePackages(): Set<String> {
        val packages = mutableSetOf<String>()
        val pm = appContext.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
        for (info in resolveInfo) {
            info.activityInfo?.packageName?.let { packages.add(it) }
        }
        return packages
    }
}