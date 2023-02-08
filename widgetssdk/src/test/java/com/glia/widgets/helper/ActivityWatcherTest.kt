package com.glia.widgets.helper

import android.app.Activity
import android.app.Application
import com.glia.widgets.callvisualizer.controller.CallVisualizerMediaUpgradeController
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock

internal class ActivityWatcherTest {

    @Test
    fun resumedActivity_cleared_whenActivityPaused() {
        val app = mock(Application::class.java)
        val controller = mock(CallVisualizerMediaUpgradeController::class.java)
        val activityWatcher = ActivityWatcher(app, controller)

        activityWatcher.onActivityResumed(mock(Activity::class.java))
        activityWatcher.onActivityPaused(mock(Activity::class.java))

        assertNull(activityWatcher.resumedActivity)
    }

    @Test
    fun resumedActivity_saved_whenActivityResumed() {
        val app = mock(Application::class.java)
        val controller = mock(CallVisualizerMediaUpgradeController::class.java)
        val activityWatcher = ActivityWatcher(app, controller)

        activityWatcher.onActivityResumed(mock(Activity::class.java))

        assertNotNull(activityWatcher.resumedActivity)
    }
}
