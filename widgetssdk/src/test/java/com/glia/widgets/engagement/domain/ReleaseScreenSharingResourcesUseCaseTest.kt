package com.glia.widgets.engagement.domain

import android.os.Build
import com.glia.widgets.internal.notification.domain.RemoveScreenSharingNotificationUseCase
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
class ReleaseScreenSharingResourcesUseCaseTest {
    private lateinit var removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase
    private lateinit var stopMediaProjectionServiceUseCase: StopMediaProjectionServiceUseCase
    private lateinit var useCase: ReleaseScreenSharingResourcesUseCase

    @Before
    fun setUp() {
        removeScreenSharingNotificationUseCase = mockk(relaxUnitFun = true)
        stopMediaProjectionServiceUseCase = mockk(relaxUnitFun = true)

        useCase = ReleaseScreenSharingResourcesUseCaseImpl(removeScreenSharingNotificationUseCase, stopMediaProjectionServiceUseCase)
    }

    @After
    fun tearDown() {
        confirmVerified(removeScreenSharingNotificationUseCase, stopMediaProjectionServiceUseCase)
    }

    @Test
    fun `invoke will call removeScreenSharingNotificationUseCase when Build_VERSION_SDK_INT is less than O`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 25)

        useCase()

        verify { removeScreenSharingNotificationUseCase() }
        verify(exactly = 0) { stopMediaProjectionServiceUseCase() }
    }

    @Test
    fun `invoke will call stopMediaProjectionServiceUseCase when Build_VERSION_SDK_INT is greater than or equal to O`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 26)

        useCase()

        verify { stopMediaProjectionServiceUseCase() }
        verify(exactly = 0) { removeScreenSharingNotificationUseCase() }
    }
}
