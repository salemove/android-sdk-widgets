package com.glia.widgets.internal.chathead

import android.annotation.SuppressLint
import com.glia.androidsdk.Operator
import com.glia.telemetry_lib.BubbleType
import com.glia.widgets.callbacks.OnResult
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.EndAction
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.imageUrl
import com.glia.widgets.internal.chathead.domain.DecideBubbleRenderTargetUseCase
import com.glia.widgets.internal.chathead.domain.ResolveChatHeadNavigationUseCase
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.head.ChatHeadLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.glia.widgets.engagement.State as EngagementState

/**
 * The single owner of the chat bubble. It subscribes to every source once, keeps the bubble state in
 * private fields, and republishes a [BubbleUiModel] whenever anything changes.
 *
 * Inputs arrive on RxJava threads, so every mutation happens under [lock] together with the
 * [recompute] that follows it — that keeps the render-target transition edges, which is what the
 * telemetry counts, from being observed twice. No `CoroutineScope` is needed: the controller only
 * ever writes to a `MutableStateFlow`; the hosts own the collection.
 *
 * This instance lives for the whole process, mirroring the controllers it replaces.
 */
@SuppressLint("CheckResult")
internal class ChatBubbleController(
    private val decideBubbleRenderTargetUseCase: DecideBubbleRenderTargetUseCase,
    private val resolveChatHeadNavigationUseCase: ResolveChatHeadNavigationUseCase,
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    private val isFromCallScreenUseCase: IsFromCallScreenUseCase,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val chatHeadManager: ChatHeadManager,
    messagesNotSeenHandler: MessagesNotSeenHandler,
    engagementStateUseCase: EngagementStateUseCase,
    currentOperatorUseCase: CurrentOperatorUseCase,
    visitorMediaUseCase: VisitorMediaUseCase
) : ChatBubbleContract.Controller {

    private val lock = Any()

    private val _uiState: MutableStateFlow<BubbleUiModel> = MutableStateFlow(BubbleUiModel.INITIAL)
    override val uiState: StateFlow<BubbleUiModel> = _uiState.asStateFlow()

    override var bubblePosition: BubblePosition? = null

    private var engagementPhase: EngagementPhase = EngagementPhase.ENDED
    private var operator: Operator? = null
    private var unreadMessagesCount: Int = 0
    private var isOnHold: Boolean = false

    /**
     * The topmost screen, or `null` while nothing that could host a bubble is resumed. Set verbatim
     * from [BubbleContextEvent.ScreenChanged] — the host owns the "which activity is on top" question.
     */
    private var resumedScreen: VisibleScreen? = null

    private val bubbleContext: BubbleContext
        get() = resumedScreen?.let(BubbleContext::InApp) ?: BubbleContext.AppInBackground

    private val engagementEndCallback: OnResult<Int> = OnResult { unreadMessageCount ->
        // The bubble stays on the screen while there are unread messages, to let the visitor know
        if (unreadMessageCount == 0) {
            synchronized(lock) { resetState() }
            Dependencies.secureConversations.unSubscribeFromUnreadMessageCount(engagementEndCallback)
        }
    }

    init {
        engagementStateUseCase().subscribe(::handleEngagementState) { Logger.e(TAG, "Bubble: engagement state failed", it) }
        currentOperatorUseCase().subscribe(::onOperatorLoaded) { Logger.e(TAG, "Bubble: current operator failed", it) }
        visitorMediaUseCase.onHoldState.subscribe(::onHoldChanged) { Logger.e(TAG, "Bubble: on hold state failed", it) }
        messagesNotSeenHandler.addListener(::onUnreadMessageCountChanged)
    }

    override fun onContextEvent(event: BubbleContextEvent) {
        synchronized(lock) {
            resumedScreen = when (event) {
                is BubbleContextEvent.ScreenChanged -> event.screen
                BubbleContextEvent.AppBackgrounded -> null
            }
            recompute()
        }
    }

    override fun onBubbleTapped(): ResolveChatHeadNavigationUseCase.Destinations {
        val destination = resolveChatHeadNavigationUseCase.execute()
        ChatHeadLogger.logChatHeadClicked()
        return destination
    }

    override fun isFromCallScreen(): Boolean = isFromCallScreenUseCase()

    override fun resetFromCallScreen() {
        updateFromCallScreenUseCase(false)
    }

    override fun onDestroy() {
        synchronized(lock) {
            Dependencies.secureConversations.unSubscribeFromUnreadMessageCount(engagementEndCallback)
            resetState()
            chatHeadManager.stopChatHeadService()
        }
    }

    private fun handleEngagementState(state: EngagementState): Unit = synchronized(lock) {
        when (state) {
            is EngagementState.EngagementStarted -> {
                isOnHold = false
                engagementPhase = EngagementPhase.ENGAGEMENT
                recompute()
            }

            is EngagementState.Update -> {
                engagementPhase = EngagementPhase.ENGAGEMENT
                recompute()
            }

            is EngagementState.Queuing,
            is EngagementState.PreQueuing -> {
                engagementPhase = EngagementPhase.QUEUEING
                recompute()
            }

            is EngagementState.EngagementEnded -> engagementEnded(state.endAction)

            is EngagementState.QueueUnstaffed,
            is EngagementState.UnexpectedErrorHappened,
            is EngagementState.QueueingCanceled -> engagementEnded(null)

            else -> {
                //no op
            }
        }
    }

    private fun engagementEnded(action: EndAction?) {
        if (action == EndAction.Retain) {
            Dependencies.secureConversations.subscribeToUnreadMessageCount(engagementEndCallback)
        } else {
            resetState()
        }
    }

    private fun resetState() {
        isOnHold = false
        engagementPhase = EngagementPhase.ENDED
        operator = null
        unreadMessagesCount = 0
        // No engagement means no bubble is needed anywhere, so this both drops the target to NONE and
        // stops the overlay service.
        recompute()
        ChatHeadLogger.reset()
    }

    private fun onOperatorLoaded(operator: Operator) {
        synchronized(lock) {
            this.operator = operator
            recompute()
        }
    }

    private fun onHoldChanged(isOnHold: Boolean) {
        synchronized(lock) {
            this.isOnHold = isOnHold
            recompute()
        }
    }

    private fun onUnreadMessageCountChanged(count: Int) {
        synchronized(lock) {
            unreadMessagesCount = count
            recompute()
        }
    }

    private fun recompute() {
        val target: BubbleRenderTarget = decideBubbleRenderTargetUseCase(bubbleContext)
        val model = BubbleUiModel(
            target = target,
            content = bubbleContent(),
            // The Call Visualizer engagement has no chat to have unread messages in
            unreadCount = if (isCurrentEngagementCallVisualizerUseCase()) 0 else unreadMessagesCount,
            isOnHold = isOnHold && engagementPhase == EngagementPhase.ENGAGEMENT,
            isOnChatScreen = resumedScreen == VisibleScreen.CHAT
        )

        applyOverlayServiceLifetime()
        applyTargetTransition(from = _uiState.value.target, to = target)
        _uiState.value = model
    }

    /**
     * The service runs for as long as the overlay bubble could be needed, which is wider than the
     * bubble actually being visible — see `isOverlayServiceNeeded`. `ChatHeadManager` ignores a
     * redundant start or stop, so this runs unconditionally rather than only on a change: a start
     * that the OS refused while the app was in the background is then retried on the next state
     * change instead of being lost.
     */
    private fun applyOverlayServiceLifetime() {
        if (decideBubbleRenderTargetUseCase.isOverlayServiceNeeded) {
            chatHeadManager.startChatHeadService()
        } else {
            chatHeadManager.stopChatHeadService()
        }
    }

    private fun bubbleContent(): BubbleContent = when (engagementPhase) {
        EngagementPhase.ENGAGEMENT -> {
            // The operator image is only ever drawn here, so its presence marks the operator as
            // connected. Recomputing happens many times per engagement; ChatHeadLogger drops the
            // duplicate logs.
            operator?.also(ChatHeadLogger::logOperatorConnected)
            BubbleContent.Engaged(operator?.imageUrl)
        }

        EngagementPhase.QUEUEING -> BubbleContent.Queueing
        EngagementPhase.ENDED -> BubbleContent.Ended
    }

    /**
     * Telemetry only: the hosts add and remove their own views off [uiState], so this reports what the
     * visitor sees rather than driving it.
     */
    private fun applyTargetTransition(from: BubbleRenderTarget, to: BubbleRenderTarget) {
        if (from == to) return

        when (from) {
            BubbleRenderTarget.SERVICE -> {
                Logger.d(TAG, "Bubble: hide device bubble")
                ChatHeadLogger.logChatHeadHidden(BubbleType.SERVICE)
            }

            BubbleRenderTarget.APPLICATION -> ChatHeadLogger.logChatHeadHidden(BubbleType.IN_APP)
            BubbleRenderTarget.NONE -> Unit
        }

        when (to) {
            BubbleRenderTarget.SERVICE -> {
                Logger.i(TAG, "Bubble: show device bubble")
                ChatHeadLogger.logChatHeadShown(BubbleType.SERVICE)
            }

            BubbleRenderTarget.APPLICATION -> ChatHeadLogger.logChatHeadShown(BubbleType.IN_APP)
            BubbleRenderTarget.NONE -> Unit
        }
    }

    private enum class EngagementPhase {
        ENDED,
        QUEUEING,
        ENGAGEMENT
    }
}
