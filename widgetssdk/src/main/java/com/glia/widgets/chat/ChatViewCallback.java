package com.glia.widgets.chat;

import com.glia.widgets.chat.adapter.ChatItem;
import com.glia.widgets.model.DialogsState;

import java.util.List;

public interface ChatViewCallback {

    void emitState(ChatState chatState);

    void emitItems(List<ChatItem> items);

    void emitDialog(DialogsState dialogsState);

    void handleFloatingChatHead(String returnDestination);

    void navigateToCall();

    void destroyView();
}
