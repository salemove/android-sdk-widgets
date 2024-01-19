package com.glia.widgets.view.head.controller

import com.glia.androidsdk.Glia
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.core.screensharing.ScreenSharingContract
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.head.ChatHeadLayoutContract

internal class ActivityWatcherForChatHeadController(
    private var serviceChatHeadController: ChatHeadContract.Controller,
    private var applicationChatHeadController: ChatHeadLayoutContract.Controller,
    private val screenSharingController: ScreenSharingContract.Controller,
    private val engagementStateUseCase: EngagementStateUseCase,
    private val isFromCallScreenUseCase: IsFromCallScreenUseCase,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase
) : ActivityWatcherForChatHeadContract.Controller {

    private lateinit var watcher: ActivityWatcherForChatHeadContract.Watcher

    override fun setWatcher(watcher: ActivityWatcherForChatHeadContract.Watcher) {
        this.watcher = watcher
    }

    internal var screenSharingViewCallback: ScreenSharingContract.ViewCallback? = null

    override fun init() {
        engagementStateUseCase().unSafeSubscribe(::handleEngagementState)
    }

    private fun handleEngagementState(state: State) {
        when (state) {
            is State.StartedOmniCore,
            is State.StartedCallVisualizer,
            is State.Queuing,
            is State.PreQueuing -> onEngagementOrQueueingStarted()

            is State.Update -> updateBubble()

            is State.FinishedCallVisualizer,
            is State.FinishedOmniCore,
            is State.QueueUnstaffed,
            is State.UnexpectedErrorHappened,
            is State.QueueingCanceled -> onEngagementOrQueueingEnded()

            else -> {
                //no op
            }
        }
    }

    private fun onEngagementOrQueueingEnded() {
        watcher.removeChatHeadLayoutIfPresent()
    }

    private fun onEngagementOrQueueingStarted() {
        updateBubble()
    }

    private fun updateBubble() {
        if (shouldShowBubble(watcher.fetchGliaOrRootView()?.javaClass?.simpleName.orEmpty())) {
            showBubble()
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
        if (isCurrentEngagementCallVisualizerUseCase()) {
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
        val gliaOrRootView = watcher.fetchGliaOrRootView()
        val viewName: String = if (gliaOrRootView != null) gliaOrRootView::class.java.simpleName else ""
        serviceChatHeadController.onResume(gliaOrRootView)
        applicationChatHeadController.onResume(viewName)
        if (applicationChatHeadController.shouldShow(viewName)) {
            watcher.addChatHeadLayoutIfAbsent()
            applicationChatHeadController.updateChatHeadView()
        }
    }

    private fun setupScreenSharingViewCallback() {
        screenSharingViewCallback = object : ScreenSharingContract.ViewCallback {
            override fun onScreenSharingRequestSuccess() {
                showBubble()
            }
        }
    }
}
