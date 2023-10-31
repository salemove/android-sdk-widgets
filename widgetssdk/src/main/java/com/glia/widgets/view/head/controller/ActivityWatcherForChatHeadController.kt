package com.glia.widgets.view.head.controller

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Glia
import com.glia.androidsdk.engagement.EngagementState
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerUseCase
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal class ActivityWatcherForChatHeadController(
    private var serviceChatHeadController: ServiceChatHeadController,
    private var applicationChatHeadController: ApplicationChatHeadLayoutController,
    private val screenSharingController: ScreenSharingController,
    private val gliaOnEngagementUseCase: GliaOnEngagementUseCase,
    private val isFromCallScreenUseCase: IsFromCallScreenUseCase,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val isCallVisualizerUseCase: IsCallVisualizerUseCase
) : ActivityWatcherForChatHeadContract.Controller {

    private lateinit var watcher: ActivityWatcherForChatHeadContract.Watcher

    override fun setWatcher(watcher: ActivityWatcherForChatHeadContract.Watcher) {
        this.watcher = watcher
    }

    internal var screenSharingViewCallback: ScreenSharingController.ViewCallback? = null

    override fun init() {
        gliaOnEngagementUseCase.execute { engagement ->
            showBubble()

            engagement.on(
                Engagement.Events.STATE_UPDATE
            ) { engagementState: EngagementState? ->
                showBubble()
            }
        }
    }

    override fun shouldShowBubble(gliaOrRootView: String?): Boolean {
        return shouldShowAppBubble(gliaOrRootView)
    }

    override fun isFromCallScreen(): Boolean {
        return isFromCallScreenUseCase.isFromCallScreen
    }

    override fun resetFromCallScreen() {
        updateFromCallScreenUseCase.updateFromCallScreen(false)
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
        if (isCallVisualizerUseCase()) {
            screenSharingController.setViewCallback(screenSharingViewCallback)
        } else {
            // Show screen sharing requests for any screen (not only Chat and Call) for Call Visualizer only.
            // For Omnicore engagement show only on Chat and Call screen.
            screenSharingController.removeViewCallback(screenSharingViewCallback)
        }
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

    private fun setupScreenSharingViewCallback() {
        screenSharingViewCallback = object : ScreenSharingController.ViewCallback {
            override fun onScreenSharingRequestSuccess() {
                showBubble()
            }
        }
    }
}
