package com.glia.widgets.core.permissions.domain

import android.Manifest
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.permissions.PermissionsGrantedCallback
import com.glia.widgets.push.notifications.IsPushNotificationsSetUpUseCase
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class RequestNotificationPermissionIfPushNotificationsSetUpUseCaseTest {
    private lateinit var permissionManager: PermissionManager
    private lateinit var isNotificationPermissionGrantedUseCase: IsNotificationPermissionGrantedUseCase
    private lateinit var pushNotificationsSetUpUseCase: IsPushNotificationsSetUpUseCase
    private lateinit var callback: () -> Unit
    private lateinit var useCase: RequestNotificationPermissionIfPushNotificationsSetUpUseCase

    @Before
    fun setUp() {
        permissionManager = mockk(relaxUnitFun = true)
        isNotificationPermissionGrantedUseCase = mockk()
        pushNotificationsSetUpUseCase = mockk()
        callback = mockk(relaxed = true)

        useCase = RequestNotificationPermissionIfPushNotificationsSetUpUseCaseImpl(
            permissionManager,
            isNotificationPermissionGrantedUseCase,
            pushNotificationsSetUpUseCase
        )
    }

    @After
    fun tearDown() {
        confirmVerified(
            permissionManager,
            isNotificationPermissionGrantedUseCase,
            pushNotificationsSetUpUseCase,
            callback
        )
    }

    @Test
    fun `invoke invokes callback when notification push notifications is setUp`() {
        every { pushNotificationsSetUpUseCase() } returns false
        useCase(callback)
        verify { pushNotificationsSetUpUseCase() }
        verify { callback() }
        verify(exactly = 0) {
            permissionManager.handlePermissions(
                additionalPermissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
                additionalPermissionsGrantedCallback = any()
            )
        }
        verify(exactly = 0) { isNotificationPermissionGrantedUseCase() }
    }


    @Test
    fun `invoke invokes callback when notification permission is granted`() {
        every { pushNotificationsSetUpUseCase() } returns true
        every { isNotificationPermissionGrantedUseCase() } returns true
        useCase(callback)
        verify { isNotificationPermissionGrantedUseCase() }
        verify { pushNotificationsSetUpUseCase() }
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
        every { pushNotificationsSetUpUseCase() } returns true
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
        verify { pushNotificationsSetUpUseCase() }
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
