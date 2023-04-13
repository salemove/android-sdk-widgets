package com.glia.widgets.chat.model.history;

import com.glia.widgets.chat.adapter.ChatAdapter;

import java.util.Objects;

public class ChatItem {
    @ChatAdapter.Type
    private final int viewType;
    private final String id;

    protected ChatItem(String id, @ChatAdapter.Type int viewType) {
        this.id = id;
        this.viewType = viewType;
    }

    @ChatAdapter.Type
    public int getViewType() {
        return viewType;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatItem chatItem = (ChatItem) o;
        return viewType == chatItem.viewType &&
                id.equals(chatItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(viewType, id);
    }
}
