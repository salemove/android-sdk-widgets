package com.glia.widgets.internal.permissions.domain

import com.glia.widgets.push.notifications.IsPushNotificationsSetUpUseCase
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class RequestNotificationPermissionIfPushNotificationsSetUpUseCaseTest {
    private lateinit var withNotificationPermissionUseCase: WithNotificationPermissionUseCase
    private lateinit var pushNotificationsSetUpUseCase: IsPushNotificationsSetUpUseCase
    private lateinit var callback: () -> Unit
    private lateinit var useCase: RequestNotificationPermissionIfPushNotificationsSetUpUseCase

    @Before
    fun setUp() {
        withNotificationPermissionUseCase = mockk(relaxUnitFun = true)
        pushNotificationsSetUpUseCase = mockk()
        callback = mockk(relaxed = true)

        useCase = RequestNotificationPermissionIfPushNotificationsSetUpUseCaseImpl(
            withNotificationPermissionUseCase,
            pushNotificationsSetUpUseCase
        )
    }

    @After
    fun tearDown() {
        confirmVerified(withNotificationPermissionUseCase, pushNotificationsSetUpUseCase, callback)
    }

    @Test
    fun `invoke invokes callback when notification push notifications is setUp`() {
        every { pushNotificationsSetUpUseCase() } returns false
        useCase(callback)
        verify { pushNotificationsSetUpUseCase() }
        verify { callback() }
        verify(exactly = 0) { withNotificationPermissionUseCase(any()) }
    }

    @Test
    fun `invoke invokes callback when permission request result is received`() {
        every { pushNotificationsSetUpUseCase() } returns true
        every { withNotificationPermissionUseCase(captureLambda()) } answers {
            firstArg<() -> Unit>().invoke()
        }
        useCase(callback)
        verify { pushNotificationsSetUpUseCase() }
        verify { callback() }
        verify { withNotificationPermissionUseCase(any()) }
    }

}
