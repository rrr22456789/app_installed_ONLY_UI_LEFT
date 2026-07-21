package com.example.adaptiveisland.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.adaptiveisland.service.OverlayForegroundService
import com.example.adaptiveisland.tracker.MidnightResetScheduler

/**
 * Receives the scheduled midnight alarm intent, routes processing to background tracking
 * components via the service instance, and schedules the next daily alarm frame.
 */
class MidnightResetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext

        val serviceIntent = Intent(appContext, OverlayForegroundService::class.java).apply {
            action = "com.example.adaptiveisland.ACTION_MIDNIGHT_CROSSOVER"
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(serviceIntent)
        } else {
            appContext.startService(serviceIntent)
        }

        // Queue the next systematic layout reset schedule sequence
        MidnightResetScheduler.scheduleNextReset(appContext)
    }
}