package com.glia.widgets.view.head.model;

import com.glia.widgets.UiTheme;

import java.util.Objects;

public class ChatHeadInput {
    public final String companyName;
    public final String queueId;
    public final String contextUrl;
    public final UiTheme uiTheme;

    public ChatHeadInput(
            String companyName,
            String queueId,
            String contextUrl,
            UiTheme uiTheme
    ) {
        this.companyName = companyName;
        this.queueId = queueId;
        this.contextUrl = contextUrl;
        this.uiTheme = uiTheme;
    }

    @Override
    public String toString() {
        return "ChatHeadInput{" +
                "companyName='" + companyName + '\'' +
                ", queueId='" + queueId + '\'' +
                ", contextUrl='" + contextUrl + '\'' +
                ", uiTheme=" + uiTheme +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatHeadInput that = (ChatHeadInput) o;
        return Objects.equals(companyName, that.companyName) &&
                Objects.equals(queueId, that.queueId) &&
                Objects.equals(contextUrl, that.contextUrl) &&
                Objects.equals(uiTheme, that.uiTheme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName, queueId, contextUrl, uiTheme);
    }
}
