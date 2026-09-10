package com.glia.widgets.internal.chathead

/**
 * Where the bubble renders right now.
 *
 * [SERVICE] is the system-overlay bubble hosted by `ChatHeadService`, [APPLICATION] is the in-app
 * bubble attached to the resumed activity's root, [NONE] means no bubble is shown.
 */
internal enum class BubbleRenderTarget { SERVICE, APPLICATION, NONE }

/**
 * What the bubble shows.
 */
internal sealed interface BubbleContent {
    /** Queueing animation while the visitor waits for an operator. */
    data object Queueing : BubbleContent

    /** Live engagement; [operatorImageUrl] is `null` when the operator has no avatar. */
    data class Engaged(val operatorImageUrl: String?) : BubbleContent

    /** No engagement — placeholder icon. */
    data object Ended : BubbleContent
}

/**
 * Everything a bubble host needs to draw itself. [unreadCount] is already 0 when the badge is
 * suppressed, so hosts apply no rules of their own.
 *
 * [isOnChatScreen] is true while the chat screen is the one the bubble would sit on. A bubble there
 * is part of the chat UI and is themed by `chatTheme.bubble`; everywhere else — any other screen and
 * the overlay — it is the standalone bubble, themed by the top-level `bubbleTheme`.
 */
internal data class BubbleUiModel(
    val target: BubbleRenderTarget,
    val content: BubbleContent,
    val unreadCount: Int,
    val isOnHold: Boolean,
    val isOnChatScreen: Boolean
) {
    internal companion object {
        val INITIAL: BubbleUiModel = BubbleUiModel(BubbleRenderTarget.NONE, BubbleContent.Ended, 0, false, false)
    }
}
