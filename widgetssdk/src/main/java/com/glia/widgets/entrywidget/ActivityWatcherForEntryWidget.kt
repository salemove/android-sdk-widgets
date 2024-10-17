package com.glia.widgets.entrywidget

import android.annotation.SuppressLint
import android.app.Activity
import androidx.annotation.VisibleForTesting
import com.glia.widgets.base.BaseActivityStackWatcher
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.WeakReferenceDelegate

@SuppressLint("CheckResult")
internal class ActivityWatcherForEntryWidget(
    entryWidgetHideController: EntryWidgetHideController
) : BaseActivityStackWatcher() {

    @VisibleForTesting
    var resumedActivity: Activity? by WeakReferenceDelegate()

    init {
        topActivityObserver.subscribe(
            { resumedActivity = it },
            { error -> Logger.d(TAG, "Top activity observer error $error") }
        )

        entryWidgetHideController.onHide.subscribe { _ -> hideEntryWidget() }
    }

    private fun hideEntryWidget() {
        resumedActivity.let {
            if (it is EntryWidgetActivity) {
                it.finish()
            }
        }
    }
}
