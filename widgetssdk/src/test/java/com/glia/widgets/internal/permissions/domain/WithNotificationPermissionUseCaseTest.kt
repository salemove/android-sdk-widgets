package com.glia.widgets.internal.permissions.domain

import android.Manifest
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.permissions.PermissionsGrantedCallback
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class WithNotificationPermissionUseCaseTest {

    private lateinit var permissionManager: PermissionManager
    private lateinit var isNotificationPermissionGrantedUseCase: IsNotificationPermissionGrantedUseCase
    private lateinit var callback: () -> Unit
    private lateinit var useCase: WithNotificationPermissionUseCase

    @Before
    fun setUp() {
        permissionManager = mockk(relaxUnitFun = true)
        isNotificationPermissionGrantedUseCase = mockk()
        callback = mockk(relaxed = true)

        useCase = WithNotificationPermissionUseCaseImpl(permissionManager, isNotificationPermissionGrantedUseCase)
    }

    @After
    fun tearDown() {
        confirmVerified(permissionManager, isNotificationPermissionGrantedUseCase, callback)
    }

    @Test
    fun `invoke invokes callback when notification permission is granted`() {
        every { isNotificationPermissionGrantedUseCase() } returns true
        useCase(callback)
        verify { isNotificationPermissionGrantedUseCase() }
        verify { callback() }
        verify(exactly = 0) {
            permissionManager.handlePermissions(
                additionalPermissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
                additionalPermissionsGrantedCallback = any()
            )
        }
    }

    @Test
    fun `invoke invokes callback when permission request result is received`() {
        every { isNotificationPermissionGrantedUseCase() } returns false
        every {
            permissionManager.handlePermissions(
                additionalPermissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
                additionalPermissionsGrantedCallback = captureLambda()
            )
        } answers {
            arg<PermissionsGrantedCallback>(3).invoke(true)
        }
        useCase(callback)
        verify { isNotificationPermissionGrantedUseCase() }
        verify { callback() }
        verify {
            permissionManager.handlePermissions(
                additionalPermissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
                additionalPermissionsGrantedCallback = any()
            )
        }
    }

}
