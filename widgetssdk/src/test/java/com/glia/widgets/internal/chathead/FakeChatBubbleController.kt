package com.glia.widgets.internal.chathead

import com.glia.widgets.internal.chathead.domain.ResolveChatHeadNavigationUseCase.Destinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Stands in for [ChatBubbleController] in host tests: the hosts only ever collect [uiState] and push
 * events back, so a plain [MutableStateFlow] is all they need.
 */
internal class FakeChatBubbleController(
    override var bubblePosition: BubblePosition? = null
) : ChatBubbleContract.Controller {

    val state: MutableStateFlow<BubbleUiModel> = MutableStateFlow(BubbleUiModel.INITIAL)
    override val uiState: StateFlow<BubbleUiModel> = state

    val contextEvents: MutableList<BubbleContextEvent> = mutableListOf()
    var tapCount: Int = 0
    var tapDestination: Destinations = Destinations.CHAT_VIEW
    var fromCallScreen: Boolean = false
    var resetFromCallScreenCount: Int = 0
    var isDestroyed: Boolean = false

    fun emit(
        target: BubbleRenderTarget = BubbleRenderTarget.NONE,
        content: BubbleContent = BubbleContent.Ended,
        unreadCount: Int = 0,
        isOnHold: Boolean = false,
        isOnChatScreen: Boolean = false
    ) {
        state.value = BubbleUiModel(target, content, unreadCount, isOnHold, isOnChatScreen)
    }

    override fun onContextEvent(event: BubbleContextEvent) {
        contextEvents += event
    }

    override fun onBubbleTapped(): Destinations {
        tapCount++
        return tapDestination
    }

    override fun isFromCallScreen(): Boolean = fromCallScreen

    override fun resetFromCallScreen() {
        resetFromCallScreenCount++
        fromCallScreen = false
    }

    override fun onDestroy() {
        isDestroyed = true
    }
}
