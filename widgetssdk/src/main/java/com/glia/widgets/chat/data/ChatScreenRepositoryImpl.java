package com.glia.widgets.chat.data;

public class ChatScreenRepositoryImpl implements ChatScreenRepository {
    private boolean fromCallScreen;

    @Override
    public boolean isFromCallScreen() {
        return fromCallScreen;
    }

    @Override
    public void setFromCallScreen(boolean fromCallScreen) {
        this.fromCallScreen = fromCallScreen;
    }
}