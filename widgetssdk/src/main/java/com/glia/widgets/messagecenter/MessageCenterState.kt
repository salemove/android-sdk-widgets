package com.glia.widgets.messagecenter

internal data class MessageCenterState(
    val addAttachmentButtonVisible: Boolean = false,
    val addAttachmentButtonEnabled: Boolean = true,
    val messageEditTextEnabled: Boolean = true,
    val sendMessageButtonState: ButtonState = ButtonState.DISABLE,
    val showMessageLimitError: Boolean = false,
    val showSendMessageGroup: Boolean = false
) {
    enum class ButtonState {
        NORMAL,
        DISABLE,
        PROGRESS
    }
}
