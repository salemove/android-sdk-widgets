package com.glia.widgets.chat;

import com.glia.widgets.chat.adapter.ChatItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatState {
    public final boolean integratorChatStarted;
    public final boolean isVisible;
    public final boolean useFloatingChatHeads;
    public final String queueTicketId;
    public final boolean historyLoaded;
    public final String operatorName;
    public final String companyName;
    public final String queueId;
    public final String contextUrl;
    public final boolean hasOverlayPermissions;
    public final boolean overlaysPermissionDialogShown;
    public final List<ChatItem> chatItems;

    private ChatState(
            boolean useFloatingChatHeads,
            String queueTicketId,
            boolean historyLoaded,
            String operatorName,
            String companyName,
            String queueId,
            String contextUrl,
            boolean isVisible,
            boolean integratorChatStarted,
            boolean hasOverlayPermissions,
            boolean overlaysPermissionDialogShown,
            List<ChatItem> chatItems) {
        this.useFloatingChatHeads = useFloatingChatHeads;
        this.queueTicketId = queueTicketId;
        this.historyLoaded = historyLoaded;
        this.operatorName = operatorName;
        this.companyName = companyName;
        this.queueId = queueId;
        this.contextUrl = contextUrl;
        this.isVisible = isVisible;
        this.integratorChatStarted = integratorChatStarted;
        this.hasOverlayPermissions = hasOverlayPermissions;
        this.overlaysPermissionDialogShown = overlaysPermissionDialogShown;
        this.chatItems = Collections.unmodifiableList(chatItems);
    }

    public boolean isOperatorOnline() {
        return operatorName != null;
    }

    public String getFormattedOperatorName() {
        int i = operatorName.indexOf(' ');
        if (i != -1) {
            return operatorName.substring(0, i);
        } else {
            return operatorName;
        }
    }

    public static class Builder {
        private boolean useFloatingChatHeads;
        private String queueTicketId;
        private boolean historyLoaded;
        private String operatorName;
        private String companyName;
        private String queueId;
        private String contextUrl;
        private boolean isVisible;
        private boolean integratorChatStarted;
        private boolean hasOverlayPermissions;
        private boolean overlaysPermissionDialogShown;
        private List<ChatItem> chatItems;

        public Builder copyFrom(ChatState chatState) {
            useFloatingChatHeads = chatState.useFloatingChatHeads;
            queueTicketId = chatState.queueTicketId;
            historyLoaded = chatState.historyLoaded;
            operatorName = chatState.operatorName;
            companyName = chatState.companyName;
            queueId = chatState.queueId;
            contextUrl = chatState.contextUrl;
            isVisible = chatState.isVisible;
            integratorChatStarted = chatState.integratorChatStarted;
            hasOverlayPermissions = chatState.hasOverlayPermissions;
            overlaysPermissionDialogShown = chatState.overlaysPermissionDialogShown;
            chatItems = chatState.chatItems;
            return this;
        }

        public Builder setUseFloatingChatHeads(boolean useFloatingChatHeads) {
            this.useFloatingChatHeads = useFloatingChatHeads;
            return this;
        }

        public Builder setQueueTicketId(String queueTicketId) {
            this.queueTicketId = queueTicketId;
            return this;
        }

        public Builder setHistoryLoaded(boolean historyLoaded) {
            this.historyLoaded = historyLoaded;
            return this;
        }

        public Builder setOperatorName(String operatorName) {
            this.operatorName = operatorName;
            return this;
        }

        public Builder setCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder setQueueId(String queueId) {
            this.queueId = queueId;
            return this;
        }

        public Builder setContextUrl(String contextUrl) {
            this.contextUrl = contextUrl;
            return this;
        }

        public Builder setIsVisible(boolean isVisible) {
            this.isVisible = isVisible;
            return this;
        }

        public Builder setIntegratorChatStarted(boolean integratorChatStarted) {
            this.integratorChatStarted = integratorChatStarted;
            return this;
        }

        public Builder setHasOverlayPermissions(boolean hasOverlayPermissions) {
            this.hasOverlayPermissions = hasOverlayPermissions;
            return this;
        }

        public Builder setOverlaysPermissionDialogShown(boolean overlaysPermissionDialogShown) {
            this.overlaysPermissionDialogShown = overlaysPermissionDialogShown;
            return this;
        }

        public Builder setChatItems(List<ChatItem> chatItems) {
            this.chatItems = chatItems;
            return this;
        }

        public ChatState createChatState() {
            return new ChatState(useFloatingChatHeads, queueTicketId, historyLoaded, operatorName, companyName, queueId, contextUrl, isVisible, integratorChatStarted, hasOverlayPermissions, overlaysPermissionDialogShown, chatItems);
        }
    }

    public ChatState queueingStarted(boolean useChatHeads,
                                     String companyName,
                                     String queueId,
                                     String contextUrl) {
        return new Builder()
                .setUseFloatingChatHeads(useChatHeads)
                .setQueueTicketId(null)
                .setHistoryLoaded(false)
                .setOperatorName(null)
                .setCompanyName(companyName)
                .setQueueId(queueId)
                .setContextUrl(contextUrl)
                .setIsVisible(true)
                .setIntegratorChatStarted(true)
                .setChatItems(new ArrayList<>()).createChatState();
    }

    public ChatState queueTicketSuccess(String queueTicketId) {
        return new Builder()
                .copyFrom(this)
                .setQueueTicketId(queueTicketId)
                .setHistoryLoaded(false)
                .setIntegratorChatStarted(true)
                .createChatState();
    }

    public ChatState initQueueing() {
        return new Builder()
                .copyFrom(this)
                .setHistoryLoaded(false)
                .setOperatorName(null)
                .setIntegratorChatStarted(true)
                .createChatState();
    }


    public ChatState engagementStarted(String operatorName) {
        return new Builder()
                .copyFrom(this)
                .setHistoryLoaded(false)
                .setOperatorName(operatorName)
                .setIntegratorChatStarted(true)
                .createChatState();
    }

    public ChatState stop(boolean isVisible) {
        return new Builder()
                .copyFrom(this)
                .setQueueTicketId(null)
                .setHistoryLoaded(false)
                .setOperatorName(null)
                .setIsVisible(isVisible)
                .setIntegratorChatStarted(false)
                .createChatState();
    }

    public ChatState historyLoaded(List<ChatItem> chatItems) {
        return new Builder()
                .copyFrom(this)
                .setHistoryLoaded(true)
                .setChatItems(chatItems)
                .createChatState();
    }

    public ChatState changeItems(List<ChatItem> newItems) {
        return new Builder()
                .copyFrom(this)
                .setChatItems(newItems)
                .createChatState();
    }

    public ChatState show() {
        return new Builder()
                .copyFrom(this)
                .setIsVisible(true)
                .createChatState();
    }

    public ChatState hide() {
        return new Builder()
                .copyFrom(this)
                .setIsVisible(false)
                .createChatState();
    }

    public ChatState drawOverlaysPermissionChanged(boolean hasOverlaysPermission) {
        return new Builder()
                .copyFrom(this)
                .setHasOverlayPermissions(hasOverlaysPermission)
                .setOverlaysPermissionDialogShown(true)
                .createChatState();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatState chatState = (ChatState) o;
        return integratorChatStarted == chatState.integratorChatStarted &&
                isVisible == chatState.isVisible &&
                useFloatingChatHeads == chatState.useFloatingChatHeads &&
                historyLoaded == chatState.historyLoaded &&
                operatorName == chatState.operatorName &&
                hasOverlayPermissions == chatState.hasOverlayPermissions &&
                overlaysPermissionDialogShown == chatState.overlaysPermissionDialogShown &&
                Objects.equals(queueTicketId, chatState.queueTicketId) &&
                Objects.equals(companyName, chatState.companyName) &&
                Objects.equals(queueId, chatState.queueId) &&
                Objects.equals(contextUrl, chatState.contextUrl) &&
                Objects.equals(chatItems, chatState.chatItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(integratorChatStarted, isVisible, useFloatingChatHeads, queueTicketId, historyLoaded, operatorName, companyName, queueId, contextUrl, hasOverlayPermissions, overlaysPermissionDialogShown, chatItems);
    }

    @Override
    public String toString() {
        return "ChatState{" +
                "integratorChatStarted=" + integratorChatStarted +
                ", isVisible=" + isVisible +
                ", useFloatingChatHeads=" + useFloatingChatHeads +
                ", queueTicketId='" + queueTicketId + '\'' +
                ", historyLoaded=" + historyLoaded +
                ", operatorName='" + operatorName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", queueId='" + queueId + '\'' +
                ", contextUrl='" + contextUrl + '\'' +
                ", hasOverlayPermissions=" + hasOverlayPermissions +
                ", overlaysPermissionDialogShown=" + overlaysPermissionDialogShown +
                ", chatItems=" + chatItems +
                '}';
    }
}
