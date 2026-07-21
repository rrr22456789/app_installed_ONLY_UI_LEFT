package com.example.adaptiveisland.tracker

data class ActiveSessionState(
    val packageName: String = "",
    val appName: String = "",
    val startTimeMs: Long = 0L,
    val elapsedTimeMs: Long = 0L,
    val formattedTime: String = "00:00:00"
)