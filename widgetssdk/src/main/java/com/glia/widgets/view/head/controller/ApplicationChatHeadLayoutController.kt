package com.glia.widgets.view.head.controller

import com.glia.androidsdk.Operator
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase
import com.glia.widgets.core.chathead.domain.IsDisplayBubbleInsideAppUseCase
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase.Destinations
import com.glia.widgets.engagement.ScreenSharingState
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.imageUrl
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.head.ChatHeadLayoutContract
import com.glia.widgets.engagement.State as EngagementState

internal class ApplicationChatHeadLayoutController(
    private val isDisplayBubbleInsideAppUseCase: IsDisplayBubbleInsideAppUseCase,
    private val navigationDestinationUseCase: ResolveChatHeadNavigationUseCase,
    private val messagesNotSeenHandler: MessagesNotSeenHandler,
    private val isCallVisualizerScreenSharingUseCase: IsCallVisualizerScreenSharingUseCase,
    private val engagementStateUseCase: EngagementStateUseCase,
    private val currentOperatorUseCase: CurrentOperatorUseCase,
    private val visitorMediaUseCase: VisitorMediaUseCase,
    private val screenSharingUseCase: ScreenSharingUseCase,
    private val engagementTypeUseCase: EngagementTypeUseCase
) : ChatHeadLayoutContract.Controller {
    private var chatHeadLayout: ChatHeadLayoutContract.View? = null
    private var state = State.ENDED
    private var operatorProfileImgUrl: String? = null
    private var unreadMessagesCount = 0
    private var isOnHold = false

    /*
     * We need to keep track of the currently active (topmost) view. This can be either ChatView
     * or CallView. CallView has a translucent theme and this changes the usual lifecycle of an activity.
     * When the translucent CallActivity is on top of the ChatActivity and config change happens (e.g., screen rotation)
     * then ChatActivity onResume and onPause are called right after CallActivity's onResume. The current
     * solution ignores such onResume calls because another activity is already in resumed state.
     */
    private var resumedViewName: String? = null

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        messagesNotSeenHandler.addListener(::onUnreadMessageCountChange)
        engagementStateUseCase().unSafeSubscribe {
            when (it) {
                is EngagementState.EngagementEnded,
                is EngagementState.QueueUnstaffed,
                is EngagementState.UnexpectedErrorHappened,
                is EngagementState.QueueingCanceled -> engagementEnded()

                is EngagementState.EngagementStarted -> onNewEngagementLoaded()

                is EngagementState.Update -> onEngagementUpdated()

                is EngagementState.Queuing,
                is EngagementState.PreQueuing -> onQueuingStarted()

                else -> {
                    //no-op
                }
            }
        }
        visitorMediaUseCase.onHoldState.unSafeSubscribe(::onHoldChanged)
        currentOperatorUseCase().unSafeSubscribe(::operatorDataLoaded)
        screenSharingUseCase().filter { isCallVisualizerScreenSharingUseCase() }.unSafeSubscribe {
            when (it) {
                ScreenSharingState.Ended -> {
                    chatHeadLayout?.hide()
                    updateChatHeadView()
                }
                else -> updateChatHeadView()
            }
        }
    }

    private fun onEngagementUpdated() {
        state = State.ENGAGEMENT
        chatHeadLayout?.show()
        updateChatHeadView()
    }

    override fun onChatHeadClicked() {
        val destination = navigationDestinationUseCase.execute() ?: return
        when (destination) {
            Destinations.CALL_VIEW -> chatHeadLayout?.navigateToCall()
            Destinations.CHAT_VIEW -> chatHeadLayout?.navigateToChat()
            Destinations.SCREEN_SHARING -> chatHeadLayout?.navigateToEndScreenSharing()
        }
    }

    override fun setView(view: ChatHeadLayoutContract.View) {
        chatHeadLayout = view
        updateChatHeadView()
    }

    override fun onDestroy() {
        chatHeadLayout?.hide()
        messagesNotSeenHandler.removeListener(::onUnreadMessageCountChange)
    }

    private fun onHoldChanged(isOnHold: Boolean) {
        this.isOnHold = isOnHold
        updateChatHeadView()
    }

    override fun updateChatHeadView() {
        updateChatHeadViewState(chatHeadLayout)
        updateOnHold()
        chatHeadLayout?.showUnreadMessageCount(unreadMessagesCount)
    }

    override fun shouldShow(gliaOrRootViewName: String?): Boolean {
        return isDisplayBubbleInsideAppUseCase(gliaOrRootViewName)
    }

    override fun onResume(viewName: String) {
        setResumedViewName(viewName)
        updateChatHeadView()
    }

    override fun onPause(viewName: String) {
        clearResumedViewName(viewName)
    }

    private fun setResumedViewName(viewName: String) {
        if (resumedViewName == null) {
            resumedViewName = viewName
        }
    }

    private fun clearResumedViewName(viewName: String) {
        if (isResumedView(viewName)) {
            resumedViewName = null
        }
    }

    private fun isResumedView(viewName: String): Boolean {
        return resumedViewName != null && resumedViewName == viewName
    }

    private fun engagementEnded() {
        isOnHold = false
        state = State.ENDED
        operatorProfileImgUrl = null
        unreadMessagesCount = 0
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

    private fun updateChatHeadViewState(view: ChatHeadLayoutContract.View?) {
        when (state) {
            State.ENGAGEMENT -> decideOnEngagementBubbleDesign(view ?: return)
            State.QUEUEING -> view?.showQueueing()
            State.ENDED -> view?.showPlaceholder()
        }
    }

    private fun decideOnEngagementBubbleDesign(view: ChatHeadLayoutContract.View) {
        if (isCallVisualizerScreenSharingUseCase() && !engagementTypeUseCase.isMediaEngagement) {
            // Show screen sharing icon only if there is no 1 or 2 way video
            view.showScreenSharing()
            return
        }

        operatorProfileImgUrl?.also(view::showOperatorImage) ?: view.showPlaceholder()
    }

    private fun updateOnHold() {
        if (isOnHold && state == State.ENGAGEMENT) {
            chatHeadLayout?.showOnHold()
        } else {
            chatHeadLayout?.hideOnHold()
        }
    }

    private fun onNewEngagementLoaded() {
        isOnHold = false
        state = State.ENGAGEMENT
        chatHeadLayout?.show()
        updateChatHeadView()
    }

    private fun onQueuingStarted() {
        state = State.QUEUEING
        updateChatHeadView()
    }

    private enum class State {
        ENDED, QUEUEING, ENGAGEMENT
    }
}
