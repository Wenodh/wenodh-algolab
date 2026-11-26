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
    fun ignoresMissingPackageName() {
        assertFalse(AppBlockPolicy.shouldBlock(null, "com.wenodh.zen", blocked, true))
    }
}
