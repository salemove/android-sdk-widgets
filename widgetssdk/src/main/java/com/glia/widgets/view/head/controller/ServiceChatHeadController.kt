package com.glia.widgets.view.head.controller

import android.view.View
import androidx.core.util.Pair
import com.glia.androidsdk.Operator
import com.glia.widgets.UiTheme
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase.Destinations
import com.glia.widgets.core.chathead.domain.ToggleChatHeadServiceUseCase
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.CurrentOperatorUseCase
import com.glia.widgets.engagement.EngagementStateUseCase
import com.glia.widgets.engagement.State.FinishedCallVisualizer
import com.glia.widgets.engagement.State.FinishedOmniCore
import com.glia.widgets.engagement.State.StartedCallVisualizer
import com.glia.widgets.engagement.State.StartedOmniCore
import com.glia.widgets.engagement.VisitorMediaUseCase
import com.glia.widgets.helper.Logger.d
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.imageUrl
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.head.ChatHeadPosition

internal class ServiceChatHeadController(
    private val toggleChatHeadServiceUseCase: ToggleChatHeadServiceUseCase,
    private val resolveChatHeadNavigationUseCase: ResolveChatHeadNavigationUseCase,
    messagesNotSeenHandler: MessagesNotSeenHandler,
    private val chatHeadPosition: ChatHeadPosition,
    private val isCallVisualizerScreenSharingUseCase: IsCallVisualizerScreenSharingUseCase,
    engagementStateUseCase: EngagementStateUseCase,
    currentOperatorUseCase: CurrentOperatorUseCase,
    visitorMediaUseCase: VisitorMediaUseCase
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

    init {
        engagementStateUseCase().unSafeSubscribe(::handleEngagementState)
        currentOperatorUseCase().unSafeSubscribe(::operatorDataLoaded)
        messagesNotSeenHandler.addListener(::onUnreadMessageCountChange)
        visitorMediaUseCase.onHoldState.unSafeSubscribe(::onHoldChanged)
    }

    override fun onResume(view: View?) {
        setResumedViewName(view)

        // see the comment on the resumedViewName field declaration above
        if (!isResumedView(view)) return
        toggleChatHeadServiceUseCase.invoke(view?.javaClass?.simpleName)
    }

    fun onPause(view: View?) {
        clearResumedViewName(view)
    }

    override fun onSetChatHeadView(view: ChatHeadContract.View) {
        chatHeadView = view
    }

    override fun onApplicationStop() {
        d(TAG, "onApplicationStop()")
        toggleChatHeadServiceUseCase.invoke(null)
    }

    override fun onChatHeadPositionChanged(x: Int, y: Int) {
        chatHeadPosition[x] = y
    }

    override fun getChatHeadPosition(): Pair<Int, Int> {
        return chatHeadPosition.get()
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

    fun setSdkConfiguration(configuration: GliaSdkConfiguration?) {
        sdkConfiguration = configuration
    }

    private fun handleEngagementState(state: com.glia.widgets.engagement.State) {
        if (state is StartedOmniCore || state is StartedCallVisualizer) {
            newEngagementLoaded()
        } else if (state is FinishedCallVisualizer || state is FinishedOmniCore) {
            engagementEnded()
        }
    }

    fun updateChatHeadView() {
        if (chatHeadView != null && buildTimeTheme != null) {
            updateChatHeadViewState()
            updateOnHold()
            chatHeadView!!.showUnreadMessageCount(unreadMessagesCount)
            chatHeadView!!.updateConfiguration(buildTimeTheme, sdkConfiguration)
        }
    }

    fun setBuildTimeTheme(theme: UiTheme?) {
        buildTimeTheme = theme
    }

    private fun engagementEnded() {
        state = State.ENDED
        operatorProfileImgUrl = null
        unreadMessagesCount = 0
        resumedViewName = null
        toggleChatHeadServiceUseCase.onDestroy()
        updateChatHeadView()
    }

    private fun newEngagementLoaded() {
        state = State.ENGAGEMENT
        toggleChatHeadServiceUseCase.invoke(resumedViewName)
        if (sdkConfiguration == null) setSdkConfiguration(
            Dependencies.getSdkConfigurationManager().createWidgetsConfiguration()
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
        if (isCallVisualizerScreenSharingUseCase.invoke()) {
            chatHeadView?.showScreenSharing()
        } else if (operatorProfileImgUrl != null) {
            chatHeadView?.showOperatorImage(operatorProfileImgUrl)
        } else {
            chatHeadView?.showPlaceholder()
        }
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
