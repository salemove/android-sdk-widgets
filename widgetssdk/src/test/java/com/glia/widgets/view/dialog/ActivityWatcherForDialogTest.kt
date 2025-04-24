package com.glia.widgets.view.dialog

import android.CONTEXT_EXTENSIONS_CLASS_PATH
import android.app.Activity
import android.content.Context
import android.mock
import android.mockkOneTimeEvent
import android.unMock
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.withRuntimeTheme
import com.glia.widgets.view.Dialogs
import io.mockk.every
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

internal class ActivityWatcherForDialogTest {
    lateinit var activityManager: GliaActivityManager
    lateinit var controller: GlobalDialogController
    lateinit var watcher: ActivityWatcherForDialog
    lateinit var stateFlowable: PublishProcessor<OneTimeEvent<GlobalDialogController.State>>

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        Logger.mock()
        mockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
        mockkObject(Dialogs)

        stateFlowable = PublishProcessor.create()

        activityManager = mockk(relaxUnitFun = true)
        controller = mockk(relaxUnitFun = true) {
            every { state } returns stateFlowable
        }

        watcher = ActivityWatcherForDialog(activityManager, controller)
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()

        Logger.unMock()
        unmockkObject(Dialogs)
        unmockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
    }

    private val skippingMatcher: (String) -> Boolean = { it.contains("skipping..") }

    @Test
    fun `handleState will skip when event is already consumed`() {
        val activity = mockkActivity()
        val event = mockkOneTimeEvent(GlobalDialogController.State.DismissDialog, isConsumed = true)
        stateFlowable.onNext(event)
        watcher.onActivityResumed(activity)

        verify { event.consumed }
        verify { Logger.d(any(), match(skippingMatcher)) }
    }

    @Test
    fun `handleState will skip when activity is finishing`() {
        val activity = mockkActivity(isFinishing = true)
        watcher.onActivityResumed(activity)
        val event = mockkOneTimeEvent(GlobalDialogController.State.NotificationPermissionDialog({ }, {}))
        stateFlowable.onNext(event)

        verify { event.consumed }
        verify { activity.isFinishing }
        verify { Logger.d(any(), match(skippingMatcher)) }
    }

    @Test
    fun `handleState will skip when activity is null`() {
        val activity = mockkActivity()
        val resumedActivity = watcher.resumedActivity.test()
        watcher.onActivityResumed(activity)
        resumedActivity.values().forEach { it.clear() }

        val event = mockkOneTimeEvent(GlobalDialogController.State.NotificationPermissionDialog({ }, {}))
        stateFlowable.onNext(event)

        verify { event.consumed }
        verify { Logger.d(any(), match(skippingMatcher)) }
    }

    @Test
    fun `handleState will show permissions dialog when state is NotificationPermissionDialog`() {
        val activity = mockkActivity()

        val dialog: AlertDialog = mockk(relaxed = true)
        val positiveButtonSlot = slot<View.OnClickListener>()
        val negativeButtonSlot = slot<View.OnClickListener>()
        every { Dialogs.showPushNotificationsPermissionDialog(any(), any(), any(), any()) } returns dialog

        watcher.onActivityResumed(activity)
        val onAllow = mockk<() -> Unit>(relaxed = true)
        val onCancel = mockk<() -> Unit>(relaxed = true)
        val event = mockkOneTimeEvent(GlobalDialogController.State.NotificationPermissionDialog(onAllow, onCancel))
        stateFlowable.onNext(event)

        verify(exactly = 0) { Logger.d(any(), match(skippingMatcher)) }
        verify { Dialogs.showPushNotificationsPermissionDialog(any(), any(), capture(positiveButtonSlot), capture(negativeButtonSlot)) }

        positiveButtonSlot.captured.onClick(mockk(relaxed = true))
        verify { event.markConsumed() }
        verify { dialog.dismiss() }
        verify { onAllow() }

        negativeButtonSlot.captured.onClick(mockk(relaxed = true))
        verify { event.markConsumed() }
        verify { onCancel() }
    }

    @Test
    fun `handleState will Dismiss the dialog when state is dismissDialog`() {
        val activity = mockkActivity()

        val dialog: AlertDialog = mockk(relaxed = true)
        every { Dialogs.showPushNotificationsPermissionDialog(any(), any(), any(), any()) } returns dialog

        watcher.onActivityResumed(activity)
        val onAllow = mockk<() -> Unit>(relaxed = true)
        val onCancel = mockk<() -> Unit>(relaxed = true)
        val event = mockkOneTimeEvent(GlobalDialogController.State.NotificationPermissionDialog(onAllow, onCancel))
        stateFlowable.onNext(event)

        verify(exactly = 0) { Logger.d(any(), match(skippingMatcher)) }
        verify { Dialogs.showPushNotificationsPermissionDialog(any(), any(), any(), any()) }

        val dismissEvent = mockkOneTimeEvent(GlobalDialogController.State.DismissDialog)
        every { dismissEvent.consume(captureLambda()) } answers {
            firstArg<GlobalDialogController.State.DismissDialog.() -> Unit>().invoke(GlobalDialogController.State.DismissDialog)
        }
        stateFlowable.onNext(dismissEvent)
        verify { dialog.dismiss() }
        verify(exactly = 0) { onAllow() }
        verify(exactly = 0) { onCancel() }
    }

    private fun mockkActivity(isFinishing: Boolean = false) = mockk<CallActivity>(relaxed = true) {
        every { this@mockk.isFinishing } returns isFinishing
        every { any<Activity>().withRuntimeTheme(captureLambda()) } answers {
            secondArg<(Context, UiTheme) -> Unit>().invoke(this@mockk, UiTheme())
        }
    }

}
