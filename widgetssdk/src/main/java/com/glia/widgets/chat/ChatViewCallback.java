package com.glia.widgets.chat;

import java.util.List;

public interface ChatViewCallback {

    void queueing(OperatorStatusItem item);

    void chatStarted(OperatorStatusItem item);

    void appendItem(ChatItem item);

    void replaceReceiverItem(ReceiveMessageItem item);

    void replaceItems(List<ChatItem> items);

    void engagementEndShowNoMoreOperatorsDialog();

    void engagementEndNoDialog();

    void unexpectedError();
}
