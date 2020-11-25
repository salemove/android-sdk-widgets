package com.glia.widgets.chat;

class ChatItem {
    private final int viewType;

    protected ChatItem(int viewType) {
        this.viewType = viewType;
    }

    public int getViewType() {
        return viewType;
    }
}
