package com.glia.widgets.callvisualizer

import android.CONTEXT_EXTENSIONS_CLASS_PATH
import android.LOGGER_PATH
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.glia.widgets.UiTheme
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.withRuntimeTheme
import com.glia.widgets.internal.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.unifiedui.theme.UnifiedThemeManager
import com.glia.widgets.webbrowser.WebBrowserActivity
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

class CallVisualizerActivityWatcherTest {
    private val controllerState: PublishProcessor<OneTimeEvent<CallVisualizerContract.State>> = PublishProcessor.create()

    private lateinit var controller: CallVisualizerContract.Controller
    private lateinit var gliaActivityManager: GliaActivityManager
    private lateinit var localeProvider: LocaleProvider
    private lateinit var themeManager: UnifiedThemeManager

    private lateinit var watcher: CallVisualizerActivityWatcher
    private lateinit var mockLocale: LocaleString
    private lateinit var activityLauncher: ActivityLauncher

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        Logger.setIsDebug(false)
        mockkStatic(LOGGER_PATH, CONTEXT_EXTENSIONS_CLASS_PATH)
        every { Logger.d(any(), any()) } just Runs

        controller = mockk(relaxUnitFun = true) {
            every { state } returns controllerState
        }
        gliaActivityManager = mockk(relaxed = true)
        localeProvider = mockk(relaxed = true)
        themeManager = mockk(relaxed = true)
        mockLocale = mockk(relaxed = true)
        activityLauncher = mockk(relaxed = true)

        watcher = CallVisualizerActivityWatcher(controller, gliaActivityManager, localeProvider, themeManager, activityLauncher)
        verify { controller.state }
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()

        confirmVerified(controller)
        unmockkStatic(LOGGER_PATH, CONTEXT_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `resuming WebBrowserActivity will notify web browser opened when current state is OpenWebBrowserScreen`() {
        emitActivity<WebBrowserActivity>()
        val event = createMockEvent(CallVisualizerContract.State.OpenWebBrowserScreen(mockLocale, "url"))
        emitState(event)

        verify { event.consume(any()) }
        verify { controller.onWebBrowserOpened() }
    }

    @Test
    fun `event will be skipped when it is consumed`() {
        emitActivity<ChatActivity>()
        val event = createMockEvent(CallVisualizerContract.State.DismissDialog, isConsumed = true)
        emitState(event)

        verify { event.consumed }
        verify { Logger.d(any(), any()) }
    }

    @Test
    fun `event will be skipped when activity is null or finishing`() {
        emitActivity<ChatActivity>(finishing = true)
        val event = createMockEvent(CallVisualizerContract.State.ShowTimeoutSnackBar)
        emitState(event)

        verify { event.consumed }
        verify { Logger.d(any(), any()) }
    }

    @Test
    fun `event will be skipped when activity is WebBrowser activity and state is DisplayConfirmationDialog`() {
        emitActivity<WebBrowserActivity>()
        val event = createMockEvent(CallVisualizerContract.State.DisplayConfirmationDialog(mockk()))
        emitState(event)

        verify { event.consumed }
        verify { Logger.d(any(), any()) }
    }

    @Test
    fun `visitor code dialog will be shown when state is a DisplayVisitorCodeDialog`() {
        mockkObject(Dialogs)
        val dialog: AlertDialog = mockk(relaxed = true)
        every { Dialogs.showVisitorCodeDialog(any()) } returns dialog

        val activity = emitActivity<DialogHolderActivity>()
        val event = createMockEvent(CallVisualizerContract.State.DisplayVisitorCodeDialog)
        emitState(event)

        verify(exactly = 0) { event.markConsumed() }
        verify(exactly = 0) { event.consume(any()) }
        verify { event.consumed }
        verify { event.value }
        verify { activity.withRuntimeTheme(any()) }
        verify { activity.isFinishing }

        verify { Dialogs.showVisitorCodeDialog(any()) }

        val dismissDialogEvent = createMockEvent(CallVisualizerContract.State.DismissDialog)
        emitState(dismissDialogEvent)

        verify { activity.isFinishing }
        verify { dismissDialogEvent.consumed }
        verify { dismissDialogEvent.value }
        verify { dismissDialogEvent.consume(any()) }
        verify { dialog.dismiss() }
        verify(exactly = 0) { activity.finish() }

        confirmVerified(dialog, activity, event, dismissDialogEvent)

        unmockkObject(Dialogs)
    }

    @Test
    fun `confirmation dialog will be shown when activity is not a WebBrowserActivity and state is a DisplayConfirmationDialog`() {
        mockkObject(Dialogs)
        val dialog: AlertDialog = mockk(relaxed = true)
        every { Dialogs.showEngagementConfirmationDialog(any(), any(), any(), any(), any(), any()) } returns dialog

        val activity = emitActivity<ChatActivity>()
        val event = createMockEvent(CallVisualizerContract.State.DisplayConfirmationDialog(mockk()))
        emitState(event)

        verify(exactly = 0) { event.markConsumed() }
        verify { activity.withRuntimeTheme(any()) }

        verify { Dialogs.showEngagementConfirmationDialog(any(), any(), any(), any(), any(), any()) }

        unmockkObject(Dialogs)
    }

    @Test
    fun `controller should be notified when confirmation dialog link clicked`() {
        mockkObject(Dialogs)
        val dialog: AlertDialog = mockk(relaxed = true)
        val onLinkClickedSlot = slot<(Link) -> Unit>()
        every { Dialogs.showEngagementConfirmationDialog(any(), any(), any(), any(), any(), any()) } returns dialog

        val links = ConfirmationDialogLinks(
            link1 = Link(mockLocale, mockLocale),
            link2 = Link(mockLocale, mockLocale)
        )

        val activity = emitActivity<ChatActivity>()
        val event = createMockEvent(CallVisualizerContract.State.DisplayConfirmationDialog(links))
        emitState(event)

        verify { activity.isFinishing }
        verify { activity.withRuntimeTheme(any()) }
        verify(exactly = 0) { event.markConsumed() }
        verify { event.consumed }
        verify { event.value }

        verify {
            Dialogs.showEngagementConfirmationDialog(
                any(),
                any(),
                eq(links),
                capture(onLinkClickedSlot),
                any(),
                any()
            )
        }

        onLinkClickedSlot.captured.invoke(links.link1)

        verify { event.markConsumed() }
        verify { dialog.dismiss() }
        verify { controller.onLinkClicked(eq(links.link1)) }

        confirmVerified(dialog, activity, event)

        unmockkObject(Dialogs)
    }

    @Test
    fun `controller should be notified when confirmation dialog accepted`() {
        mockkObject(Dialogs)
        val dialog: AlertDialog = mockk(relaxed = true)
        val onAcceptSlot = slot<View.OnClickListener>()
        every { Dialogs.showEngagementConfirmationDialog(any(), any(), any(), any(), any(), any()) } returns dialog

        val links = ConfirmationDialogLinks(
            link1 = Link(mockLocale, mockLocale),
            link2 = Link(mockLocale, mockLocale)
        )

        val activity = emitActivity<ChatActivity>()
        val event = createMockEvent(CallVisualizerContract.State.DisplayConfirmationDialog(links))
        emitState(event)

        verify { activity.isFinishing }
        verify { activity.withRuntimeTheme(any()) }
        verify(exactly = 0) { event.markConsumed() }
        verify { event.consumed }
        verify { event.value }

        verify {
            Dialogs.showEngagementConfirmationDialog(
                any(),
                any(),
                eq(links),
                any(),
                capture(onAcceptSlot),
                any()
            )
        }

        onAcceptSlot.captured.onClick(mockk())

        verify { event.markConsumed() }
        verify { dialog.dismiss() }
        verify { controller.onEngagementConfirmationDialogAllowed() }

        confirmVerified(dialog, activity, event)

        unmockkObject(Dialogs)
    }

    @Test
    fun `controller should be notified when confirmation dialog declined`() {
        mockkObject(Dialogs)
        val dialog: AlertDialog = mockk(relaxed = true)
        val onDeclineSlot = slot<View.OnClickListener>()
        every { Dialogs.showEngagementConfirmationDialog(any(), any(), any(), any(), any(), any()) } returns dialog

        val links = ConfirmationDialogLinks(
            link1 = Link(mockLocale, mockLocale),
            link2 = Link(mockLocale, mockLocale)
        )

        val activity = emitActivity<ChatActivity>()
        val event = createMockEvent(CallVisualizerContract.State.DisplayConfirmationDialog(links))
        emitState(event)

        verify { activity.isFinishing }
        verify { activity.withRuntimeTheme(any()) }
        verify(exactly = 0) { event.markConsumed() }
        verify { event.consumed }
        verify { event.value }

        verify {
            Dialogs.showEngagementConfirmationDialog(
                any(),
                any(),
                eq(links),
                any(),
                any(),
                capture(onDeclineSlot)
            )
        }

        onDeclineSlot.captured.onClick(mockk())

        verify { event.markConsumed() }
        verify { dialog.dismiss() }
        verify { controller.onEngagementConfirmationDialogDeclined() }

        confirmVerified(dialog, activity, event)

        unmockkObject(Dialogs)
    }

    @Test
    fun `holder activity should be closed when current activity is DialogHolderActivity and state is CloseHolderActivity`() {
        val activity = emitActivity<DialogHolderActivity>()
        val event = createMockEvent(CallVisualizerContract.State.CloseHolderActivity)
        emitState(event)

        verify { event.consumed }
        verify { event.value }
        verify { event.consume(any()) }
        verify { gliaActivityManager.finishActivity(DialogHolderActivity::class) }

        confirmVerified(activity, event)
    }

    @Test
    fun `WebBrowserActivity should be shown when current state is OpenWebBrowserScreen`() {
        val title = mockLocale
        val url = "url"

        val activity = emitActivity<DialogHolderActivity>()
        val event = createMockEvent(CallVisualizerContract.State.OpenWebBrowserScreen(title, url))
        emitState(event)

        verify { activity.isFinishing }
        verify { event.consumed }
        verify { event.value }
        verify(exactly = 0) { event.consume(any()) }
        verify(exactly = 0) { event.markConsumed() }
        verify { activityLauncher.launchWebBrowser(activity, title, url) }

        confirmVerified(activity, event)
    }

    private fun createMockEvent(state: CallVisualizerContract.State, isConsumed: Boolean = false): OneTimeEvent<CallVisualizerContract.State> {
        val event: OneTimeEvent<CallVisualizerContract.State> = mockk(relaxed = true) {
            every { value } returns state
            every { consumed } returns isConsumed
            every { consume(captureLambda()) } answers {
                firstArg<CallVisualizerContract.State.() -> Unit>().invoke(state)
            }
        }

        return event
    }

    private fun emitState(event: OneTimeEvent<CallVisualizerContract.State>) {
        controllerState.onNext(event)
    }

    private inline fun <reified T : Activity> emitActivity(finishing: Boolean = false): T {
        val activity = mockk<T>(relaxed = true) {
            every { isFinishing } returns finishing
        }

        every { any<Activity>().withRuntimeTheme(captureLambda()) } answers {
            secondArg<(Context, UiTheme) -> Unit>().invoke(activity, UiTheme())
        }

        watcher.onActivityResumed(activity)

        return activity
    }
}
