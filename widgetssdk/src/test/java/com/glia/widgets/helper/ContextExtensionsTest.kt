package com.glia.widgets.helper

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ContextExtensionsTest {

    private lateinit var context: Context
    private lateinit var intent: Intent
    private var failureCount: Int = 0
    private var successCount: Int = 0

    private val onFailure: () -> Unit = { failureCount++ }
    private val onSuccess: () -> Unit = { successCount++ }

    @Before
    fun setUp() {
        failureCount = 0
        successCount = 0
        val packageManager = mockk<PackageManager>(relaxed = true)
        context = mockk(relaxed = true) {
            every { this@mockk.packageManager } returns packageManager
        }
        intent = mockk(relaxed = true) {
            every { action } returns Intent.ACTION_VIEW
            every { resolveActivity(packageManager) } returns ComponentName("com.example", "Viewer")
        }
    }

    @Test
    fun `safeStartActivity calls onSuccess when activity starts`() {
        context.safeStartActivity(intent, onFailure, onSuccess)

        verify { context.startActivity(intent) }
        assertEquals(1, successCount)
        assertEquals(0, failureCount)
    }

    @Test
    fun `safeStartActivity calls onFailure when no activity resolves`() {
        every { intent.resolveActivity(any()) } returns null

        context.safeStartActivity(intent, onFailure, onSuccess)

        verify(exactly = 0) { context.startActivity(any()) }
        assertEquals(0, successCount)
        assertEquals(1, failureCount)
    }

    @Test
    fun `safeStartActivity calls onFailure when uri permission can not be granted`() {
        every { context.startActivity(intent) } throws SecurityException("UID does not have permission")

        context.safeStartActivity(intent, onFailure, onSuccess)

        assertEquals(0, successCount)
        assertEquals(1, failureCount)
    }

    @Test
    fun `safeStartActivity calls onFailure when activity is not found at start time`() {
        every { context.startActivity(intent) } throws ActivityNotFoundException()

        context.safeStartActivity(intent, onFailure, onSuccess)

        assertEquals(0, successCount)
        assertEquals(1, failureCount)
    }
}
