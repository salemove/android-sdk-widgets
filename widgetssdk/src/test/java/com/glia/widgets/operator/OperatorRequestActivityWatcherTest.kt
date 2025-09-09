package com.glia.widgets.operator

import android.COMMON_EXTENSIONS_CLASS_PATH
import android.CONTEXT_EXTENSIONS_CLASS_PATH
import android.LOGGER_PATH
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.showToast
import com.glia.widgets.helper.withRuntimeTheme
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.view.Dialogs
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.invoke
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class OperatorRequestActivityWatcherTest {
    private val controllerState: PublishProcessor<OneTimeEvent<OperatorRequestContract.State>> = PublishProcessor.create()
    private lateinit var controller: OperatorRequestContract.Controller
    private lateinit var gliaActivityManager: GliaActivityManager
    private lateinit var activityLauncher: ActivityLauncher

    private lateinit var watcher: OperatorRequestActivityWatcher

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        mockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)

        Logger.setIsDebug(false)

        controller = mockk(relaxUnitFun = true)
        every { controller.state } returns controllerState
        gliaActivityManager = mockk(relaxUnitFun = true)
        activityLauncher = mockk(relaxed = true)

        watcher = OperatorRequestActivityWatcher(controller, activityLauncher, gliaActivityManager)

        verify { controller.state }
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()

        confirmVerified(controller, activityLauncher)
        unmockkStatic(LOGGER_PATH, CONTEXT_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `handleState does nothing when activity is finishing`() {
        mockLogger()
        val activityFinishing: Activity = mockk(relaxed = true)
        every { activityFinishing.isFinishing } returns true

        val state: OperatorRequestContract.State = OperatorRequestContract.State.OpenCallActivity(MediaType.VIDEO)

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
        fireState<ChatActivity>(
            controllerState,
            watcher,
            OperatorRequestContract.State.OpenCallActivity(MediaType.VIDEO)
        ) { event, activity ->
            verify { event.consume(any()) }
            verify { gliaActivityManager.finishActivities() }
            verify { activityLauncher.launchCall(any(), any<MediaType>(), eq(true)) }

            confirmVerified(activity, event)
        }
    }

    @Test
    fun `openCallActivity will start CallActivity when current activity is not a GliaActivity`() {
        fireState<Activity>(
            controllerState, watcher, OperatorRequestContract.State.OpenCallActivity(MediaType.VIDEO)
        ) { event, activity ->
            verify { event.consume(any()) }
            verify(exactly = 0) { gliaActivityManager.finishActivities() }
            verify { activityLauncher.launchCall(any(), any<MediaType>(), eq(true)) }

            confirmVerified(activity, event)
        }
    }

    @Test
    fun `openCallActivity will do nothing when current activity is Call Activity`() {
        fireState<CallActivity>(
            controllerState, watcher, OperatorRequestContract.State.OpenCallActivity(MediaType.VIDEO)
        ) { event, activity ->
            verify { event.consume(any()) }
            verify(exactly = 0) { gliaActivityManager.finishActivities() }
            verify(exactly = 0) { activityLauncher.launchCall(any(), any<MediaType>(), eq(true)) }

            confirmVerified(activity, event)
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
    fun `state DisplayToast will display toast message`() {
        val message = "_message"
        mockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
        every { any<ChatActivity>().showToast(any(), any()) } just Runs
        fireState<ChatActivity>(controllerState, watcher, OperatorRequestContract.State.DisplayToast(message)) { _, activity ->
            verify { activity.showToast(message, any()) }
        }
    }

    @Test
    fun `state OpenOverlayPermissionScreen will open overlay permissions screen when system can handle that intent`() {
        val onSuccessSlot = slot<() -> Unit>()
        fireState<ChatActivity>(
            controllerState,
            watcher,
            OperatorRequestContract.State.OpenOverlayPermissionScreen
        ) { _, activity ->
            verify { activityLauncher.launchOverlayPermission(activity, capture(onSuccessSlot), any()) }
            onSuccessSlot.invoke()
            verify { controller.overlayPermissionScreenOpened() }
            confirmVerified(activityLauncher)
        }
    }

    @Test
    fun `state OpenOverlayPermissionScreen will do nothing when system can't handle that intent`() {
        val onFailureSlot = slot<() -> Unit>()
        fireState<ChatActivity>(
            controllerState,
            watcher,
            OperatorRequestContract.State.OpenOverlayPermissionScreen
        ) { _, activity ->
            verify { activityLauncher.launchOverlayPermission(activity, any(), capture(onFailureSlot)) }
            onFailureSlot.invoke()
            verify { controller.failedToOpenOverlayPermissionScreen() }
            confirmVerified(activityLauncher)
        }
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
