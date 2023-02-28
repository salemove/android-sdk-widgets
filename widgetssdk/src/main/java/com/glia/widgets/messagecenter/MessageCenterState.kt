package com.glia.widgets.messagecenter

data class State(
    val addAttachmentButtonVisible: Boolean = false,
    val addAttachmentButtonEnabled: Boolean = true,
    val messageEditTextEnabled: Boolean = true,
    val sendMessageButtonState: ButtonState = ButtonState.DISABLE,
    val showMessageLimitError: Boolean = false,
    val showSendMessageGroup: Boolean = true
) {
    enum class ButtonState {
        NORMAL,
        DISABLE,
        PROGRESS
    }
}
