package com.glia.widgets.chat;

import com.glia.widgets.chat.adapter.ChatItem;
import com.glia.widgets.fileupload.model.FileAttachment;
import com.glia.widgets.model.DialogsState;

import java.util.List;

public interface ChatViewCallback {

    void emitUploadAttachments(List<FileAttachment> attachments);

    void emitState(ChatState chatState);

    void emitItems(List<ChatItem> items);

    void navigateToCall(String requestedMediaType);

    void destroyView();

    void setLastTypedText(String lastTypedText);

    void smoothScrollToBottom();

    void scrollToBottomImmediate();
}
