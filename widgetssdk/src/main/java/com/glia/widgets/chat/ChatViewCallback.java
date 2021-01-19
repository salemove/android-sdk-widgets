package com.glia.widgets.chat;

import android.util.Pair;

import com.glia.widgets.chat.adapter.ChatItem;

import java.util.List;

public interface ChatViewCallback {

    void emitState(ChatState chatState);

    void emitItems(List<ChatItem> items,
                   Pair<Integer, Integer> indexesToBeInvalidated,
                   boolean scrollToBottom);

    void emitDialog(DialogsState dialogsState);

    void handleFloatingChatHead(boolean show);
}
