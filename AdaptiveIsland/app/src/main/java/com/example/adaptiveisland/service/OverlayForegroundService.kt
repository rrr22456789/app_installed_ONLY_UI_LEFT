package com.example.adaptiveisland.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.adaptiveisland.R
import com.example.adaptiveisland.data.history.AppDatabase
import com.example.adaptiveisland.data.history.UsageHistoryRepository
import com.example.adaptiveisland.data.settings.PreferencesRepository
import com.example.adaptiveisland.session.SessionManager
import com.example.adaptiveisland.tracker.ForegroundAppMonitor
import com.example.adaptiveisland.tracker.UsageTracker
import com.example.adaptiveisland.ui.overlay.IslandOverlayView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * System daemon managing window overlays, telemetry collectors, and configuration loops.
 */
class OverlayForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var usageHistoryRepository: UsageHistoryRepository
    private lateinit var appMonitor: ForegroundAppMonitor
    private lateinit var usageTracker: UsageTracker
    private lateinit var sessionManager: SessionManager
    private var overlayView: IslandOverlayView? = null

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> sessionManager.handleScreenStateChanged(true, serviceScope)
                Intent.ACTION_SCREEN_OFF -> sessionManager.handleScreenStateChanged(false, serviceScope)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        initializeComponents()
        createNotificationChannel()
        startForeground(1001, buildNotification())
        
        registerScreenReceiver()
        startTrackingPipelines()
        observeSystemStates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1001, buildNotification())
        }

        if (intent?.action == "com.example.adaptiveisland.ACTION_MIDNIGHT_CROSSOVER") {
            sessionManager.handleMidnightCrossover(serviceScope)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterReceiver(screenStateReceiver)
        sessionManager.stop()
        appMonitor.stopMonitoring()
        
        serviceScope.launch(Dispatchers.Main) {
            usageTracker.closeCurrentSession()
            removeOverlayImmediate()
            serviceScope.cancel()
        }
        super.onDestroy()
    }

    private fun initializeComponents() {
        val appContext = applicationContext
        preferencesRepository = PreferencesRepository(appContext)
        val database = AppDatabase.getDatabase(appContext)
        usageHistoryRepository = UsageHistoryRepository(database.usageDao())
        
        appMonitor = ForegroundAppMonitor(appContext)
        usageTracker = UsageTracker(appContext, usageHistoryRepository)
        sessionManager = SessionManager(appContext, appMonitor, usageTracker)
    }

    private fun registerScreenReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    private fun startTrackingPipelines() {
        appMonitor.startMonitoring(serviceScope)
        sessionManager.start(serviceScope)
    }

    private fun observeSystemStates() {
        serviceScope.launch {
            preferencesRepository.preferencesFlow.collect { preferences ->
                if (preferences.isOverlayEnabled) {
                    ensureOverlayCreated()
                    overlayView?.updateLayoutDimensions(preferences.capsuleWidth, preferences.capsuleHeight)
                    overlayView?.updatePositionCoordinates(preferences.positionX, preferences.positionY)
                } else {
                    removeOverlayImmediate()
                }
            }
        }

        serviceScope.launch {
            usageTracker.sessionState.collect { sessionState ->
                overlayView?.renderSessionMetrics(sessionState.appName, sessionState.formattedTime)
            }
        }
    }

    private fun ensureOverlayCreated() {
        if (overlayView == null) {
            overlayView = IslandOverlayView(applicationContext).apply {
                setOnPositionChangeListener { x, y ->
                    serviceScope.launch {
                        preferencesRepository.updatePosition(x, y)
                    }
                }
            }
            overlayView?.attachToWindow()
        }
    }

    private fun removeOverlayImmediate() {
        overlayView?.removeFromWindow()
        overlayView = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "island_service_channel",
                "Adaptive Island Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "island_service_channel")
            .setContentTitle("Adaptive Island")
            .setContentText("Application usage tracking service is active.")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}