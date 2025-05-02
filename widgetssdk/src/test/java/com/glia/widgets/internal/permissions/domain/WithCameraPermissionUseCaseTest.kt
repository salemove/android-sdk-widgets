package com.glia.widgets.internal.permissions.domain

import android.Manifest
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.permissions.PermissionsGrantedCallback
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class WithCameraPermissionUseCaseTest {
    private lateinit var permissionManager: PermissionManager
    private lateinit var useCase: WithCameraPermissionUseCase
    private lateinit var callback: () -> Unit

    @Before
    fun setUp() {
        permissionManager = mockk(relaxUnitFun = true)
        callback = mockk(relaxed = true)
        useCase = WithCameraPermissionUseCaseImpl(permissionManager)
    }

    @After
    fun tearDown() {
        confirmVerified(permissionManager, callback)
    }

    @Test
    fun `invoke invokes callback when camera permission is granted`() {
        useCase(callback)
        verify(exactly = 0) { callback.invoke() }
        val permissionsGrantedCallbackSlot = slot<PermissionsGrantedCallback>()

        verify {
            permissionManager.handlePermissions(
                eq(listOf(Manifest.permission.CAMERA)),
                isNull(),
                capture(permissionsGrantedCallbackSlot),
                isNull(),
                isNull()
            )
        }
        permissionsGrantedCallbackSlot.captured.invoke(true)
        verify { callback.invoke() }
    }

    @Test
    fun `invoke does nothing when camera permission is not granted`() {
        useCase(callback)
        verify(exactly = 0) { callback.invoke() }
        val permissionsGrantedCallbackSlot = slot<PermissionsGrantedCallback>()

        verify {
            permissionManager.handlePermissions(
                eq(listOf(Manifest.permission.CAMERA)),
                isNull(),
                capture(permissionsGrantedCallbackSlot),
                isNull(),
                isNull()
            )
        }
        permissionsGrantedCallbackSlot.captured.invoke(false)
        verify(exactly = 0) { callback.invoke() }
    }
}
