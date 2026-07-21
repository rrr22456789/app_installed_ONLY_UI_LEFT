package com.example.adaptiveisland.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.adaptiveisland.R
import com.example.adaptiveisland.util.PermissionHelper

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)

        findViewById<Button>(R.id.btnGrantOverlay).setOnClickListener {
            startActivity(PermissionHelper.getOverlayPermissionIntent(this))
        }

        findViewById<Button>(R.id.btnGrantUsage).setOnClickListener {
            startActivity(PermissionHelper.getUsageStatsPermissionIntent())
        }

        findViewById<Button>(R.id.btnGrantBattery).setOnClickListener {
            startActivity(PermissionHelper.getBatteryOptimizationIntent(this))
        }

        updatePermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {

        val overlay = PermissionHelper.hasOverlayPermission(this)
        val usage = PermissionHelper.hasUsageStatsPermission(this)
        val battery = PermissionHelper.isIgnoringBatteryOptimizations(this)

        tvStatus.text = buildString {
            append("Overlay: ")
            append(if (overlay) "✅ Granted" else "❌ Missing")

            append("\nUsage Access: ")
            append(if (usage) "✅ Granted" else "❌ Missing")

            append("\nBattery Optimization: ")
            append(if (battery) "✅ Granted" else "❌ Missing")
        }

        if (overlay && usage) {
            tvStatus.append("\n\nAll required permissions granted.")
        }
    }
}