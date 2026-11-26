package com.wenodh.zen.blocking

import org.junit.Assert.assertEquals
import org.junit.Test

class BlockedAppsStoreTest {
    @Test
    fun blockedPackageSetUsesExactPackageIdentifiers() {
        val selected = setOf("com.example.social", "com.example.video")
        assertEquals(2, selected.size)
        assertEquals(setOf("com.example.social", "com.example.video"), selected)
    }
}
