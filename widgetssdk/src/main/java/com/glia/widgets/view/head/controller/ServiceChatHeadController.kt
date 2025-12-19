package com.glia.widgets.view.head.controller

import android.annotation.SuppressLint
import android.view.View
import com.glia.androidsdk.Operator
import com.glia.widgets.callbacks.OnResult
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.EndAction
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.imageUrl
import com.glia.widgets.internal.chathead.domain.DisplayBubbleOutsideAppUseCase
import com.glia.widgets.internal.chathead.domain.ResolveChatHeadNavigationUseCase
import com.glia.widgets.internal.chathead.domain.ResolveChatHeadNavigationUseCase.Destinations
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.head.ChatHeadLogger
import com.glia.widgets.view.head.ChatHeadPosition
import com.glia.widgets.engagement.State as EngagementState

//This is in fact a singleton
@SuppressLint("CheckResult")
internal class ServiceChatHeadController(
    private val displayBubbleOutsideAppUseCase: DisplayBubbleOutsideAppUseCase,
    private val resolveChatHeadNavigationUseCase: ResolveChatHeadNavigationUseCase,
    messagesNotSeenHandler: MessagesNotSeenHandler,
    private var _chatHeadPosition: ChatHeadPosition,
    engagementStateUseCase: EngagementStateUseCase,
    currentOperatorUseCase: CurrentOperatorUseCase,
    visitorMediaUseCase: VisitorMediaUseCase
) : ChatHeadContract.Controller {
    private var chatHeadView: ChatHeadContract.View? = null
    private var state = State.ENDED
    private var operator: Operator? = null
    private var unreadMessagesCount = 0
    private var isOnHold = false
    private var engagementEndCallback: OnResult<Int> = OnResult { unreadMessageCount ->
        // Bubble should stay on the screen if there are unread messages to let the visitor know about that
        if (unreadMessageCount == 0) {
            updateStateOnEngagementEnded()
            Dependencies.secureConversations.unSubscribeFromUnreadMessageCount(engagementEndCallback)
        }
    }

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
        engagementStateUseCase().subscribe(::handleEngagementState)
        currentOperatorUseCase().subscribe(::operatorDataLoaded)
        messagesNotSeenHandler.addListener(::onUnreadMessageCountChange)
        visitorMediaUseCase.onHoldState.subscribe(::onHoldChanged)
    }

    override fun onResume(view: View?) {
        setResumedViewName(view)

        // see the comment on the resumedViewName field declaration above
        if (!isResumedView(view)) return
        displayBubbleOutsideAppUseCase(view?.javaClass?.simpleName)
    }

    override fun onPause(gliaOrRootView: View?) {
        clearResumedViewName(gliaOrRootView)
    }

    override fun onSetChatHeadView(view: ChatHeadContract.View) {
        chatHeadView = view
    }

    override fun onApplicationStop() {
        Logger.d(TAG, "onApplicationStop()")
        displayBubbleOutsideAppUseCase(null)
    }

    override fun onChatHeadPositionChanged(x: Int, y: Int) {
        _chatHeadPosition = ChatHeadPosition(x, y)
    }

    override fun onChatHeadClicked() {
        when (resolveChatHeadNavigationUseCase.execute()) {
            Destinations.CALL_VIEW -> chatHeadView?.navigateToCall()
            Destinations.CHAT_VIEW -> chatHeadView?.navigateToChat()
        }
        ChatHeadLogger.logChatHeadClicked()
    }

    private fun onHoldChanged(isOnHold: Boolean) {
        this.isOnHold = isOnHold
        updateChatHeadView()
    }


    private fun handleEngagementState(state: EngagementState) {
        when (state) {
            is EngagementState.EngagementStarted -> {
                newEngagementLoaded()
            }

            is EngagementState.Update -> toggleChatHead()

            is EngagementState.EngagementEnded -> engagementEnded(state.endAction)
            is EngagementState.QueueUnstaffed,
            is EngagementState.UnexpectedErrorHappened,
            is EngagementState.QueueingCanceled -> {
                engagementEnded(null)
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
        state = State.ENGAGEMENT
        displayBubbleOutsideAppUseCase(resumedViewName)
        updateChatHeadView()
    }

    override fun updateChatHeadView() {
        if (chatHeadView != null) {
            updateChatHeadViewState()
            updateOnHold()
            chatHeadView!!.showUnreadMessageCount(unreadMessagesCount)
        }
    }

    private fun engagementEnded(action: EndAction?) {
        when (action) {
            EndAction.Retain -> {
                Dependencies.secureConversations.subscribeToUnreadMessageCount(engagementEndCallback)
            }

            else -> updateStateOnEngagementEnded()
        }
    }

    private fun updateStateOnEngagementEnded() {
        displayBubbleOutsideAppUseCase.onDestroy()
        isOnHold = false
        state = State.ENDED
        operator = null
        unreadMessagesCount = 0
        updateChatHeadView()
        ChatHeadLogger.reset()
    }

    private fun newEngagementLoaded() {
        isOnHold = false
        state = State.ENGAGEMENT
        displayBubbleOutsideAppUseCase(resumedViewName)
        updateChatHeadView()
    }

    private fun queueingStarted() {
        state = State.QUEUEING
        displayBubbleOutsideAppUseCase(resumedViewName)
        updateChatHeadView()
    }

    private fun onUnreadMessageCountChange(count: Int) {
        unreadMessagesCount = count
        updateChatHeadView()
    }

    private fun operatorDataLoaded(operator: Operator) {
        this.operator = operator
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
        operator?.imageUrl?.also(view::showOperatorImage) ?: view.showPlaceholder()
        // Here we draw operator image if we have it, otherwise we show placeholder
        // This is the only place where we show operator image inside bubble, so this is an indicator that the operator is connected
        // This function is called multiple times during engagement, but we're filtering out duplicate logs inside ChatHeadLogger
        ChatHeadLogger.logOperatorConnected(operator ?: return)
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
