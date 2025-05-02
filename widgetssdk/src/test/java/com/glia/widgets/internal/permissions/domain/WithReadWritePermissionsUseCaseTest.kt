package com.glia.widgets.internal.permissions.domain

import android.Manifest
import android.os.Build
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.permissions.PermissionsGrantedCallback
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers


@RunWith(RobolectricTestRunner::class)
class WithReadWritePermissionsUseCaseTest {
    private lateinit var permissionManager: PermissionManager
    private lateinit var useCase: WithReadWritePermissionsUseCase
    private lateinit var callback: () -> Unit

    @Before
    fun setUp() {
        permissionManager = mockk(relaxUnitFun = true)
        callback = mockk(relaxed = true)
        useCase = WithReadWritePermissionsUseCaseImpl(permissionManager)
    }

    @After
    fun tearDown() {
        confirmVerified(permissionManager, callback)
    }

    @Test
    fun `invoke invokes callback when API version is higher than 29`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 34)
        useCase(callback)
        verify { callback.invoke() }

        verify(exactly = 0) { permissionManager.handlePermissions(any(), any()) }
    }

    @Test
    fun `invoke invokes callback when API version is 29`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 29)
        useCase(callback)
        verify { callback.invoke() }

        verify(exactly = 0) { permissionManager.handlePermissions(any(), any()) }
    }

    @Test
    fun `invoke invokes callback when read and write permission is granted API version less then 29`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 28)
        useCase(callback)
        verify(exactly = 0) { callback.invoke() }
        val permissionsGrantedCallbackSlot = slot<PermissionsGrantedCallback>()

        verify {
            permissionManager.handlePermissions(
                eq(
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ), null, capture(permissionsGrantedCallbackSlot), null, null
            )
        }
        permissionsGrantedCallbackSlot.captured.invoke(true)
        verify { callback.invoke() }
    }

    @Test
    fun `invoke does nothing when read and write permission is not granted API version less then 29`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 28)
        useCase(callback)
        verify(exactly = 0) { callback.invoke() }
        val permissionsGrantedCallbackSlot = slot<PermissionsGrantedCallback>()

        verify {
            permissionManager.handlePermissions(
                eq(
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ), null, capture(permissionsGrantedCallbackSlot), null, null
            )
        }
        permissionsGrantedCallbackSlot.captured.invoke(false)
        verify(exactly = 0) { callback.invoke() }
    }
}
