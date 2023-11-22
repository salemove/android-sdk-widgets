package com.glia.widgets.view.head.controller

import com.glia.androidsdk.Operator
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase
import com.glia.widgets.core.chathead.domain.IsDisplayApplicationChatHeadUseCase
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase.Destinations
import com.glia.widgets.engagement.CurrentOperatorUseCase
import com.glia.widgets.engagement.EngagementStateUseCase
import com.glia.widgets.engagement.VisitorMediaUseCase
import com.glia.widgets.helper.imageUrl
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.head.ChatHeadLayoutContract
import io.reactivex.disposables.CompositeDisposable

internal class ApplicationChatHeadLayoutController(
    private val isDisplayApplicationChatHeadUseCase: IsDisplayApplicationChatHeadUseCase,
    private val navigationDestinationUseCase: ResolveChatHeadNavigationUseCase,
    private val messagesNotSeenHandler: MessagesNotSeenHandler,
    private val isCallVisualizerScreenSharingUseCase: IsCallVisualizerScreenSharingUseCase,
    private val engagementStateUseCase: EngagementStateUseCase,
    private val currentOperatorUseCase: CurrentOperatorUseCase,
    private val visitorMediaUseCase: VisitorMediaUseCase
) : ChatHeadLayoutContract.Controller {
    private val engagementDisposables = CompositeDisposable()
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
        init()
    }

    override fun onDestroy() {
        chatHeadLayout?.hide()
        messagesNotSeenHandler.removeListener { count: Int -> onUnreadMessageCountChange(count) }
    }

    private fun onHoldChanged(isOnHold: Boolean) {
        this.isOnHold = isOnHold
        updateChatHeadView()
    }

    fun updateChatHeadView() {
        updateChatHeadViewState(chatHeadLayout)
        updateOnHold()
        chatHeadLayout?.showUnreadMessageCount(unreadMessagesCount)
    }

    fun shouldShow(gliaOrRootViewName: String?): Boolean {
        return isDisplayApplicationChatHeadUseCase(gliaOrRootViewName)
    }

    private fun init() {
        messagesNotSeenHandler.addListener(::onUnreadMessageCountChange)
        engagementStateUseCase()
            .unSafeSubscribe {
                when (it) {
                    com.glia.widgets.engagement.State.FinishedCallVisualizer, com.glia.widgets.engagement.State.FinishedOmniCore -> engagementEnded()
                    com.glia.widgets.engagement.State.StartedCallVisualizer, com.glia.widgets.engagement.State.StartedOmniCore -> onNewEngagementLoaded()
                    else -> {
                        //no-op
                    }
                }
            }
        visitorMediaUseCase.onHoldState.unSafeSubscribe(::onHoldChanged)
    }

    override fun onResume(viewName: String) {
        setResumedViewName(viewName)
    }

    fun onPause(viewName: String) {
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
        state = State.ENDED
        operatorProfileImgUrl = null
        unreadMessagesCount = 0
        engagementDisposables.clear()
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
            State.ENGAGEMENT -> decideOnEngagementBubbleDesign(view)
            State.QUEUEING -> view?.showQueueing()
            State.ENDED -> view?.showPlaceholder()
        }
    }

    private fun decideOnEngagementBubbleDesign(view: ChatHeadLayoutContract.View?) {
        if (isCallVisualizerScreenSharingUseCase()) {
            view?.showScreenSharing()
        } else if (operatorProfileImgUrl != null) {
            view?.showOperatorImage(operatorProfileImgUrl)
        } else {
            view?.showPlaceholder()
        }
    }

    private fun updateOnHold() {
        if (isOnHold && state == State.ENGAGEMENT) {
            chatHeadLayout?.showOnHold()
        } else {
            chatHeadLayout?.hideOnHold()
        }
    }

    private fun onNewEngagementLoaded() {
        state = State.ENGAGEMENT
        engagementDisposables.add(currentOperatorUseCase().subscribe(::operatorDataLoaded))
        updateChatHeadView()
    }

    private enum class State {
        ENDED, QUEUEING, ENGAGEMENT
    }
}
