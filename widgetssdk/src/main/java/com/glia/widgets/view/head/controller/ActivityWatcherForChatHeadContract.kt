package com.glia.widgets.view.head.controller

import android.view.View

internal class ActivityWatcherForChatHeadContract {
    interface Controller {
        fun init()
        fun setWatcher(watcher: Watcher)
        fun onActivityPaused()
        fun onActivityResumed()
        fun shouldShowBubble(gliaOrRootView: String?): Boolean
        fun isFromCallScreen(): Boolean
        fun resetFromCallScreen()
    }

    interface Watcher {
        fun openCallActivity()
        fun fetchGliaOrRootView(): View?
        fun addChatHeadLayoutIfAbsent()
        fun removeChatHeadLayoutIfPresent()
    }
}
