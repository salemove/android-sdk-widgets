package com.glia.widgets.engagement.domain

import android.os.Build
import com.glia.widgets.internal.notification.domain.ShowScreenSharingNotificationUseCase
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
class PrepareToScreenSharingUseCaseTest {
    private lateinit var showScreenSharingNotificationUseCase: ShowScreenSharingNotificationUseCase
    private lateinit var startMediaProjectionServiceUseCase: StartMediaProjectionServiceUseCase
    private lateinit var informThatReadyToShareScreenUseCase: InformThatReadyToShareScreenUseCase

    private lateinit var useCase: PrepareToScreenSharingUseCase

    @Before
    fun setUp() {
        showScreenSharingNotificationUseCase = mockk(relaxUnitFun = true)
        startMediaProjectionServiceUseCase = mockk(relaxUnitFun = true)
        informThatReadyToShareScreenUseCase = mockk(relaxUnitFun = true)

        useCase = PrepareToScreenSharingUseCaseImpl(
            showScreenSharingNotificationUseCase,
            startMediaProjectionServiceUseCase,
            informThatReadyToShareScreenUseCase
        )
    }

    @After
    fun tearDown() {
        confirmVerified(showScreenSharingNotificationUseCase, startMediaProjectionServiceUseCase, informThatReadyToShareScreenUseCase)
    }

    @Test
    fun `invoke should call startMediaProjectionServiceUseCase when Build version is greater than or equal to O`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 26)
        useCase()

        verify { startMediaProjectionServiceUseCase() }
        verify(exactly = 0) { informThatReadyToShareScreenUseCase() }
        verify(exactly = 0) { showScreenSharingNotificationUseCase() }
    }

    @Test
    fun `invoke should call informThatReadyToShareScreenUseCase and showScreenSharingNotificationUseCase when Build version is less than O`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 25)
        useCase()

        verify { informThatReadyToShareScreenUseCase() }
        verify { showScreenSharingNotificationUseCase() }
        verify(exactly = 0) { startMediaProjectionServiceUseCase() }
    }
}
