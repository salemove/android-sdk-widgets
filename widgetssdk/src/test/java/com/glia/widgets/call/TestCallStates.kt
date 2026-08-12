package com.glia.widgets.call

/**
 * Builds a [CallState] for tests. Lives in the `com.glia.widgets.call` package because
 * [CallState] is package-private.
 */
internal fun testCallState(
    callStatus: CallStatus,
    isVisible: Boolean = false,
    messagesNotSeen: Int = 0,
    isMuted: Boolean = false,
    hasVideo: Boolean = false,
    isSpeakerOn: Boolean = false
): CallState = CallState.Builder()
    .setVisible(isVisible)
    .setMessagesNotSeen(messagesNotSeen)
    .setCallStatus(callStatus)
    .setIsMuted(isMuted)
    .setHasVideo(hasVideo)
    .setIsSpeakerOn(isSpeakerOn)
    .createCallState()
