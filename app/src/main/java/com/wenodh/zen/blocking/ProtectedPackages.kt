package com.wenodh.zen.blocking

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telecom.TelecomManager

object ProtectedPackages {
    fun forDevice(context: Context): Set<String> = buildSet {
        add(context.packageName)
        add("com.android.settings")
        add("com.android.systemui")
        add("com.android.phone")
        add("com.android.emergency")
        add("com.google.android.emergency")

        val packageManager = context.packageManager
        runCatching {
            val telecom = context.getSystemService(TelecomManager::class.java)
            telecom?.defaultDialerPackage?.let(::add)
        }
        resolvePackage(packageManager, Intent(Intent.ACTION_DIAL))?.let(::add)
        resolvePackage(packageManager, Intent("android.intent.action.EMERGENCY_DIAL"))?.let(::add)
        resolvePackage(
            packageManager,
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        )?.let(::add)
    }

    private fun resolvePackage(packageManager: PackageManager, intent: Intent): String? =
        runCatching {
            @Suppress("DEPRECATION")
            packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
        }.getOrNull()
}
