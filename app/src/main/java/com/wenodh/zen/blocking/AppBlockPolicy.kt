package com.wenodh.zen.blocking

object AppBlockPolicy {
    fun shouldBlock(
        packageName: String?,
        ownPackageName: String,
        blockedPackages: Set<String>,
        focusSessionActive: Boolean
    ): Boolean {
        if (!focusSessionActive || packageName.isNullOrBlank() || packageName == ownPackageName) return false
        return packageName in blockedPackages
    }
}
