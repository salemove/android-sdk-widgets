package com.glia.widgets.chat.model;

public enum ChatInputMode {
    ENABLED_NO_ENGAGEMENT,
    ENABLED,
    SINGLE_CHOICE_CARD,
    DISABLED;

    public boolean isEnabled() {
        return this == ENABLED_NO_ENGAGEMENT || this == ENABLED;
    }

}
