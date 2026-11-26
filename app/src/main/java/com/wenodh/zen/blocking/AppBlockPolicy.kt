package com.wenodh.zen.blocking

object AppBlockPolicy {
    fun shouldBlock(
        packageName: String?,
        ownPackageName: String,
        blockedPackages: Set<String>,
        focusSessionActive: Boolean,
        protectedPackages: Set<String> = setOf(ownPackageName)
    ): Boolean {
        if (!focusSessionActive || packageName.isNullOrBlank()) return false
        if (packageName == ownPackageName || packageName in protectedPackages) return false
        return packageName in blockedPackages
    }
}
