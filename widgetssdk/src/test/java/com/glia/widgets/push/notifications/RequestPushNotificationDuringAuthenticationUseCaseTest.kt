package com.glia.widgets.push.notifications

import android.Manifest
import android.os.Build
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.view.dialog.DialogDispatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.VANILLA_ICE_CREAM])
class RequestPushNotificationDuringAuthenticationUseCaseTest {
    private lateinit var isPushNotificationsSetUpUseCase: IsPushNotificationsSetUpUseCase
    private lateinit var dialogDispatcher: DialogDispatcher
    private lateinit var permissionManager: PermissionManager
    private lateinit var configurationManager: ConfigurationManager

    private lateinit var useCase: RequestPushNotificationDuringAuthenticationUseCase

    @Before
    fun setUp() {
        isPushNotificationsSetUpUseCase = mockk()
        dialogDispatcher = mockk(relaxUnitFun = true)
        permissionManager = mockk(relaxUnitFun = true)
        configurationManager = mockk()

        useCase = RequestPushNotificationDuringAuthenticationUseCaseImpl(
            isPushNotificationsSetUpUseCase = isPushNotificationsSetUpUseCase,
            dialogDispatcher = dialogDispatcher,
            permissionManager = permissionManager,
            configurationManager = configurationManager
        )
    }

    @Test
    fun `invoke does nothing when suppressPushNotificationsPermissionRequestDuringAuthentication is true`() {
        every { configurationManager.suppressPushNotificationsPermissionRequestDuringAuthentication } returns true
        every { isPushNotificationsSetUpUseCase() } returns true

        useCase()

        verify(exactly = 0) { dialogDispatcher.showNotificationPermissionDialog(any(), any()) }
        verify(exactly = 0) { permissionManager.handlePermissions(any(), any(), any(), any(), any()) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S_V2])
    fun `invoke does nothing when SDK version is lower than Tiramisu`() {
        every { configurationManager.suppressPushNotificationsPermissionRequestDuringAuthentication } returns false
        every { isPushNotificationsSetUpUseCase() } returns true

        useCase()

        verify(exactly = 0) { dialogDispatcher.showNotificationPermissionDialog(any(), any()) }
        verify(exactly = 0) { permissionManager.handlePermissions(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke does nothing when push notifications are not set up`() {
        every { configurationManager.suppressPushNotificationsPermissionRequestDuringAuthentication } returns false
        every { isPushNotificationsSetUpUseCase() } returns false

        useCase()

        verify(exactly = 0) { dialogDispatcher.showNotificationPermissionDialog(any(), any()) }
        verify(exactly = 0) { permissionManager.handlePermissions(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke does nothing when push notifications permission is granted`() {
        every { configurationManager.suppressPushNotificationsPermissionRequestDuringAuthentication } returns false
        every { isPushNotificationsSetUpUseCase() } returns true
        every { permissionManager.hasPermission(Manifest.permission.POST_NOTIFICATIONS) } returns true

        useCase()

        verify(exactly = 0) { dialogDispatcher.showNotificationPermissionDialog(any(), any()) }
        verify(exactly = 0) { permissionManager.handlePermissions(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke shows intermediate dialog when shouldShowPermissionRationale is true`() {
        val onAllowSlot = slot<() -> Unit>()
        val onCancelSlot = slot<() -> Unit>()

        every { configurationManager.suppressPushNotificationsPermissionRequestDuringAuthentication } returns false
        every { isPushNotificationsSetUpUseCase() } returns true
        every { permissionManager.hasPermission(Manifest.permission.POST_NOTIFICATIONS) } returns false
        every { permissionManager.shouldShowPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) } returns true

        useCase()

        verify { permissionManager.shouldShowPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) }
        verify { dialogDispatcher.showNotificationPermissionDialog(capture(onAllowSlot), capture(onCancelSlot)) }

        onCancelSlot.captured.invoke()
        verify(exactly = 0) { permissionManager.handlePermissions(any(), any(), any(), any(), any()) }

        onAllowSlot.captured.invoke()
        verify { permissionManager.handlePermissions(any(), eq(listOf(Manifest.permission.POST_NOTIFICATIONS)), any(), any(), any()) }
    }

    @Test
    fun `invoke requests system permission when the permission is not granted`() {
        every { configurationManager.suppressPushNotificationsPermissionRequestDuringAuthentication } returns false
        every { isPushNotificationsSetUpUseCase() } returns true
        every { permissionManager.hasPermission(Manifest.permission.POST_NOTIFICATIONS) } returns false
        every { permissionManager.shouldShowPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) } returns false

        useCase()

        verify { permissionManager.shouldShowPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) }
        verify(exactly = 0) { dialogDispatcher.showNotificationPermissionDialog(any(), any()) }
        verify { permissionManager.handlePermissions(any(), eq(listOf(Manifest.permission.POST_NOTIFICATIONS)), any(), any(), any()) }
    }

}
