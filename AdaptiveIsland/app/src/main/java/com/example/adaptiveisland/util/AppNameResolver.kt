package com.example.adaptiveisland.util

import android.content.Context
import android.content.pm.PackageManager

/**
 * Stateless processing node parsing application names from dynamic system identifiers.
 */
object AppNameResolver {

    /**
     * Resolves localized labels using the native system package infrastructure.
     * Returns the raw package namespace cleanly if query loops match missing installations.
     */
    fun getAppName(context: Context, packageName: String): String {
        val appContext = context.applicationContext
        val packageManager = appContext.packageManager
        return try {
            val appInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                packageManager.getApplicationInfo(packageName, 0)
            }
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}