package com.wenodh.zen.blocking

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockedAppsStoreTest {
    private val blocked = setOf("com.example.social", "com.example.video")

    @Test
    fun blocksSelectedPackageDuringActiveFocus() {
        assertTrue(AppBlockPolicy.shouldBlock("com.example.social", "com.wenodh.zen", blocked, true))
    }

    @Test
    fun doesNotBlockUnselectedPackage() {
        assertFalse(AppBlockPolicy.shouldBlock("com.example.mail", "com.wenodh.zen", blocked, true))
    }

    @Test
    fun doesNotBlockOutsideFocusSession() {
        assertFalse(AppBlockPolicy.shouldBlock("com.example.social", "com.wenodh.zen", blocked, false))
    }

    @Test
    fun neverBlocksWenodhZenItself() {
        assertFalse(AppBlockPolicy.shouldBlock("com.wenodh.zen", "com.wenodh.zen", blocked, true))
    }

    @Test
    fun neverBlocksProtectedDialerOrLauncherPackages() {
        val protected = setOf("com.android.dialer", "com.android.launcher")
        assertFalse(AppBlockPolicy.shouldBlock("com.android.dialer", "com.wenodh.zen", protected + blocked, true, protected))
        assertFalse(AppBlockPolicy.shouldBlock("com.android.launcher", "com.wenodh.zen", protected + blocked, true, protected))
    }

    @Test
    fun ignoresMissingPackageName() {
        assertFalse(AppBlockPolicy.shouldBlock(null, "com.wenodh.zen", blocked, true))
    }
}
