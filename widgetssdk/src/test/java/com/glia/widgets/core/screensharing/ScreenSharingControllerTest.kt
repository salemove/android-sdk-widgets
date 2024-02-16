package com.glia.widgets.core.screensharing

import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.core.notification.domain.ShowScreenSharingNotificationUseCase
import com.glia.widgets.core.permissions.domain.HasScreenSharingNotificationChannelEnabledUseCase
import com.glia.widgets.engagement.ScreenSharingState
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import io.reactivex.processors.PublishProcessor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScreenSharingControllerTest {
    private lateinit var dialogController: DialogContract.Controller
    private lateinit var showScreenSharingNotificationUseCase: ShowScreenSharingNotificationUseCase
    private lateinit var removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase
    private lateinit var hasScreenSharingNotificationChannelEnabledUseCase: HasScreenSharingNotificationChannelEnabledUseCase
    private lateinit var screenSharingUseCase: ScreenSharingUseCase
    private lateinit var subjectUnderTest: ScreenSharingController
    private lateinit var configurationManager: GliaSdkConfigurationManager
    private val screenSharingState: PublishProcessor<ScreenSharingState> = PublishProcessor.create()

    @Before
    fun setUp() {
        dialogController = mock()
        showScreenSharingNotificationUseCase = mock()
        removeScreenSharingNotificationUseCase = mock()
        hasScreenSharingNotificationChannelEnabledUseCase = mock()
        configurationManager = mock {
            on { screenSharingMode } doReturn ScreenSharing.Mode.APP_BOUNDED
        }
        screenSharingUseCase = mock {
            on { invoke() } doReturn screenSharingState
        }
        subjectUnderTest = ScreenSharingController(
            screenSharingUseCase,
            dialogController,
            showScreenSharingNotificationUseCase,
            removeScreenSharingNotificationUseCase,
            hasScreenSharingNotificationChannelEnabledUseCase,
            configurationManager
        )
        verify(screenSharingUseCase).invoke()
    }

    @Test
    fun appropriate_methods_should_be_triggered_when_screen_sharing_state_receives() {
        val viewCallback: ScreenSharingContract.ViewCallback = mock()
        subjectUnderTest.setViewCallback(viewCallback)

        screenSharingState.onNext(ScreenSharingState.Ended)
        verify(viewCallback).onScreenSharingEnded()

        screenSharingState.onNext(ScreenSharingState.FailedToAcceptRequest("message"))
        verify(viewCallback).onScreenSharingRequestError(eq("message"))

        screenSharingState.onNext(ScreenSharingState.RequestAccepted)
        verify(viewCallback).onScreenSharingRequestSuccess()

        whenever(hasScreenSharingNotificationChannelEnabledUseCase()) doReturn true
        val operatorName = "Operator"
        screenSharingState.onNext(ScreenSharingState.Requested)
        verify(dialogController).showStartScreenSharingDialog()

        screenSharingState.onNext(ScreenSharingState.Started)
        verify(viewCallback).onScreenSharingStarted()

        screenSharingState.onNext(ScreenSharingState.RequestDeclined)
        verifyNoMoreInteractions(dialogController, viewCallback, screenSharingUseCase)

    }

    @Test
    fun onScreenSharingRequest_noInteractions_whenNoViewCallbacksAdded() {
        val operatorName = "Operator"
        subjectUnderTest.onScreenSharingRequest(operatorName)
        verifyNoInteractions(dialogController)
    }

    @Test
    fun onScreenSharingRequest_showsEnableNotificationsDialog_whenNotificationChannelDisabled() {
        val operatorName = "Operator"
        whenever(hasScreenSharingNotificationChannelEnabledUseCase()).thenReturn(false)
        subjectUnderTest.setViewCallback(mock())
        subjectUnderTest.onScreenSharingRequest(operatorName)
        verify(dialogController).showEnableScreenSharingNotificationsAndStartSharingDialog()
    }

    @Test
    fun onScreenSharingRequest_showsStartScreenSharingDialog_whenNotificationChannelEnabled() {
        val operatorName = "Operator"
        whenever(hasScreenSharingNotificationChannelEnabledUseCase()).thenReturn(true)
        subjectUnderTest.setViewCallback(mock())
        subjectUnderTest.onScreenSharingRequest(operatorName)
        verify(dialogController).showStartScreenSharingDialog()
    }

    @Test
    fun onScreenSharingRequestError_removesNotificationCallsOnScreenSharingRequestError() {
        val viewCallback: ScreenSharingContract.ViewCallback = mock()
        subjectUnderTest.setViewCallback(viewCallback)
        val message = "Error Message"
        subjectUnderTest.onScreenSharingRequestError(message)
        verify(viewCallback).onScreenSharingRequestError(message)
        verify(removeScreenSharingNotificationUseCase).invoke()
    }

    @Test
    fun onResume_acceptsScreenSharing_whenNotificationChannelEnabled() {
        subjectUnderTest.hasPendingScreenSharingRequest = true
        whenever(hasScreenSharingNotificationChannelEnabledUseCase()).thenReturn(true)
        subjectUnderTest.onResume(mock(), null)
        verify(showScreenSharingNotificationUseCase).invoke()
        verify(screenSharingUseCase).acceptRequest(anyOrNull(), anyOrNull())
    }

    @Test
    fun onResume_requestScreenSharingCallback_whenCallbackIsNotNull() {
        val requestScreenSharingCallback: () -> Unit = mock()
        subjectUnderTest.hasPendingScreenSharingRequest = true
        whenever(hasScreenSharingNotificationChannelEnabledUseCase()).thenReturn(true)
        subjectUnderTest.onResume(mock(), requestScreenSharingCallback)
        verify(requestScreenSharingCallback).invoke()
        verify(showScreenSharingNotificationUseCase, never()).invoke()
    }

    @Test
    fun onResume_showsEnableNotifications_whenNotificationChannelDisabled() {
        subjectUnderTest.hasPendingScreenSharingRequest = true
        whenever(hasScreenSharingNotificationChannelEnabledUseCase()).thenReturn(false)
        subjectUnderTest.onResume(mock(), null)
        verify(dialogController).showEnableScreenSharingNotificationsAndStartSharingDialog()
    }

    @Test
    fun onResume_doNothing_whenEnableScreenSharingNotificationsAndStartSharingDialogShown() {
        subjectUnderTest.hasPendingScreenSharingRequest = true
        whenever(hasScreenSharingNotificationChannelEnabledUseCase()).thenReturn(false)
        whenever(dialogController.isEnableScreenSharingNotificationsAndStartSharingDialogShown).thenReturn(true)
        subjectUnderTest.onResume(mock(), null)
        verify(dialogController, never()).showEnableScreenSharingNotificationsAndStartSharingDialog()
    }

    @Test
    fun onScreenSharingAccepted_acceptsScreenSharing() {
        subjectUnderTest.onScreenSharingAccepted(mock())
        verify(showScreenSharingNotificationUseCase).invoke()
        verify(screenSharingUseCase).acceptRequest(anyOrNull(), anyOrNull())
    }

    @Test
    fun onScreenSharingDeclined_declinesScreenSharing() {
        subjectUnderTest.onScreenSharingDeclined()
        verify(screenSharingUseCase).declineRequest()
    }

    @Test
    fun onScreenSharingNotificationEndPressed_hidesNotificationEndsScreenSharing() {
        subjectUnderTest.onScreenSharingNotificationEndPressed()
        verify(removeScreenSharingNotificationUseCase).invoke()
        verify(screenSharingUseCase).end()
    }
}
