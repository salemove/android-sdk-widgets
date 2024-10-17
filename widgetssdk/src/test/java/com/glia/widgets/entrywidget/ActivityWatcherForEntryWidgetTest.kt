package com.glia.widgets.entrywidget

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Test

class ActivityWatcherForEntryWidgetTest {

    @Test
    fun `hideEntryWidget is called when onHide emits a value`() {
        val controller = EntryWidgetHideController()
        val activity = mockk<EntryWidgetActivity>(relaxed = true)
        val watcher = ActivityWatcherForEntryWidget(controller)
        mockkObject(watcher)
        every { watcher.resumedActivity } returns activity

        controller.onHide.onNext("Hide Entry Widget")

        verify { activity.finish() }
    }
}
