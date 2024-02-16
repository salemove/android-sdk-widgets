package com.glia.widgets.operator

import android.COMMON_EXTENSIONS_CLASS_PATH
import android.CONTEXT_EXTENSIONS_CLASS_PATH
import android.LOGGER_PATH
import android.NOTIFICATION_EXTENSIONS_PATH
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.core.notification.openNotificationChannelScreen
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.IntentConfigurationHelper
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.withRuntimeTheme
import com.glia.widgets.view.Dialogs
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class OperatorRequestActivityWatcherTest {
    private val controllerState: PublishProcessor<OneTimeEvent<OperatorRequestContract.State>> = PublishProcessor.create()
    private lateinit var controller: OperatorRequestContract.Controller
    private lateinit var gliaActivityManager: GliaActivityManager
    private lateinit var intentConfigurationHelper: IntentConfigurationHelper

    private lateinit var watcher: OperatorRequestActivityWatcher

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        mockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)

        Logger.setIsDebug(false)

        controller = mockk(relaxUnitFun = true)
        every { controller.state } returns controllerState
        gliaActivityManager = mockk(relaxUnitFun = true)
        intentConfigurationHelper = mockk(relaxUnitFun = true)

        watcher = OperatorRequestActivityWatcher(controller, intentConfigurationHelper, gliaActivityManager)

        verify { controller.state }
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()

        confirmVerified(controller, intentConfigurationHelper)
        unmockkStatic(LOGGER_PATH, CONTEXT_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `handleState does nothing when activity is finishing`() {
        mockLogger()
        val activityFinishing: Activity = mockk(relaxed = true)
        every { activityFinishing.isFinishing } returns true

        val state: OperatorRequestContract.State = OperatorRequestContract.State.OpenCallActivity(Engagement.MediaType.VIDEO)

        val event: OneTimeEvent<OperatorRequestContract.State> = mockk()
        every { event.value } returns state
        every { event.consumed } returns false

        watcher.onActivityResumed(activityFinishing)
        controllerState.onNext(event)

        verify { event.value }
        verify { event.consumed }
        verify { activityFinishing.isFinishing }
        verify { Logger.d(any(), any()) }

        confirmVerified(activityFinishing, event)
    }

    @Test
    fun `handleState does nothing when state is consumed`() {
        mockLogger()
        val activityFinishing: Activity = mockk(relaxed = true)
        every { activityFinishing.isFinishing } returns false

        val event: OneTimeEvent<OperatorRequestContract.State> = mockk()
        every { event.consumed } returns true
        every { event.value } returns mockk()

        watcher.onActivityResumed(activityFinishing)
        controllerState.onNext(event)

        verify { event.value }
        verify { event.consumed }
        verify(exactly = 0) { activityFinishing.isFinishing }
        verify { Logger.d(any(), any()) }

        confirmVerified(activityFinishing, event)
    }

    @Test
    fun `openCallActivity will start CallActivity when current activity is not a CallActivity`() {
        val intent: Intent = mockk(relaxed = true)
        every { intentConfigurationHelper.createForCall(any(), any(), any()) } returns intent

        fireState<ChatActivity>(
            controllerState, watcher, OperatorRequestContract.State.OpenCallActivity(Engagement.MediaType.VIDEO)
        ) { event, activity ->
            verify { event.consume(any()) }
            verify { gliaActivityManager.finishActivities() }
            verify { intentConfigurationHelper.createForCall(any(), any(), any()) }
            verify { activity.startActivity(intent) }

            confirmVerified(intent, activity, event)
        }
    }

    @Test
    fun `openCallActivity will start CallActivity when current activity is not a GliaActivity`() {
        val intent: Intent = mockk(relaxed = true)
        every { intentConfigurationHelper.createForCall(any(), any(), any()) } returns intent

        fireState<Activity>(
            controllerState, watcher, OperatorRequestContract.State.OpenCallActivity(Engagement.MediaType.VIDEO)
        ) { event, activity ->
            verify { event.consume(any()) }
            verify(exactly = 0) { gliaActivityManager.finishActivities() }
            verify { intentConfigurationHelper.createForCall(any(), any(), any()) }
            verify { activity.startActivity(intent) }

            confirmVerified(intent, activity, event)
        }
    }

    @Test
    fun `openCallActivity will do nothing when current activity is Call Activity`() {
        val intent: Intent = mockk(relaxed = true)
        every { intentConfigurationHelper.createForCall(any(), any(), any()) } returns intent

        fireState<CallActivity>(
            controllerState, watcher, OperatorRequestContract.State.OpenCallActivity(Engagement.MediaType.VIDEO)
        ) { event, activity ->
            verify { event.consume(any()) }
            verify(exactly = 0) { gliaActivityManager.finishActivities() }
            verify(exactly = 0) { intentConfigurationHelper.createForCall(any(), any(), any()) }
            verify(exactly = 0) { activity.startActivity(intent) }

            confirmVerified(intent, activity, event)
        }
    }

    @Test
    fun `RequestMediaUpgrade will open holder activity when activity is not a Glia activity`() {
        fireState<Activity>(
            controllerState, watcher, OperatorRequestContract.State.RequestMediaUpgrade(mockk())
        ) { event, activity ->
            verify(exactly = 0) { event.markConsumed() }
            verify { activity.packageName }
            verify { activity.startActivity(any()) }

            confirmVerified(activity, event)
        }
    }

    @Test
    fun `RequestMediaUpgrade will open show dialog when activity is a Glia activity`() {
        mockkObject(Dialogs)
        val dialog: AlertDialog = mockk(relaxed = true)
        every { Dialogs.showUpgradeDialog(any(), any(), any(), any(), any()) } returns dialog
        val offer: MediaUpgradeOffer = mockk(relaxed = true)
        val data: MediaUpgradeOfferData = mockk(relaxed = true)
        every { data.offer } returns offer
        val state = OperatorRequestContract.State.RequestMediaUpgrade(data)

        fireState<ChatActivity>(
            controllerState, watcher, state
        ) { event, activity ->

            verify(exactly = 0) { event.markConsumed() }
            verify { activity.withRuntimeTheme(any()) }

            val onAcceptSlot = slot<View.OnClickListener>()
            val onDeclineSlot = slot<View.OnClickListener>()
            verify { Dialogs.showUpgradeDialog(activity, any(), data, capture(onAcceptSlot), capture(onDeclineSlot)) }

            onAcceptSlot.captured.onClick(mockk())
            verify { event.markConsumed() }
            verify { data.offer }
            verify { controller.onMediaUpgradeAccepted(offer, activity) }

            confirmVerified(dialog, offer, data, event, activity)
        }

        unmockkObject(Dialogs)
    }

    @Test
    fun `RequestMediaUpgrade will decline offer when it dialog is declined`() {
        mockkObject(Dialogs)
        mockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
        val dialog: AlertDialog = mockk(relaxed = true)
        every { Dialogs.showUpgradeDialog(any(), any(), any(), any(), any()) } returns dialog
        val offer: MediaUpgradeOffer = mockk(relaxed = true)
        val data: MediaUpgradeOfferData = mockk(relaxed = true)
        every { data.offer } returns offer
        val state = OperatorRequestContract.State.RequestMediaUpgrade(data)

        fireState<ChatActivity>(
            controllerState, watcher, state
        ) { event, activity ->

            verify(exactly = 0) { event.markConsumed() }
            verify { activity.withRuntimeTheme(any()) }

            val onAcceptSlot = slot<View.OnClickListener>()
            val onDeclineSlot = slot<View.OnClickListener>()
            verify { Dialogs.showUpgradeDialog(activity, any(), data, capture(onAcceptSlot), capture(onDeclineSlot)) }

            onDeclineSlot.captured.onClick(mockk())
            verify { event.markConsumed() }
            verify { data.offer }
            verify { controller.onMediaUpgradeDeclined(offer, activity) }

            confirmVerified(dialog, offer, data, event, activity)
        }

        unmockkObject(Dialogs)
    }

    @Test
    fun `DismissAlertDialog will dismiss dialog`() {
        mockkObject(Dialogs)
        val dialog: AlertDialog = mockk(relaxed = true)
        every { Dialogs.showUpgradeDialog(any(), any(), any(), any(), any()) } returns dialog
        val offer: MediaUpgradeOffer = mockk(relaxed = true)
        val data: MediaUpgradeOfferData = mockk(relaxed = true)
        every { data.offer } returns offer
        val state = OperatorRequestContract.State.RequestMediaUpgrade(data)

        fireState<ChatActivity>(
            controllerState, watcher, state
        ) { event, activity ->

            verify(exactly = 0) { event.markConsumed() }
            verify { activity.withRuntimeTheme(any()) }

            val onAcceptSlot = slot<View.OnClickListener>()
            val onDeclineSlot = slot<View.OnClickListener>()
            verify { Dialogs.showUpgradeDialog(activity, any(), data, capture(onAcceptSlot), capture(onDeclineSlot)) }

            val dismissEvent: OneTimeEvent<OperatorRequestContract.State> = mockk(relaxed = true)
            every { dismissEvent.value } returns OperatorRequestContract.State.DismissAlertDialog
            every { dismissEvent.consume(captureLambda()) } answers {
                firstArg<OperatorRequestContract.State.() -> Unit>().invoke(state)
            }
            controllerState.onNext(dismissEvent)

            verify { dismissEvent.value }
            verify { activity.isFinishing }
            verify { dismissEvent.consume(any()) }
            verify { dialog.dismiss() }

            confirmVerified(dialog, offer, data, event, activity)
        }

        unmockkObject(Dialogs)
    }

    @Test
    fun `state WaitForNotificationScreenOpen will trigger controller onNotificationScreenOpened when activity is null`() {
        val event = createMockEvent(OperatorRequestContract.State.WaitForNotificationScreenOpen)
        watcher.onActivityPaused(mockk())
        controllerState.onNext(event)

        verify { event.consumed }
        verify { controller.onNotificationScreenOpened() }
    }

    @Test
    fun `state WaitForNotificationScreenOpen will do nothing when activity is not null`() {
        fireState<ChatActivity>(controllerState, watcher, OperatorRequestContract.State.WaitForNotificationScreenOpen) { _, _ ->
            verify(exactly = 0) { controller.onNotificationScreenOpened() }
        }
    }

    @Test
    fun `showEnableScreenSharingNotifications will call onShowEnableScreenSharingNotificationsAccepted when dialog is accepted`() {
        mockkObject(Dialogs)
        fireState<ChatActivity>(
            controllerState,
            watcher,
            OperatorRequestContract.State.EnableScreenSharingNotificationsAndStartSharing
        ) { state, activity ->
            val onAcceptSlot = slot<View.OnClickListener>()
            val onDeclineSlot = slot<View.OnClickListener>()
            verify {
                Dialogs.showAllowScreenSharingNotificationsAndStartSharingDialog(
                    activity,
                    any(),
                    capture(onAcceptSlot),
                    capture(onDeclineSlot)
                )
            }
            onAcceptSlot.captured.onClick(mockk())

            verify { state.markConsumed() }
            verify { controller.onShowEnableScreenSharingNotificationsAccepted() }
        }
        unmockkObject(Dialogs)
    }

    @Test
    fun `showEnableScreenSharingNotifications will call onShowEnableScreenSharingNotificationsDeclined when dialog is declined`() {
        mockkObject(Dialogs)
        fireState<ChatActivity>(
            controllerState,
            watcher,
            OperatorRequestContract.State.EnableScreenSharingNotificationsAndStartSharing
        ) { state, activity ->
            val onAcceptSlot = slot<View.OnClickListener>()
            val onDeclineSlot = slot<View.OnClickListener>()
            verify {
                Dialogs.showAllowScreenSharingNotificationsAndStartSharingDialog(
                    activity,
                    any(),
                    capture(onAcceptSlot),
                    capture(onDeclineSlot)
                )
            }
            onDeclineSlot.captured.onClick(mockk())

            verify { state.markConsumed() }
            verify { controller.onShowEnableScreenSharingNotificationsDeclined(activity) }
        }
        unmockkObject(Dialogs)
    }

    @Test
    fun `showScreenSharingDialog will call onScreenSharingDialogAccepted when dialog is accepted`() {
        mockkObject(Dialogs)
        fireState<ChatActivity>(
            controllerState,
            watcher,
            OperatorRequestContract.State.ShowScreenSharingDialog("operator_name")
        ) { state, activity ->
            val onAcceptSlot = slot<View.OnClickListener>()
            val onDeclineSlot = slot<View.OnClickListener>()
            verify {
                Dialogs.showScreenSharingDialog(
                    activity,
                    any(),
                    "operator_name",
                    capture(onAcceptSlot),
                    capture(onDeclineSlot)
                )
            }
            onAcceptSlot.captured.onClick(mockk())

            verify { state.markConsumed() }
            verify { controller.onScreenSharingDialogAccepted(activity) }
        }
        unmockkObject(Dialogs)
    }

    @Test
    fun `showScreenSharingDialog will call onScreenSharingDialogDeclined when dialog is declined`() {
        mockkObject(Dialogs)
        fireState<ChatActivity>(
            controllerState,
            watcher,
            OperatorRequestContract.State.ShowScreenSharingDialog("operator_name")
        ) { state, activity ->
            val onAcceptSlot = slot<View.OnClickListener>()
            val onDeclineSlot = slot<View.OnClickListener>()
            verify {
                Dialogs.showScreenSharingDialog(
                    activity,
                    any(),
                    "operator_name",
                    capture(onAcceptSlot),
                    capture(onDeclineSlot)
                )
            }
            onDeclineSlot.captured.onClick(mockk())

            verify { state.markConsumed() }
            verify { controller.onScreenSharingDialogDeclined(activity) }
        }
        unmockkObject(Dialogs)
    }

    @Test
    fun `openNotificationsScreen will open notification screen`() {
        mockkStatic(NOTIFICATION_EXTENSIONS_PATH)
        every { any<Activity>().openNotificationChannelScreen() } just Runs
        fireState<ChatActivity>(
            controllerState,
            watcher,
            OperatorRequestContract.State.OpenNotificationsScreen
        ) { state, activity ->
            verify { activity.openNotificationChannelScreen() }
            verify { state.consume(any()) }
            verify { controller.onNotificationScreenRequested() }
        }
        unmockkStatic(NOTIFICATION_EXTENSIONS_PATH)
    }

    private fun mockLogger() {
        mockkStatic(LOGGER_PATH)
        every { Logger.d(any(), any()) } just Runs
    }
}

internal inline fun <reified T : Activity> fireState(
    controllerState: PublishProcessor<OneTimeEvent<OperatorRequestContract.State>>,
    watcher: OperatorRequestActivityWatcher,
    state: OperatorRequestContract.State,
    callback: (OneTimeEvent<OperatorRequestContract.State>, T) -> Unit
) {
    val activity = mockk<T>(relaxed = true)
    every { activity.isFinishing } returns false

    every { any<Activity>().withRuntimeTheme(captureLambda()) } answers {
        secondArg<(Context, UiTheme) -> Unit>().invoke(activity, UiTheme())
    }

    val event: OneTimeEvent<OperatorRequestContract.State> = createMockEvent(state)

    watcher.onActivityResumed(activity)
    controllerState.onNext(event)

    verify { activity.isFinishing }
    verify { event.value }
    verify { event.consumed }

    callback(event, activity)
}

private fun createMockEvent(state: OperatorRequestContract.State): OneTimeEvent<OperatorRequestContract.State> {
    val event: OneTimeEvent<OperatorRequestContract.State> = mockk(relaxed = true)
    every { event.value } returns state
    every { event.consume(captureLambda()) } answers {
        firstArg<OperatorRequestContract.State.() -> Unit>().invoke(state)
    }
    return event
}
