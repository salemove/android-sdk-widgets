package com.glia.widgets.view.head.controller

import android.app.Activity
import android.view.View

internal class ActivityWatcherForChatHeadContract {
    interface Controller {
        fun onActivityPaused()
        fun onActivityResumed(activity: Activity)
        fun setWatcher(watcher: Watcher)
    }

    interface Watcher {
        fun openCallActivity()
        fun fetchGliaOrRootView(): View?
    }
}
