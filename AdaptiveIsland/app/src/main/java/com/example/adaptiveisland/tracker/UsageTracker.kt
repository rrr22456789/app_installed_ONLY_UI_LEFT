package com.example.adaptiveisland.tracker

import android.content.Context
import com.example.adaptiveisland.data.history.UsageHistoryRepository
import com.example.adaptiveisland.util.AppNameResolver
import com.example.adaptiveisland.util.TimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Synchronized single source of truth tracking application usage sessions.
 * Safely aggregates elapsed session times and saves structural tracking blocks.
 */
class UsageTracker(
    context: Context,
    private val repository: UsageHistoryRepository
) {
    private val appContext = context.applicationContext
    
    private val _sessionState = MutableStateFlow(ActiveSessionState())
    val sessionState: StateFlow<ActiveSessionState> = _sessionState.asStateFlow()

    private val sessionMutex = Mutex()
    private val dateSignatureFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private var currentPackage: String = ""
    private var sessionStartTime: Long = 0L
    private var tickerJob: Job? = null

    /**
     * Evaluates incoming package identifiers, closing out older traces cleanly.
     */
    suspend fun handlePackageChange(packageName: String, scope: CoroutineScope) {
        if (packageName.isEmpty()) return

        sessionMutex.withLock {
            if (packageName == currentPackage) return

            closeCurrentSessionInternal()

            currentPackage = packageName
            sessionStartTime = System.currentTimeMillis()
            val appLabel = AppNameResolver.getAppName(appContext, packageName)

            _sessionState.value = ActiveSessionState(
                packageName = packageName,
                appName = appLabel,
                startTimeMs = sessionStartTime,
                elapsedTimeMs = 0L,
                formattedTime = TimeFormatter.formatElapsedTime(0L)
            )

            startTicker(scope)
        }
    }

    /**
     * Public thread-safe lane to terminate the active session via external components.
     */
    suspend fun closeCurrentSession() {
        sessionMutex.withLock {
            closeCurrentSessionInternal()
        }
    }

    /**
     * Internal un-locked session finalization routine executed inside atomic lock blocks.
     */
    private suspend fun closeCurrentSessionInternal() {
        tickerJob?.cancel()
        tickerJob = null

        val state = _sessionState.value
        if (currentPackage.isNotEmpty() && state.elapsedTimeMs > 0L) {
            val dateStr = synchronized(dateSignatureFormatter) {
                dateSignatureFormatter.format(Date(sessionStartTime))
            }
            withContext(Dispatchers.IO) {
                repository.recordAppUsage(
                    packageName = state.packageName,
                    appName = state.appName,
                    dateStr = dateStr,
                    durationMs = state.elapsedTimeMs
                )
            }
        }
        currentPackage = ""
        sessionStartTime = 0L
    }

    /**
     * Forces session boundary re-alignments to manage structural midnight loops cleanly.
     */
    suspend fun handleMidnightCrossover(scope: CoroutineScope) {
        sessionMutex.withLock {
            if (currentPackage.isEmpty()) return
            val activePkg = currentPackage
            closeCurrentSessionInternal()
            
            currentPackage = activePkg
            sessionStartTime = System.currentTimeMillis()
            val appLabel = AppNameResolver.getAppName(appContext, activePkg)

            _sessionState.value = ActiveSessionState(
                packageName = activePkg,
                appName = appLabel,
                startTimeMs = sessionStartTime,
                elapsedTimeMs = 0L,
                formattedTime = TimeFormatter.formatElapsedTime(0L)
            )

            startTicker(scope)
        }
    }

    /**
     * Spawns a synchronized, cooperative counting loop tracking active duration states.
     */
    private fun startTicker(scope: CoroutineScope) {
        tickerJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000L)
                val elapsed = System.currentTimeMillis() - sessionStartTime
                _sessionState.value = _sessionState.value.copy(
                    elapsedTimeMs = elapsed,
                    formattedTime = TimeFormatter.formatElapsedTime(elapsed)
                )
            }
        }
    }
}