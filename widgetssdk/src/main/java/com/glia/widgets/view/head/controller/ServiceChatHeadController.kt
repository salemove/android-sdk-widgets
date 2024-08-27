package com.glia.widgets.view.head.controller

import android.view.View
import com.glia.androidsdk.Operator
import com.glia.widgets.UiTheme
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase.Destinations
import com.glia.widgets.core.chathead.domain.ToggleChatHeadServiceUseCase
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.Logger.d
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.imageUrl
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.head.ChatHeadPosition
import com.glia.widgets.engagement.State as EngagementState

internal class ServiceChatHeadController(
    private val toggleChatHeadServiceUseCase: ToggleChatHeadServiceUseCase,
    private val resolveChatHeadNavigationUseCase: ResolveChatHeadNavigationUseCase,
    messagesNotSeenHandler: MessagesNotSeenHandler,
    private var _chatHeadPosition: ChatHeadPosition,
    private val isCallVisualizerScreenSharingUseCase: IsCallVisualizerScreenSharingUseCase,
    engagementStateUseCase: EngagementStateUseCase,
    currentOperatorUseCase: CurrentOperatorUseCase,
    visitorMediaUseCase: VisitorMediaUseCase,
    screenSharingUseCase: ScreenSharingUseCase
) : ChatHeadContract.Controller {
    private var chatHeadView: ChatHeadContract.View? = null
    private var state = State.ENDED
    private var operatorProfileImgUrl: String? = null
    private var unreadMessagesCount = 0
    private var isOnHold = false
    private var sdkConfiguration: GliaSdkConfiguration? = null
    private var buildTimeTheme: UiTheme? = null

    /*
     * We need to keep track of the currently active (topmost) view. This can be either ChatView
     * or CallView. CallView has a translucent theme and this changes the usual lifecycle of an activity.
     * When the translucent CallActivity is on top of the ChatActivity and config change happens (e.g., screen rotation)
     * then ChatActivity onResume and onPause are called right after CallActivity's onResume. The Current
     * solution ignores such onResume calls because another activity is already in resumed state.
     */
    private var resumedViewName: String? = null

    override val chatHeadPosition: ChatHeadPosition get() = _chatHeadPosition

    init {
        engagementStateUseCase().unSafeSubscribe(::handleEngagementState)
        currentOperatorUseCase().unSafeSubscribe(::operatorDataLoaded)
        messagesNotSeenHandler.addListener(::onUnreadMessageCountChange)
        visitorMediaUseCase.onHoldState.unSafeSubscribe(::onHoldChanged)
        screenSharingUseCase().filter { isCallVisualizerScreenSharingUseCase() }.unSafeSubscribe {
            toggleChatHead()
        }
    }

    override fun onResume(view: View?) {
        setResumedViewName(view)

        // see the comment on the resumedViewName field declaration above
        if (!isResumedView(view)) return
        toggleChatHeadServiceUseCase(view?.javaClass?.simpleName)
    }

    override fun onPause(gliaOrRootView: View?) {
        clearResumedViewName(gliaOrRootView)
    }

    override fun onSetChatHeadView(view: ChatHeadContract.View) {
        chatHeadView = view
    }

    override fun onApplicationStop() {
        d(TAG, "onApplicationStop()")
        toggleChatHeadServiceUseCase(null)
    }

    override fun onChatHeadPositionChanged(x: Int, y: Int) {
        _chatHeadPosition = ChatHeadPosition(x, y)
    }

    override fun onChatHeadClicked() {
        when (resolveChatHeadNavigationUseCase.execute()) {
            Destinations.CALL_VIEW -> chatHeadView?.navigateToCall()
            Destinations.SCREEN_SHARING -> chatHeadView?.navigateToEndScreenSharing()
            Destinations.CHAT_VIEW -> chatHeadView?.navigateToChat()
            else -> chatHeadView?.navigateToChat()
        }
    }

    private fun onHoldChanged(isOnHold: Boolean) {
        this.isOnHold = isOnHold
        updateChatHeadView()
    }

    override fun setSdkConfiguration(configuration: GliaSdkConfiguration?) {
        sdkConfiguration = configuration
    }

    private fun handleEngagementState(state: com.glia.widgets.engagement.State) {
        when (state) {
            is EngagementState.StartedOmniCore,
            is EngagementState.StartedCallVisualizer -> {
                newEngagementLoaded()
            }

            is EngagementState.Update -> toggleChatHead()

            is EngagementState.FinishedCallVisualizer,
            is EngagementState.FinishedOmniCore,
            is EngagementState.QueueUnstaffed,
            is EngagementState.UnexpectedErrorHappened,
            is EngagementState.QueueingCanceled -> {
                engagementEnded()
            }

            is EngagementState.Queuing,
            is EngagementState.PreQueuing -> {
                queueingStarted()
            }

            else -> {
                //no op
            }
        }
    }

    private fun toggleChatHead() {
        toggleChatHeadServiceUseCase(resumedViewName)
    }

    override fun updateChatHeadView() {
        if (chatHeadView != null && buildTimeTheme != null) {
            updateChatHeadViewState()
            updateOnHold()
            chatHeadView!!.showUnreadMessageCount(unreadMessagesCount)
            chatHeadView!!.updateConfiguration(buildTimeTheme!!, sdkConfiguration)
        }
    }

    override fun setBuildTimeTheme(uiTheme: UiTheme?) {
        buildTimeTheme = uiTheme
    }

    private fun engagementEnded() {
        toggleChatHeadServiceUseCase.onDestroy()
        isOnHold = false
        state = State.ENDED
        operatorProfileImgUrl = null
        unreadMessagesCount = 0
        resumedViewName = null
        updateChatHeadView()
    }

    private fun newEngagementLoaded() {
        isOnHold = false
        state = State.ENGAGEMENT
        toggleChatHeadServiceUseCase(resumedViewName)
        if (sdkConfiguration == null) setSdkConfiguration(
            Dependencies.sdkConfigurationManager.createWidgetsConfiguration()
        )
        updateChatHeadView()
    }

    private fun queueingStarted() {
        state = State.QUEUEING
        toggleChatHeadServiceUseCase(resumedViewName)
        if (sdkConfiguration == null) setSdkConfiguration(
            Dependencies.sdkConfigurationManager.createWidgetsConfiguration()
        )
        updateChatHeadView()
    }

    private fun onUnreadMessageCountChange(count: Int) {
        unreadMessagesCount = count
        updateChatHeadView()
    }

    private fun operatorDataLoaded(operator: Operator) {
        operatorProfileImgUrl = operator.imageUrl
        updateChatHeadView()
    }

    private fun updateChatHeadViewState() {
        when (state) {
            State.ENGAGEMENT -> decideOnBubbleDesign()
            State.QUEUEING -> chatHeadView?.showQueueing()
            State.ENDED -> chatHeadView?.showPlaceholder()
        }
    }

    private fun decideOnBubbleDesign() {
        val view = chatHeadView ?: return

        if (isCallVisualizerScreenSharingUseCase()) {
            view.showScreenSharing()
            return
        }

        operatorProfileImgUrl?.also(view::showOperatorImage) ?: view.showPlaceholder()
    }

    private fun setResumedViewName(view: View?) {
        resumedViewName = view?.javaClass?.simpleName
    }

    private fun clearResumedViewName(view: View?) {
        // On quick configuration changes view can be null
        if (view != null && isResumedView(view)) {
            resumedViewName = null
        }
    }

    private fun isResumedView(view: View?): Boolean {
        return resumedViewName != null && resumedViewName == view?.javaClass?.simpleName
    }

    private fun updateOnHold() {
        if (isOnHold && state == State.ENGAGEMENT) {
            chatHeadView?.showOnHold()
        } else {
            chatHeadView?.hideOnHold()
        }
    }

    override fun onDestroy() {
        //no op
    }

    private enum class State {
        ENDED,
        QUEUEING,
        ENGAGEMENT
    }

}
