package com.glia.widgets.chat

internal enum class Intention {
    /* Return to Chat. Used in cases when navigating from other screens with bubble */
    RETURN_TO_CHAT,

    /* Open Secure Conversation chat with Leave dialog, and if the visitor chooses to leave, open chat screen and start enqueueing for audio */
    SC_DIALOG_START_AUDIO,

    /* Open Secure Conversation chat with Leave dialog, and if the visitor chooses to leave, open chat screen and  start enqueueing for video */
    SC_DIALOG_START_VIDEO,

    /* Open Secure Conversation chat with Leave dialog, and if the visitor chooses to leave, start enqueueing for chat */
    SC_DIALOG_ENQUEUE_FOR_TEXT,

    /* Open Secure Conversation chat */
    SC_CHAT,

    /* Open Live chat */
    LIVE_CHAT;

    val isSecureConversation: Boolean
        get() = when (this) {
            SC_CHAT, SC_DIALOG_ENQUEUE_FOR_TEXT, SC_DIALOG_START_AUDIO, SC_DIALOG_START_VIDEO -> true
            else -> false
        }

}
