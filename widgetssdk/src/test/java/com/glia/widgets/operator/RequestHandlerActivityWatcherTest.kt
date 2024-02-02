package com.glia.widgets.operator

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.IntentConfigurationHelper
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.runtimeTheme
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
import io.reactivex.processors.PublishProcessor
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

const val DIALOG_HOLDER_ACTIVITY_PATH = "com.glia.widgets.view.dialog.holder.DialogHolderActivity"
const val LOGGER_PATH = "com.glia.widgets.helper.Logger"

@RunWith(RobolectricTestRunner::class)
class RequestHandlerActivityWatcherTest {
    private val controllerState: PublishProcessor<OneTimeEvent<RequestHandlerContract.State>> = PublishProcessor.create()
    private lateinit var controller: RequestHandlerContract.Controller
    private lateinit var gliaActivityManager: GliaActivityManager
    private lateinit var intentConfigurationHelper: IntentConfigurationHelper

    private lateinit var watcher: RequestHandlerActivityWatcher

    @Before
    fun setUp() {
        Logger.setIsDebug(false)

        controller = mockk(relaxUnitFun = true)
        every { controller.state } returns controllerState
        gliaActivityManager = mockk(relaxUnitFun = true)
        intentConfigurationHelper = mockk(relaxUnitFun = true)

        watcher = RequestHandlerActivityWatcher(controller, gliaActivityManager, intentConfigurationHelper)

        verify { controller.state }
    }

    @After
    fun tearDown() {
        confirmVerified(controller, gliaActivityManager, intentConfigurationHelper)
        unmockkStatic(DIALOG_HOLDER_ACTIVITY_PATH, LOGGER_PATH)
    }

    @Test
    fun `onActivityCreated adds activity to gliaManager`() {
        val activity: CallActivity = mockk()
        watcher.onActivityCreated(activity, null)
        verify { gliaActivityManager.onActivityCreated(activity) }
        verify(exactly = 0) { gliaActivityManager.onActivityDestroyed(activity) }

        confirmVerified(activity)
    }

    @Test
    fun `onActivityDestroyed remove activity from gliaManager`() {
        val activity: CallActivity = mockk()
        watcher.onActivityDestroyed(activity)
        verify(exactly = 0) { gliaActivityManager.onActivityCreated(activity) }
        verify { gliaActivityManager.onActivityDestroyed(activity) }

        confirmVerified(activity)
    }

    @Test
    fun `handleState does nothing when activity is finishing`() {
        mockLogger()
        val activityFinishing: Activity = mockk(relaxed = true)
        every { activityFinishing.isFinishing } returns true

        val state: RequestHandlerContract.State = RequestHandlerContract.State.OpenCallActivity(Engagement.MediaType.VIDEO)

        val event: OneTimeEvent<RequestHandlerContract.State> = mockk()
        every { event.view() } returns state

        watcher.onActivityResumed(activityFinishing)
        controllerState.onNext(event)

        verify { event.view() }
        verify { activityFinishing.isFinishing }
        verify { Logger.d(any(), any()) }
        verify { activityFinishing.toString() }

        confirmVerified(activityFinishing, event)
    }

    @Test
    fun `handleState does nothing when state is null`() {
        mockLogger()
        val activityFinishing: Activity = mockk(relaxed = true)
        every { activityFinishing.isFinishing } returns false

        val event: OneTimeEvent<RequestHandlerContract.State> = mockk()
        every { event.view() } returns null

        watcher.onActivityResumed(activityFinishing)
        controllerState.onNext(event)

        verify { event.view() }
        verify(exactly = 0) { activityFinishing.isFinishing }
        verify { Logger.d(any(), any()) }
        verify { activityFinishing.toString() }

        confirmVerified(activityFinishing, event)
    }

    @Test
    fun `openCallActivity will start CallActivity when current activity is not a CallActivity`() {
        val intent: Intent = mockk(relaxed = true)
        every { intentConfigurationHelper.createForCall(any(), any(), any()) } returns intent

        fireState<ChatActivity>(
            controllerState,
            watcher,
            RequestHandlerContract.State.OpenCallActivity(Engagement.MediaType.VIDEO)
        ) { event, activity ->
            verify { event.markConsumed() }
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
            controllerState,
            watcher,
            RequestHandlerContract.State.OpenCallActivity(Engagement.MediaType.VIDEO)
        ) { event, activity ->
            verify { event.markConsumed() }
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
            controllerState,
            watcher,
            RequestHandlerContract.State.OpenCallActivity(Engagement.MediaType.VIDEO)
        ) { event, activity ->
            verify { event.markConsumed() }
            verify(exactly = 0) { gliaActivityManager.finishActivities() }
            verify(exactly = 0) { intentConfigurationHelper.createForCall(any(), any(), any()) }
            verify(exactly = 0) { activity.startActivity(intent) }

            confirmVerified(intent, activity, event)
        }
    }

    @Test
    fun `RequestMediaUpgrade will open holder activity when activity is not a Glia activity`() {
        fireState<Activity>(
            controllerState,
            watcher,
            RequestHandlerContract.State.RequestMediaUpgrade(mockk())
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
        val state = RequestHandlerContract.State.RequestMediaUpgrade(data)
        val uiTheme = UiTheme()

        fireState<ChatActivity>(
            controllerState,
            watcher,
            state
        ) { event, activity ->
            every { activity.runtimeTheme } returns uiTheme

            val uiThreadSlot = slot<Runnable>()
            verify { activity.runOnUiThread(capture(uiThreadSlot)) }
            uiThreadSlot.captured.run()

            verify(exactly = 0) { event.markConsumed() }
            verify { activity.runtimeTheme }

            val onAcceptSlot = slot<View.OnClickListener>()
            val onDeclineSlot = slot<View.OnClickListener>()
            verify { Dialogs.showUpgradeDialog(activity, uiTheme, data, capture(onAcceptSlot), capture(onDeclineSlot)) }

            onAcceptSlot.captured.onClick(mockk())
            verify { activity.obtainStyledAttributes(any(), any(), any(), any()) }
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
        val dialog: AlertDialog = mockk(relaxed = true)
        every { Dialogs.showUpgradeDialog(any(), any(), any(), any(), any()) } returns dialog
        val offer: MediaUpgradeOffer = mockk(relaxed = true)
        val data: MediaUpgradeOfferData = mockk(relaxed = true)
        every { data.offer } returns offer
        val state = RequestHandlerContract.State.RequestMediaUpgrade(data)
        val uiTheme = UiTheme()

        fireState<ChatActivity>(
            controllerState,
            watcher,
            state
        ) { event, activity ->
            every { activity.runtimeTheme } returns uiTheme

            val uiThreadSlot = slot<Runnable>()
            verify { activity.runOnUiThread(capture(uiThreadSlot)) }
            uiThreadSlot.captured.run()

            verify(exactly = 0) { event.markConsumed() }
            verify { activity.runtimeTheme }

            val onAcceptSlot = slot<View.OnClickListener>()
            val onDeclineSlot = slot<View.OnClickListener>()
            verify { Dialogs.showUpgradeDialog(activity, uiTheme, data, capture(onAcceptSlot), capture(onDeclineSlot)) }

            onDeclineSlot.captured.onClick(mockk())
            verify { activity.obtainStyledAttributes(any(), any(), any(), any()) }
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
        val state = RequestHandlerContract.State.RequestMediaUpgrade(data)
        val uiTheme = UiTheme()

        fireState<ChatActivity>(
            controllerState,
            watcher,
            state
        ) { event, activity ->
            every { activity.runtimeTheme } returns uiTheme

            val uiThreadSlot = slot<Runnable>()
            verify { activity.runOnUiThread(capture(uiThreadSlot)) }
            uiThreadSlot.captured.run()

            verify(exactly = 0) { event.markConsumed() }
            verify { activity.runtimeTheme }

            val onAcceptSlot = slot<View.OnClickListener>()
            val onDeclineSlot = slot<View.OnClickListener>()
            verify { Dialogs.showUpgradeDialog(activity, uiTheme, data, capture(onAcceptSlot), capture(onDeclineSlot)) }

            verify { activity.obtainStyledAttributes(any(), any(), any(), any()) }

            val dismissEvent: OneTimeEvent<RequestHandlerContract.State> = mockk(relaxed = true)
            every { dismissEvent.view() } returns RequestHandlerContract.State.DismissAlertDialog
            controllerState.onNext(dismissEvent)

            verify { dismissEvent.view() }
            verify { activity.isFinishing }
            verify { dismissEvent.markConsumed() }
            verify { dialog.dismiss() }

            confirmVerified(dialog, offer, data, event, activity)
        }

        unmockkObject(Dialogs)
    }

    private fun mockLogger() {
        mockkStatic(LOGGER_PATH)
        every { Logger.d(any(), any()) } just Runs
    }
}

internal inline fun <reified T : Activity> fireState(
    controllerState: PublishProcessor<OneTimeEvent<RequestHandlerContract.State>>,
    watcher: RequestHandlerActivityWatcher,
    state: RequestHandlerContract.State,
    callback: (OneTimeEvent<RequestHandlerContract.State>, T) -> Unit
) {
    val activity = mockk<T>(relaxed = true)
    every { activity.isFinishing } returns false

    val event: OneTimeEvent<RequestHandlerContract.State> = mockk(relaxUnitFun = true)
    every { event.view() } returns state

    watcher.onActivityResumed(activity)
    controllerState.onNext(event)

    verify { activity.isFinishing }
    verify { event.view() }

    callback(event, activity)
}
