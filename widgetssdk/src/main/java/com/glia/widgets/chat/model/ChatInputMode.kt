package com.glia.widgets.chat.model

internal enum class ChatInputMode {
    ENABLED_NO_ENGAGEMENT,
    ENABLED,
    DISABLED;

    val isEnabled: Boolean
        get() = this == ENABLED_NO_ENGAGEMENT || this == ENABLED
}
