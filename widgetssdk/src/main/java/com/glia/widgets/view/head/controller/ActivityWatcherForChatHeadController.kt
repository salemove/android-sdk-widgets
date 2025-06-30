package com.glia.widgets.view.head.controller

import com.glia.widgets.GliaWidgets
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
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
    private val engagementStateUseCase: EngagementStateUseCase,
    private val isFromCallScreenUseCase: IsFromCallScreenUseCase,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase
) : ActivityWatcherForChatHeadContract.Controller {

    private lateinit var watcher: ActivityWatcherForChatHeadContract.Watcher

    override fun setWatcher(watcher: ActivityWatcherForChatHeadContract.Watcher) {
        this.watcher = watcher
    }

    override fun init() {
        engagementStateUseCase().unSafeSubscribe(::handleEngagementState)
    }

    private fun handleEngagementState(state: State) {
        when (state) {
            is State.EngagementStarted,
            is State.Queuing,
            is State.PreQueuing -> onEngagementOrQueueingStarted()

            is State.Update -> updateBubble()

            is State.EngagementEnded,
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
        return isFromCallScreenUseCase()
    }

    override fun resetFromCallScreen() {
        updateFromCallScreenUseCase(false)
    }

    override fun onActivityResumed() {
        val gliaOrRootView = watcher.fetchGliaOrRootView()
        Logger.d(TAG, "onActivityResumed(root) ${gliaOrRootView?.javaClass?.simpleName}")
        serviceChatHeadController.onResume(gliaOrRootView)

        val viewName: String = gliaOrRootView?.javaClass?.simpleName.orEmpty()
        applicationChatHeadController.onResume(viewName)
        if (GliaWidgets.isInitialized() && shouldShowAppBubble(viewName)) {
            watcher.addChatHeadLayoutIfAbsent()
        }
    }

    override fun onActivityPaused() {
        val gliaOrRootView = watcher.fetchGliaOrRootView()
        Logger.d(TAG, "onActivityPaused(root) ${gliaOrRootView?.javaClass?.simpleName}")
        serviceChatHeadController.onPause(gliaOrRootView)
        val viewName: String = gliaOrRootView?.javaClass?.simpleName.orEmpty()
        applicationChatHeadController.onPause(viewName)
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
}
