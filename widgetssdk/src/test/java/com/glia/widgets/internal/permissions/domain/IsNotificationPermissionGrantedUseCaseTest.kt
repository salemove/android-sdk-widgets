package com.glia.widgets.internal.permissions.domain

import android.Manifest
import android.os.Build
import com.glia.widgets.internal.permissions.PermissionManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
class IsNotificationPermissionGrantedUseCaseTest {
    private lateinit var permissionManager: PermissionManager
    private lateinit var useCase: IsNotificationPermissionGrantedUseCaseImpl

    @Before
    fun setUp() {
        permissionManager = mockk(relaxed = true)
        useCase = IsNotificationPermissionGrantedUseCaseImpl(permissionManager)
    }

    @Test
    fun `invoke returns true when api version is lower than 33`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 32)
        assertTrue(useCase())
    }

    @Test
    fun `invoke returns true when api version is 33 or higher and permission is granted`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 33)
        every { permissionManager.hasPermission(Manifest.permission.POST_NOTIFICATIONS) } returns true
        assertTrue(useCase())
    }

    @Test
    fun `invoke returns false when api version is 33 or higher and permission is not granted`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 33)
        every { permissionManager.hasPermission(Manifest.permission.POST_NOTIFICATIONS) } returns false
        assertFalse(useCase())
    }
}
