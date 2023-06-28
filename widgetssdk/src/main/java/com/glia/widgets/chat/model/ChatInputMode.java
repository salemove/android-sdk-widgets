package com.glia.widgets.chat.model;

public enum ChatInputMode {
    ENABLED_NO_ENGAGEMENT,
    ENABLED,
    DISABLED;

    public boolean isEnabled() {
        return this == ENABLED_NO_ENGAGEMENT || this == ENABLED;
    }

}
