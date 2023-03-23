package com.glia.widgets.view.head.controller

import android.app.Activity
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.helper.Logger

internal class ActivityWatcherForChatHeadController(
    private var serviceChatHeadController: ServiceChatHeadController,
    private val screenSharingController: ScreenSharingController
) : ActivityWatcherForChatHeadContract.Controller {

    companion object {
        private val TAG = ActivityWatcherForChatHeadController::class.java.simpleName
    }

    private lateinit var watcher: ActivityWatcherForChatHeadContract.Watcher

    override fun setWatcher(watcher: ActivityWatcherForChatHeadContract.Watcher) {
        this.watcher = watcher
    }

    internal var screenSharingViewCallback: ScreenSharingController.ViewCallback? = null

    override fun onActivityResumed(activity: Activity) {
        Logger.d(TAG, "onActivityResumed(root)")
        serviceChatHeadController.onResume(watcher.fetchGliaOrRootView())
        setupScreenSharingViewCallback()
        screenSharingController.setViewCallback(screenSharingViewCallback)
    }

    override fun onActivityPaused() {
        Logger.d(TAG, "onActivityPaused()")
        serviceChatHeadController.onPause(watcher.fetchGliaOrRootView())
        screenSharingController.removeViewCallback(screenSharingViewCallback)
    }

    private fun setupScreenSharingViewCallback() {
        screenSharingViewCallback = object : ScreenSharingController.ViewCallback {
            override fun onScreenSharingRequestError(exception: GliaException?) {
                // Is handled by com.glia.widgets.callvisualizer.ActivityWatcherController
            }

            override fun onScreenSharingStarted() {
                if (Glia.isInitialized()) {
                    serviceChatHeadController.init()
                }
                serviceChatHeadController.onResume(watcher.fetchGliaOrRootView())
            }

        }
    }

}
