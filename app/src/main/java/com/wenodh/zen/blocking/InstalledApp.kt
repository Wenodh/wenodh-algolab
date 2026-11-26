package com.wenodh.zen.blocking

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

data class InstalledApp(val packageName: String, val label: String)

object InstalledAppCatalog {
    fun load(context: Context): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val pm = context.packageManager
        val protectedPackages = ProtectedPackages.forDevice(context)
        val activities = if (Build.VERSION.SDK_INT >= 33) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
        return activities.asSequence()
            .mapNotNull { info ->
                val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
                if (packageName in protectedPackages) return@mapNotNull null
                val label = info.loadLabel(pm)?.toString()?.takeIf(String::isNotBlank) ?: packageName
                InstalledApp(packageName, label)
            }
            .distinctBy(InstalledApp::packageName)
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
