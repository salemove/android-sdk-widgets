package com.glia.widgets.view.head.controller

import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.helper.Logger

internal class ActivityWatcherForChatHeadController(
    private var serviceChatHeadController: ServiceChatHeadController,
    private var applicationChatHeadController: ApplicationChatHeadLayoutController,
    private val screenSharingController: ScreenSharingController,
    private val gliaOnEngagementUseCase: GliaOnEngagementUseCase,
) : ActivityWatcherForChatHeadContract.Controller {

    private lateinit var watcher: ActivityWatcherForChatHeadContract.Watcher

    override fun setWatcher(watcher: ActivityWatcherForChatHeadContract.Watcher) {
        this.watcher = watcher
    }

    internal var screenSharingViewCallback: ScreenSharingController.ViewCallback? = null

    override fun init() {
        gliaOnEngagementUseCase.execute { watcher.addChatHeadLayoutIfAbsent() }
    }

    override fun shouldShowBubble(gliaOrRootView: String?): Boolean {
        return shouldShowAppBubble(gliaOrRootView)
    }

    override fun onActivityResumed() {
        val gliaOrRootView = watcher.fetchGliaOrRootView()
        Logger.d(TAG, "onActivityResumed(root) ${gliaOrRootView?.javaClass?.simpleName}")
        serviceChatHeadController.onResume(gliaOrRootView)

        val viewName: String = gliaOrRootView?.javaClass?.simpleName.orEmpty()
        applicationChatHeadController.onResume(viewName)
        if (Glia.isInitialized() && shouldShowAppBubble(viewName)) {
            watcher.addChatHeadLayoutIfAbsent()
        }

        setupScreenSharingViewCallback()
        screenSharingController.setViewCallback(screenSharingViewCallback)
    }

    override fun onActivityPaused() {
        val gliaOrRootView = watcher.fetchGliaOrRootView()
        Logger.d(TAG, "onActivityPaused(root) ${gliaOrRootView?.javaClass?.simpleName}")
        serviceChatHeadController.onPause(gliaOrRootView)
        val viewName: String = gliaOrRootView?.javaClass?.simpleName.orEmpty()
        applicationChatHeadController.onPause(viewName)
        screenSharingController.removeViewCallback(screenSharingViewCallback)
    }

    private fun shouldShowAppBubble(gliaOrRootView: String?): Boolean {
        return applicationChatHeadController.shouldShow(gliaOrRootView)
    }

    private fun setupScreenSharingViewCallback() {
        screenSharingViewCallback = object : ScreenSharingController.ViewCallback {
            override fun onScreenSharingRequestError(exception: GliaException?) {
                // Is handled by com.glia.widgets.callvisualizer.ActivityWatcherController
            }

            override fun onScreenSharingRequestSuccess() {
                // no-op
            }

            override fun onScreenSharingStarted() {
                showBubble()
            }

            private fun showBubble() {
                if (Glia.isInitialized()) {
                    serviceChatHeadController.init()
                }
                val gliaOrRootView = watcher.fetchGliaOrRootView()
                val viewName: String =
                    if (gliaOrRootView != null) gliaOrRootView::class.java.simpleName else ""
                serviceChatHeadController.onResume(gliaOrRootView)
                applicationChatHeadController.onResume(viewName)
                if (applicationChatHeadController.shouldShow(viewName)) {
                    watcher.addChatHeadLayoutIfAbsent()
                    applicationChatHeadController.updateChatHeadView()
                }
            }
        }
    }

    companion object {
        private val TAG = ActivityWatcherForChatHeadController::class.java.simpleName
    }
}
