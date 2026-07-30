package com.glia.widgets.call

import com.glia.widgets.call.domain.HandleCallPermissionsUseCase
import com.glia.widgets.chat.domain.DecideOnQueueingUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.EndEngagementUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.EnqueueForEngagementUseCase
import com.glia.widgets.engagement.domain.FlipCameraButtonStateUseCase
import com.glia.widgets.engagement.domain.FlipVisitorCameraUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.IsMediaQualityPoorUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.engagement.domain.OperatorMediaUseCase
import com.glia.widgets.engagement.domain.ToggleVisitorAudioMediaStateUseCase
import com.glia.widgets.engagement.domain.ToggleVisitorVideoMediaStateUseCase
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.DeviceMonitor
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.internal.audio.domain.TurnSpeakerphoneUseCase
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.internal.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.internal.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.internal.engagement.domain.ShouldShowMediaEngagementViewUseCase
import com.glia.widgets.internal.notification.domain.CallNotificationUseCase
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.MinimizeHandler
import com.glia.widgets.webbrowser.domain.GetUrlFromLinkUseCase
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class CallControllerTest {
    private lateinit var callTimer: TimeCounter
    private lateinit var inactivityTimeCounter: TimeCounter
    private lateinit var connectingTimerCounter: TimeCounter
    private lateinit var minimizeHandler: MinimizeHandler
    private lateinit var dialogController: DialogContract.Controller
    private lateinit var messagesNotSeenHandler: MessagesNotSeenHandler
    private lateinit var callNotificationUseCase: CallNotificationUseCase
    private lateinit var endEngagementUseCase: EndEngagementUseCase
    private lateinit var shouldShowMediaEngagementViewUseCase: ShouldShowMediaEngagementViewUseCase
    private lateinit var isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase
    private lateinit var updateFromCallScreenUseCase: UpdateFromCallScreenUseCase
    private lateinit var isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase
    private lateinit var turnSpeakerphoneUseCase: TurnSpeakerphoneUseCase
    private lateinit var confirmationDialogUseCase: ConfirmationDialogUseCase
    private lateinit var confirmationDialogLinksUseCase: ConfirmationDialogLinksUseCase
    private lateinit var handleCallPermissionsUseCase: HandleCallPermissionsUseCase
    private lateinit var engagementStateUseCase: EngagementStateUseCase
    private lateinit var operatorMediaUseCase: OperatorMediaUseCase
    private lateinit var acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase
    private lateinit var visitorMediaUseCase: VisitorMediaUseCase
    private lateinit var toggleVisitorAudioMediaStateUseCase: ToggleVisitorAudioMediaStateUseCase
    private lateinit var toggleVisitorVideoMediaStateUseCase: ToggleVisitorVideoMediaStateUseCase
    private lateinit var flipVisitorCameraUseCase: FlipVisitorCameraUseCase
    private lateinit var flipCameraButtonStateUseCase: FlipCameraButtonStateUseCase
    private lateinit var isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
    private lateinit var enqueueForEngagementUseCase: EnqueueForEngagementUseCase
    private lateinit var decideOnQueueingUseCase: DecideOnQueueingUseCase
    private lateinit var getUrlFromLinkUseCase: GetUrlFromLinkUseCase
    private lateinit var deviceMonitor: DeviceMonitor
    private lateinit var isMediaQualityPoorUseCase: IsMediaQualityPoorUseCase

    private lateinit var callView: CallContract.View

    private lateinit var callController: CallController

    @Before
    fun setUp() {
        Logger.setIsDebug(false)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        callTimer = mock()
        inactivityTimeCounter = mock()
        connectingTimerCounter = mock()
        minimizeHandler = mock()
        dialogController = mock()
        messagesNotSeenHandler = mock()
        callNotificationUseCase = mock()
        endEngagementUseCase = mock()
        shouldShowMediaEngagementViewUseCase = mock()
        isShowOverlayPermissionRequestDialogUseCase = mock()
        updateFromCallScreenUseCase = mock()
        isCurrentEngagementCallVisualizerUseCase = mock()
        turnSpeakerphoneUseCase = mock()
        confirmationDialogUseCase = mock()
        confirmationDialogLinksUseCase = mock()
        handleCallPermissionsUseCase = mock()
        engagementStateUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }
        operatorMediaUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }
        acceptMediaUpgradeOfferUseCase = mock {
            on { result } doReturn Flowable.empty()
        }
        visitorMediaUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
            on { onHoldState } doReturn Flowable.empty()
        }
        toggleVisitorAudioMediaStateUseCase = mock()
        toggleVisitorVideoMediaStateUseCase = mock()
        flipVisitorCameraUseCase = mock()
        flipCameraButtonStateUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }
        isQueueingOrLiveEngagementUseCase = mock()
        enqueueForEngagementUseCase = mock()
        decideOnQueueingUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }
        getUrlFromLinkUseCase = mock()
        deviceMonitor = mock()
        isMediaQualityPoorUseCase = mock {
            on { invoke() } doReturn Flowable.empty()
        }

        callView = mock()

        callController = CallController(
            callTimer = callTimer,
            inactivityTimeCounter = inactivityTimeCounter,
            connectingTimerCounter = connectingTimerCounter,
            minimizeHandler = minimizeHandler,
            dialogController = dialogController,
            messagesNotSeenHandler = messagesNotSeenHandler,
            callNotificationUseCase = callNotificationUseCase,
            endEngagementUseCase = endEngagementUseCase,
            shouldShowMediaEngagementViewUseCase = shouldShowMediaEngagementViewUseCase,
            isShowOverlayPermissionRequestDialogUseCase = isShowOverlayPermissionRequestDialogUseCase,
            updateFromCallScreenUseCase = updateFromCallScreenUseCase,
            isCurrentEngagementCallVisualizerUseCase = isCurrentEngagementCallVisualizerUseCase,
            turnSpeakerphoneUseCase = turnSpeakerphoneUseCase,
            confirmationDialogUseCase = confirmationDialogUseCase,
            confirmationDialogLinksUseCase = confirmationDialogLinksUseCase,
            handleCallPermissionsUseCase = handleCallPermissionsUseCase,
            engagementStateUseCase = engagementStateUseCase,
            operatorMediaUseCase = operatorMediaUseCase,
            acceptMediaUpgradeOfferUseCase = acceptMediaUpgradeOfferUseCase,
            visitorMediaUseCase = visitorMediaUseCase,
            toggleVisitorAudioMediaStateUseCase = toggleVisitorAudioMediaStateUseCase,
            toggleVisitorVideoMediaStateUseCase = toggleVisitorVideoMediaStateUseCase,
            flipVisitorCameraUseCase = flipVisitorCameraUseCase,
            flipCameraButtonStateUseCase = flipCameraButtonStateUseCase,
            isQueueingOrLiveEngagementUseCase = isQueueingOrLiveEngagementUseCase,
            enqueueForEngagementUseCase = enqueueForEngagementUseCase,
            decideOnQueueingUseCase = decideOnQueueingUseCase,
            getUrlFromLinkUseCase = getUrlFromLinkUseCase,
            deviceMonitor = deviceMonitor,
            isMediaQualityPoorUseCase = isMediaQualityPoorUseCase
        )
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `setView registers the view and emits the current state`() {
        callController.setView(callView)

        assertEquals(callView, callController.getView())
        verify(callView).emitState(any())
    }

    @Test
    fun `onDestroy releases the view when retained so the host activity is not leaked`() {
        callController.setView(callView)

        callController.onDestroy(true)

        verify(callView).destroyView()
        assertNull(callController.getView())
    }

    @Test
    fun `onDestroy keeps timers and subscriptions when retained`() {
        callController.setView(callView)

        callController.onDestroy(true)

        verify(callTimer, never()).clear()
        verify(inactivityTimeCounter, never()).clear()
        verify(connectingTimerCounter, never()).clear()
        verify(minimizeHandler, never()).clear()
    }

    @Test
    fun `onDestroy releases the view and clears timers when not retained`() {
        callController.setView(callView)

        callController.onDestroy(false)

        verify(callView).destroyView()
        assertNull(callController.getView())
        verify(callTimer).clear()
        verify(inactivityTimeCounter).clear()
        verify(connectingTimerCounter).clear()
        verify(minimizeHandler).clear()
    }

    @Test
    fun `getView returns the latest view when a new view replaces the previous one`() {
        val newView = mock<CallContract.View>()
        callController.setView(callView)

        callController.setView(newView)

        assertEquals(newView, callController.getView())
    }
}
