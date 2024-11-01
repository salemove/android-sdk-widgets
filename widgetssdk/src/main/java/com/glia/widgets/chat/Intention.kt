package com.glia.widgets.chat

internal enum class Intention {
    /* Restore Chat. Used in cases when navigating from chat screen directly, or from other screens with bubble */
    RESTORE_CHAT,

    /* Open Secure Conversation chat with Leave dialog, and if the visitor chooses to leave, open chat screen and start enqueueing for audio */
    SC_DIALOG_START_AUDIO,

    /* Open Secure Conversation chat with Leave dialog, and if the visitor chooses to leave, open chat screen and  start enqueueing for video */
    SC_DIALOG_START_VIDEO,

    /* Open Secure Conversation chat with Leave dialog, and if the visitor chooses to leave, start enqueueing for chat */
    SC_DIALOG_ENQUEUE,

    /* Open Secure Conversation chat */
    SC_CHAT,

    /* Open Live chat - visitor is authenticated */
    LIVE_CHAT_AUTHENTICATED,//TODO The _AUTHENTICATION part might be unnecessary

    /* Open Live chat - visitor is not authenticated */
    LIVE_CHAT_UNAUTHENTICATED;//TODO The _AUTHENTICATION part might be unnecessary

    val isSecureConversation: Boolean
        get() = when (this) {
            SC_CHAT, SC_DIALOG_ENQUEUE, SC_DIALOG_START_AUDIO, SC_DIALOG_START_VIDEO -> true
            else -> false
        }

    val isLive: Boolean
        get() = when (this) {
            /*TODO check if we need to include RESTORE_CHAT into this case*/
            LIVE_CHAT_AUTHENTICATED, LIVE_CHAT_UNAUTHENTICATED, RESTORE_CHAT -> true
            else -> false
        }
}
