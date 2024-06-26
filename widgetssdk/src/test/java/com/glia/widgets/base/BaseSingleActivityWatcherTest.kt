package com.glia.widgets.base

import android.CONTEXT_EXTENSIONS_CLASS_PATH
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.targetActivityName
import androidx.appcompat.app.AlertDialog
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.parentActivity
import com.glia.widgets.helper.withRuntimeTheme
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class BaseSingleActivityWatcherTest {
    private lateinit var gliaActivityManager: GliaActivityManager
    private lateinit var watcher: BaseSingleActivityWatcher

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        gliaActivityManager = mockk(relaxUnitFun = true)
        watcher = BaseSingleActivityWatcher(gliaActivityManager)
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
        confirmVerified(gliaActivityManager)
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
        verify { activity.equals(any()) }

        confirmVerified(activity)
    }

    @Test
    fun `finishActivities finishes activities`() {
        watcher.finishActivities()
        verify { gliaActivityManager.finishActivities() }
    }

    @Test
    fun `finishActivity finishes activity`() {
        watcher.finishActivity(ChatActivity::class)
        verify { gliaActivityManager.finishActivity(ChatActivity::class) }
    }

    @Test
    fun `onActivityResumed will emit activity`() {
        val activity: CallActivity = mockk()
        val resumedActivity = watcher.resumedActivity.test()

        watcher.onActivityResumed(activity)
        watcher.onActivityPaused(activity)
        val values = resumedActivity.assertNoErrors().assertNotComplete().values()

        assertEquals(activity, values[0].get())
        assertNull(values[1].get())
    }

    @Test
    fun `showAlertDialogWithStyledContext will launch DialogHolderActivity when activity is not glia activity`() {
        val dialog: AlertDialog = mockk(relaxed = true)
        val activity: Activity = mockk(relaxed = true)
        val callback: (Context, UiTheme) -> AlertDialog = mockk()
        every { callback.invoke(any(), any()) } returns dialog

        val intentSlot = slot<Intent>()
        watcher.showAlertDialogWithStyledContext(activity, callback)
        verify { activity.startActivity(capture(intentSlot)) }
        verify(exactly = 0) { callback.invoke(any(), any()) }

        assertEquals(DialogHolderActivity::class.qualifiedName, intentSlot.captured.targetActivityName)
    }

    @Test
    fun `showAlertDialogWithStyledContext will invoke callback when activity is glia activity`() {
        mockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
        val dialog: AlertDialog = mockk(relaxed = true)
        val activity: ChatActivity = mockk(relaxed = true)
        every { any<Activity>().withRuntimeTheme(captureLambda()) } answers {
            secondArg<(Context, UiTheme) -> Unit>().invoke(activity, UiTheme())
        }

        val callback: (Context, UiTheme) -> AlertDialog = mockk()
        every { callback.invoke(any(), any()) } returns dialog

        watcher.showAlertDialogWithStyledContext(activity, callback)
        verify(exactly = 0) { activity.startActivity(any()) }
        verify { callback.invoke(any(), any()) }

        watcher.dismissAlertDialogSilently()
        verify { dialog.dismiss() }
        unmockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `showAlertDialogWithStyledContext will dismiss dialog when it exists`() {
        mockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
        val dialog: AlertDialog = mockk(relaxed = true)
        val activity: ChatActivity = mockk(relaxed = true)
        every { any<Activity>().withRuntimeTheme(captureLambda()) } answers {
            secondArg<(Context, UiTheme) -> Unit>().invoke(activity, UiTheme())
        }

        val callback: (Context, UiTheme) -> AlertDialog = mockk()
        every { callback.invoke(any(), any()) } returns dialog

        watcher.showAlertDialogWithStyledContext(activity, callback)
        verify(exactly = 0) { activity.startActivity(any()) }
        verify { callback.invoke(any(), any()) }

        watcher.showAlertDialogWithStyledContext(activity, callback)
        verify { dialog.dismiss() }
        unmockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `onActivityDestroyed will dismiss dialog when activity is dialogs parent activity`() {
        mockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
        val dialog: AlertDialog = mockk(relaxed = true)
        val activity: ChatActivity = mockk(relaxed = true)
        every { any<Activity>().withRuntimeTheme(captureLambda()) } answers {
            secondArg<(Context, UiTheme) -> Unit>().invoke(activity, UiTheme())
        }
        every { any<AlertDialog>().parentActivity } returns activity

        val callback: (Context, UiTheme) -> AlertDialog = mockk()
        every { callback.invoke(any(), any()) } returns dialog

        watcher.showAlertDialogWithStyledContext(activity, callback)
        verify(exactly = 0) { activity.startActivity(any()) }
        verify { callback.invoke(any(), any()) }

        watcher.onActivityDestroyed(activity)
        verify { gliaActivityManager.onActivityDestroyed(activity) }
        verify { dialog.dismiss() }
        unmockkStatic(CONTEXT_EXTENSIONS_CLASS_PATH)
    }

}
