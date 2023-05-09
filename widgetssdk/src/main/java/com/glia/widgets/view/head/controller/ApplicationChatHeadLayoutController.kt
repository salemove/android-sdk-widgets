package com.glia.widgets.view.head.controller

import com.glia.androidsdk.Operator
import com.glia.androidsdk.comms.VisitorMediaState
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.androidsdk.omnicore.OmnicoreEngagement
import com.glia.widgets.core.callvisualizer.domain.GliaOnCallVisualizerEndUseCase
import com.glia.widgets.core.callvisualizer.domain.GliaOnCallVisualizerUseCase
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase
import com.glia.widgets.core.chathead.domain.IsDisplayApplicationChatHeadUseCase
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase.Destinations
import com.glia.widgets.core.chathead.domain.SetPendingSurveyUseCase
import com.glia.widgets.core.engagement.domain.GetOperatorFlowableUseCase
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase
import com.glia.widgets.core.visitor.VisitorMediaUpdatesListener
import com.glia.widgets.core.visitor.domain.AddVisitorMediaStateListenerUseCase
import com.glia.widgets.core.visitor.domain.RemoveVisitorMediaStateListenerUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.imageUrl
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.head.ChatHeadLayoutContract
import io.reactivex.disposables.CompositeDisposable

internal class ApplicationChatHeadLayoutController(
    private val isDisplayApplicationChatHeadUseCase: IsDisplayApplicationChatHeadUseCase,
    private val navigationDestinationUseCase: ResolveChatHeadNavigationUseCase,
    private val gliaOnEngagementUseCase: GliaOnEngagementUseCase,
    private val onEngagementEndUseCase: GliaOnEngagementEndUseCase,
    private val gliaOnCallVisualizerUseCase: GliaOnCallVisualizerUseCase,
    private val onCallVisualizerEndUseCase: GliaOnCallVisualizerEndUseCase,
    private val messagesNotSeenHandler: MessagesNotSeenHandler,
    private val addVisitorMediaStateListenerUseCase: AddVisitorMediaStateListenerUseCase,
    private val removeVisitorMediaStateListenerUseCase: RemoveVisitorMediaStateListenerUseCase,
    private val getOperatorFlowableUseCase: GetOperatorFlowableUseCase,
    private val setPendingSurveyUseCase: SetPendingSurveyUseCase,
    private val isCallVisualizerScreenSharingUseCase: IsCallVisualizerScreenSharingUseCase
) : ChatHeadLayoutContract.Controller,
    VisitorMediaUpdatesListener,
    GliaOnEngagementUseCase.Listener,
    GliaOnEngagementEndUseCase.Listener,
    GliaOnCallVisualizerUseCase.Listener,
    GliaOnCallVisualizerEndUseCase.Listener {
    private val engagementDisposables = CompositeDisposable()
    private var chatHeadLayout: ChatHeadLayoutContract.View? = null
    private var state = State.ENDED
    private var operatorProfileImgUrl: String? = null
    private var unreadMessagesCount = 0
    private var isOnHold = false

    /*
     * We need to keep track of the currently active (topmost) view. This can be either ChatView
     * or CallView. CallView has translucent theme and this changes the usual lifecycle of an activity.
     * When the translucent CallActivity is on top of the ChatActivity and config change happens (e.g screen rotation)
     * then ChatActivity onResume and onPause are called right after CallActivity's onResume. Current
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
        if (isDisplayApplicationChatHeadUseCase(resumedViewName)) return

        chatHeadLayout?.hide()
        gliaOnEngagementUseCase.unregisterListener(this)
        gliaOnCallVisualizerUseCase.unregisterListener(this)
        messagesNotSeenHandler.removeListener { count: Int -> onUnreadMessageCountChange(count) }
        onEngagementEndUseCase.unregisterListener(this)
        onCallVisualizerEndUseCase.unregisterListener(this)
        removeVisitorMediaStateListenerUseCase.execute(this)
    }

    override fun onNewVisitorMediaState(visitorMediaState: VisitorMediaState?) {
        // No-op
    }

    override fun onHoldChanged(isOnHold: Boolean) {
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
        gliaOnEngagementUseCase.execute(this)
        gliaOnCallVisualizerUseCase(this)
        messagesNotSeenHandler.addListener { count: Int -> onUnreadMessageCountChange(count) }
        onEngagementEndUseCase.execute(this)
        onCallVisualizerEndUseCase.execute(this)
        addVisitorMediaStateListenerUseCase.execute(this)
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

    override fun engagementEnded() {
        setPendingSurveyUseCase.invoke()
        state = State.ENDED
        operatorProfileImgUrl = null
        unreadMessagesCount = 0
        engagementDisposables.dispose()
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
        engagementDisposables.add(
            getOperatorFlowableUseCase.execute()
                .subscribe(
                    { operator: Operator -> operatorDataLoaded(operator) }
                ) { throwable: Throwable ->
                    Logger.e(TAG, "getOperatorFlowableUseCase error: " + throwable.message)
                }
        )
        updateChatHeadView()
    }


    override fun newEngagementLoaded(engagement: OmnibrowseEngagement) {
        onNewEngagementLoaded()
    }

    override fun newEngagementLoaded(engagement: OmnicoreEngagement?) {
        onNewEngagementLoaded()
    }

    private enum class State {
        ENDED, QUEUEING, ENGAGEMENT
    }
}
