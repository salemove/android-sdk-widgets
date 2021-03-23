package com.glia.widgets.chat.adapter;

import com.glia.androidsdk.chat.SingleChoiceOption;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ReceiveMessageItemMessage {
    public final String content;
    public final List<SingleChoiceOption> attachments;
    public final Integer selectedIndex;

    public ReceiveMessageItemMessage(
            String content,
            List<SingleChoiceOption> attachments,
            Integer selectedIndex
    ) {
        this.content = content;
        this.attachments = attachments != null ? Collections.unmodifiableList(attachments) : null;
        this.selectedIndex = selectedIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReceiveMessageItemMessage that = (ReceiveMessageItemMessage) o;
        return Objects.equals(content, that.content) &&
                Objects.equals(attachments, that.attachments) &&
                Objects.equals(selectedIndex, that.selectedIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, attachments, selectedIndex);
    }

    @Override
    public String toString() {
        return "ReceiveMessageItemMessage{" +
                "content='" + content + '\'' +
                ", attachments=" + attachments +
                ", selectedIndex=" + selectedIndex +
                '}';
    }
}
