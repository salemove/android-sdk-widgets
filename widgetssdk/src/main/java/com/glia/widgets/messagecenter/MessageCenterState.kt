package com.glia.widgets.messagecenter

data class State(
    val addAttachmentButtonEnabled: Boolean = true,
    val messageEditTextEnabled: Boolean = true,
    val sendMessageButtonState: ButtonState = ButtonState.DISABLE,
    val showMessageLimitError: Boolean = false
) {
    enum class ButtonState {
        NORMAL,
        DISABLE,
        PROGRESS
    }
}