package com.glia.widgets.engagement.completion

import android.CONTEXT_EXTENSIONS_CLASS_PATH
import android.LOGGER_PATH
import android.app.Activity
import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.withRuntimeTheme
import com.glia.widgets.launcher.ActivityLauncher
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
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class EngagementCompletionActivityWatcherTest {
    private val stateProcessor: PublishProcessor<OneTimeEvent<EngagementCompletionState>> = PublishProcessor.create()
    private lateinit var dialog: AlertDialog

    private lateinit var gliaActivityManager: GliaActivityManager
    private lateinit var controller: EngagementCompletionContract.Controller

    private lateinit var watcher: EngagementCompletionActivityWatcher
    private lateinit var activityLauncher: ActivityLauncher

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        mockkObject(Dialogs)
        mockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)

        dialog = mockk(relaxed = true)
        every { Dialogs.showOperatorEndedEngagementDialog(any(), any(), any()) } returns dialog
        every { Dialogs.showNoMoreOperatorsAvailableDialog(any(), any(), any()) } returns dialog
        every { Dialogs.showUnexpectedErrorDialog(any(), any(), any()) } returns dialog

        gliaActivityManager = mockk(relaxed = true)

        controller = mockk(relaxUnitFun = true) {
            every { state } returns stateProcessor
        }

        activityLauncher = mockk(relaxUnitFun = true)

        watcher = EngagementCompletionActivityWatcher(controller, gliaActivityManager, activityLauncher)

        verify { controller.state }
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()

        confirmVerified(controller)

        unmockkObject(Dialogs)
        unmockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `handleState will do nothing when event is consumed`() {
        mockkStatic(LOGGER_PATH)
        Logger.setIsDebug(false)
        every { Logger.d(any(), any()) } just Runs

        val event = createMockEvent(EngagementCompletionState.EngagementEnded(true, false, Engagement.ActionOnEnd.END_NOTIFICATION))
        every { event.consumed } returns true

        val activity = mockkActivity<Activity>()

        watcher.onActivityResumed(activity)
        stateProcessor.onNext(event)

        verify { event.consumed }
        verify { Logger.d(any(), any()) }
        verify(exactly = 2) { event.value }

        verify(exactly = 0) { event.consume(any()) }
        verify(exactly = 0) { Dialogs.showOperatorEndedEngagementDialog(any(), any(), any()) }
        verify(exactly = 0) { Dialogs.showNoMoreOperatorsAvailableDialog(any(), any(), any()) }
        verify(exactly = 0) { Dialogs.showUnexpectedErrorDialog(any(), any(), any()) }

        verify(exactly = 0) { activity.startActivity(any()) }
        verify(exactly = 0) { gliaActivityManager.finishActivities() }

        confirmVerified(event, activity)

        unmockkStatic(LOGGER_PATH)
    }

    @Test
    fun `handleState will finish activities when state is EngagementEnded even if activity is null or finishing`() {
        val event = createMockEvent(EngagementCompletionState.EngagementEnded(true, false, Engagement.ActionOnEnd.END_NOTIFICATION))
        val activity = mockkActivity<Activity>()

        every { activity.isFinishing } returns true

        watcher.onActivityResumed(activity)
        stateProcessor.onNext(event)

        verify { event.value }
        verify { event.consumed }
        verify { event.consume(any()) }

        verify { gliaActivityManager.finishActivities() }

        verify(exactly = 0) { Dialogs.showOperatorEndedEngagementDialog(any(), any(), any()) }
        verify(exactly = 0) { Dialogs.showNoMoreOperatorsAvailableDialog(any(), any(), any()) }
        verify(exactly = 0) { Dialogs.showUnexpectedErrorDialog(any(), any(), any()) }

        verify(exactly = 0) { activity.startActivity(any()) }

        confirmVerified(event)
    }

    @Test
    fun `handleState will do nothing when event is not QueuingOrEngagementEnded and Activity is null or finishing`() {
        mockkStatic(LOGGER_PATH)
        Logger.setIsDebug(false)
        every { Logger.d(any(), any()) } just Runs

        val event = createMockEvent(EngagementCompletionState.QueueUnstaffed)

        val activity = mockkActivity<Activity>()
        every { activity.isFinishing } returns true

        watcher.onActivityResumed(activity)
        stateProcessor.onNext(event)

        verify { event.consumed }
        verify { activity.isFinishing }
        verify { Logger.d(any(), any()) }
        verify(exactly = 1) { event.value }

        verify(exactly = 0) { event.consume(any()) }
        verify(exactly = 0) { Dialogs.showOperatorEndedEngagementDialog(any(), any(), any()) }
        verify(exactly = 0) { Dialogs.showNoMoreOperatorsAvailableDialog(any(), any(), any()) }
        verify(exactly = 0) { Dialogs.showUnexpectedErrorDialog(any(), any(), any()) }

        verify(exactly = 0) { activity.startActivity(any()) }
        verify(exactly = 0) { gliaActivityManager.finishActivities() }

        confirmVerified(event, activity)

        unmockkStatic(LOGGER_PATH)
    }

    @Test
    fun `handleState will show unstaffed dialog when event is QueueUnstaffed`() {
        val event = createMockEvent(EngagementCompletionState.QueueUnstaffed)

        val activity = mockkActivity<ChatActivity>()

        watcher.onActivityResumed(activity)
        stateProcessor.onNext(event)

        verify { event.consumed }
        verify { activity.isFinishing }
        verify(exactly = 1) { event.value }

        verify(exactly = 0) { event.consume(any()) }
        verify(exactly = 0) { Dialogs.showOperatorEndedEngagementDialog(any(), any(), any()) }
        verify(exactly = 0) { Dialogs.showUnexpectedErrorDialog(any(), any(), any()) }

        verify(exactly = 0) { activity.startActivity(any()) }
        verify(exactly = 0) { gliaActivityManager.finishActivities() }

        val dialogCallbackSlot = slot<View.OnClickListener>()

        verify { Dialogs.showNoMoreOperatorsAvailableDialog(activity, any(), capture(dialogCallbackSlot)) }

        verify(exactly = 0) { dialog.dismiss() }
        verify(exactly = 0) { gliaActivityManager.finishActivities() }
        verify(exactly = 0) { event.markConsumed() }

        dialogCallbackSlot.captured.onClick(mockk())

        verify { dialog.dismiss() }
        verify { gliaActivityManager.finishActivities() }
        verify { event.markConsumed() }

        confirmVerified(event, activity)
    }

    @Test
    fun `handleState will show unexpected error dialog when event is UnexpectedErrorHappened`() {
        val event = createMockEvent(EngagementCompletionState.UnexpectedErrorHappened)

        val activity = mockkActivity<ChatActivity>()

        watcher.onActivityResumed(activity)
        stateProcessor.onNext(event)

        verify { event.consumed }
        verify { activity.isFinishing }
        verify(exactly = 1) { event.value }

        verify(exactly = 0) { event.consume(any()) }
        verify(exactly = 0) { Dialogs.showOperatorEndedEngagementDialog(any(), any(), any()) }
        verify(exactly = 0) { Dialogs.showNoMoreOperatorsAvailableDialog(any(), any(), any()) }

        verify(exactly = 0) { activity.startActivity(any()) }
        verify(exactly = 0) { gliaActivityManager.finishActivities() }

        val dialogCallbackSlot = slot<View.OnClickListener>()

        verify { Dialogs.showUnexpectedErrorDialog(activity, any(), capture(dialogCallbackSlot)) }

        verify(exactly = 0) { dialog.dismiss() }
        verify(exactly = 0) { gliaActivityManager.finishActivities() }
        verify(exactly = 0) { event.markConsumed() }

        dialogCallbackSlot.captured.onClick(mockk())

        verify { dialog.dismiss() }
        verify { gliaActivityManager.finishActivities() }
        verify { event.markConsumed() }

        confirmVerified(event, activity)
    }

    @Test
    fun `handleState will show operator ended engagement dialog when event is ActionOnEnd is END_NOTIFICATION`() {
        val event = createMockEvent(EngagementCompletionState.EngagementEnded(false, false, Engagement.ActionOnEnd.END_NOTIFICATION))

        val activity = mockkActivity<ChatActivity>()

        watcher.onActivityResumed(activity)
        stateProcessor.onNext(event)

        verify { event.consumed }
        verify { activity.isFinishing }
        verify(exactly = 1) { event.value }

        verify(exactly = 0) { event.consume(any()) }
        verify(exactly = 0) { Dialogs.showUnexpectedErrorDialog(any(), any(), any()) }
        verify(exactly = 0) { Dialogs.showNoMoreOperatorsAvailableDialog(any(), any(), any()) }

        verify(exactly = 0) { activity.startActivity(any()) }
        verify(exactly = 0) { gliaActivityManager.finishActivities() }

        val dialogCallbackSlot = slot<View.OnClickListener>()

        verify { Dialogs.showOperatorEndedEngagementDialog(activity, any(), capture(dialogCallbackSlot)) }

        verify(exactly = 0) { dialog.dismiss() }
        verify(exactly = 0) { gliaActivityManager.finishActivities() }
        verify(exactly = 0) { event.markConsumed() }

        dialogCallbackSlot.captured.onClick(mockk())

        verify { dialog.dismiss() }
        verify { gliaActivityManager.finishActivities() }
        verify { event.markConsumed() }

        confirmVerified(event, activity)
    }

    @Test
    fun `handleState will start Survey when event is SurveyLoaded`() {
        val survey = mockk<Survey>(moreInterfaces = arrayOf(Parcelable::class))
        val event = createMockEvent(EngagementCompletionState.SurveyLoaded(survey))

        val activity = mockkActivity<ChatActivity>()

        watcher.onActivityResumed(activity)
        stateProcessor.onNext(event)

        verify { event.consumed }
        verify { activity.isFinishing }
        verify(exactly = 1) { event.value }

        verify(exactly = 0) { Dialogs.showUnexpectedErrorDialog(any(), any(), any()) }
        verify(exactly = 0) { Dialogs.showNoMoreOperatorsAvailableDialog(any(), any(), any()) }
        verify(exactly = 0) { Dialogs.showOperatorEndedEngagementDialog(any(), any(), any()) }

        verify { event.consume(any()) }

        verify { activityLauncher.launchSurvey(eq(activity), eq(survey)) }

        verify(exactly = 0) { dialog.dismiss() }
        verify(exactly = 0) { gliaActivityManager.finishActivities() }

        confirmVerified(event, activity)
    }

}

private inline fun <reified T : Activity> mockkActivity(uiTheme: UiTheme = UiTheme()): T {
    val activity: T = mockk(relaxed = true)

    every { any<Activity>().withRuntimeTheme(captureLambda()) } answers {
        secondArg<(Context, UiTheme) -> Unit>().invoke(activity, uiTheme)
    }

    return activity
}

private fun createMockEvent(state: EngagementCompletionState): OneTimeEvent<EngagementCompletionState> {
    val event: OneTimeEvent<EngagementCompletionState> = mockk(relaxed = true)
    every { event.value } returns state
    every { event.consume(captureLambda()) } answers {
        firstArg<EngagementCompletionState.() -> Unit>().invoke(state)
    }
    return event
}
