package com.glia.widgets.chat

/**
 * Determines which type of chat engagement to launch.
 */
internal enum class ChatType {
    /**
     * Regular engagements with live chat.
     */
    LIVE_CHAT,

    /**
     * Secure Messaging.
     *
     * It is a secure alternative to SMS and email, which also allows for asynchronous interactions.
     *
     * @see <a href="https://docs.glia.com/glia-impl/docs/secure-conversations">Secure conversations</a>
     */
    SECURE_MESSAGING;
}
