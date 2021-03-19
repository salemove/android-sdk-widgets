package com.glia.widgets.chat.adapter;

import com.glia.androidsdk.chat.SingleChoiceOption;

import java.util.List;

public class ReceiveMessageItemMessage {
    final String content;
    final List<SingleChoiceOption> attachments;

    public ReceiveMessageItemMessage(String content, List<SingleChoiceOption> attachments) {
        this.content = content;
        this.attachments = attachments;
    }
}
