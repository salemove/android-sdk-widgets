package com.glia.widgets.chat;

import com.glia.widgets.chat.adapter.ChatItem;
import com.glia.widgets.chat.adapter.MediaUpgradeStartedTimerItem;
import com.glia.widgets.helper.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatState {
    public final boolean integratorChatStarted;
    public final boolean isVisible;
    public final boolean isChatInBottom;
    public final Integer messagesNotSeen;
    public final String queueTicketId;
    public final boolean historyLoaded;
    public final String operatorName;
    public final String operatorProfileImgUrl;
    public final String companyName;
    public final String queueId;
    public final String contextUrl;
    public final boolean overlaysPermissionDialogShown;
    public final MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem;
    public final List<ChatItem> chatItems;
    public final ChatInputMode chatInputMode;
    public final String lastTypedText;

    public final boolean engagementRequested;
    public final boolean isNavigationPending;
    public final List<String> unsentMessages;

    private ChatState(
            String queueTicketId,
            boolean historyLoaded,
            String operatorName,
            String operatorProfileImgUrl,
            String companyName,
            String queueId,
            String contextUrl,
            boolean isVisible,
            boolean integratorChatStarted,
            boolean overlaysPermissionDialogShown,
            MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem,
            List<ChatItem> chatItems,
            ChatInputMode chatInputMode,
            String lastTypedText,
            boolean isChatInBottom,
            int messagesNotSeen,
            boolean engagementRequested,
            boolean isNavigationPending,
            List<String> unsentMessages) {
        this.queueTicketId = queueTicketId;
        this.historyLoaded = historyLoaded;
        this.operatorName = operatorName;
        this.operatorProfileImgUrl = operatorProfileImgUrl;
        this.companyName = companyName;
        this.queueId = queueId;
        this.contextUrl = contextUrl;
        this.isVisible = isVisible;
        this.integratorChatStarted = integratorChatStarted;
        this.overlaysPermissionDialogShown = overlaysPermissionDialogShown;
        this.mediaUpgradeStartedTimerItem = mediaUpgradeStartedTimerItem;
        this.chatItems = Collections.unmodifiableList(chatItems);
        this.chatInputMode = chatInputMode;
        this.lastTypedText = lastTypedText;
        this.isChatInBottom = isChatInBottom;
        this.messagesNotSeen = messagesNotSeen;
        this.engagementRequested = engagementRequested;
        this.isNavigationPending = isNavigationPending;
        this.unsentMessages = unsentMessages;
    }

    public boolean isOperatorOnline() {
        return operatorName != null;
    }

    public String getFormattedOperatorName() {
        return Utils.formatOperatorName(operatorName);
    }

    public boolean isMediaUpgradeStarted() {
        return mediaUpgradeStartedTimerItem != null;
    }

    public boolean showMessagesUnseenIndicator() {
        return !isChatInBottom && messagesNotSeen != null && messagesNotSeen > 0;
    }

    public static class Builder {
        private String queueTicketId;
        private boolean historyLoaded;
        private boolean isChatInBottom;
        private String operatorName;
        private String operatorProfileImgUrl;
        private String companyName;
        private String queueId;
        private String contextUrl;
        private boolean isVisible;
        private boolean integratorChatStarted;
        private boolean overlaysPermissionDialogShown;
        private MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem;
        private List<ChatItem> chatItems;
        private ChatInputMode chatInputMode;
        private String lastTypedText;
        private Integer messagesNotSeen;
        private boolean engagementRequested;
        private boolean isNavigationPending;
        private List<String> unsentMessages;

        public Builder copyFrom(ChatState chatState) {
            queueTicketId = chatState.queueTicketId;
            historyLoaded = chatState.historyLoaded;
            isChatInBottom = chatState.isChatInBottom;
            operatorName = chatState.operatorName;
            operatorProfileImgUrl = chatState.operatorProfileImgUrl;
            companyName = chatState.companyName;
            queueId = chatState.queueId;
            contextUrl = chatState.contextUrl;
            isVisible = chatState.isVisible;
            integratorChatStarted = chatState.integratorChatStarted;
            overlaysPermissionDialogShown = chatState.overlaysPermissionDialogShown;
            mediaUpgradeStartedTimerItem = chatState.mediaUpgradeStartedTimerItem;
            chatItems = chatState.chatItems;
            chatInputMode = chatState.chatInputMode;
            lastTypedText = chatState.lastTypedText;
            messagesNotSeen = chatState.messagesNotSeen;
            engagementRequested = chatState.engagementRequested;
            isNavigationPending = chatState.isNavigationPending;
            unsentMessages = chatState.unsentMessages;
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

        public Builder setOperatorProfileImgUrl(String operatorProfileImgUrl) {
            this.operatorProfileImgUrl = operatorProfileImgUrl;
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

        public Builder setOverlaysPermissionDialogShown(boolean overlaysPermissionDialogShown) {
            this.overlaysPermissionDialogShown = overlaysPermissionDialogShown;
            return this;
        }

        public Builder setMediaUpgradeStartedItem(MediaUpgradeStartedTimerItem mediaUpgradeStartedItem) {
            this.mediaUpgradeStartedTimerItem = mediaUpgradeStartedItem;
            return this;
        }

        public Builder setChatItems(List<ChatItem> chatItems) {
            this.chatItems = chatItems;
            return this;
        }

        public Builder setChatInputMode(ChatInputMode chatInputMode) {
            this.chatInputMode = chatInputMode;
            return this;
        }

        public Builder setLastTypedText(String lastTypedText) {
            this.lastTypedText = lastTypedText;
            return this;
        }

        public Builder setIsChatInBottom(boolean isChatInBottom) {
            this.isChatInBottom = isChatInBottom;
            return this;
        }

        public Builder setMessagesNotSeen(Integer messagesNotSeen) {
            this.messagesNotSeen = messagesNotSeen;
            return this;
        }

        public Builder setengagementRequested(boolean engagementRequested) {
            this.engagementRequested = engagementRequested;
            return this;
        }

        public Builder setIsNavigationPending(boolean isNavigationPending) {
            this.isNavigationPending = isNavigationPending;
            return this;
        }

        public Builder setUnsentMessages(List<String> unsentMessages) {
            this.unsentMessages = unsentMessages;
            return this;
        }

        public ChatState createChatState() {
            return new ChatState(queueTicketId, historyLoaded, operatorName, operatorProfileImgUrl, companyName, queueId, contextUrl, isVisible, integratorChatStarted, overlaysPermissionDialogShown, mediaUpgradeStartedTimerItem, chatItems, chatInputMode, lastTypedText, isChatInBottom, messagesNotSeen, engagementRequested, isNavigationPending, unsentMessages);
        }
    }

    public ChatState initChat(String companyName,
                              String queueId,
                              String contextUrl) {
        return new Builder()
                .copyFrom(this)
                .setIntegratorChatStarted(true)
                .setCompanyName(companyName)
                .setQueueId(queueId)
                .setContextUrl(contextUrl)
                .setIsVisible(true)
                .createChatState();
    }

    public ChatState queueingStarted() {
        return new Builder()
                .copyFrom(this)
                .setQueueTicketId(null)
                .setOperatorName(null)
                .setOperatorProfileImgUrl(null)
                .setChatInputMode(ChatInputMode.ENABLED)
                .setIntegratorChatStarted(true)
                .setengagementRequested(true)
                .createChatState();
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
                .setOperatorProfileImgUrl(null)
                .setIntegratorChatStarted(true)
                .createChatState();
    }


    public ChatState engagementStarted(String operatorName, String operatorProfileImgUrl) {
        return new Builder()
                .copyFrom(this)
                .setHistoryLoaded(false)
                .setOperatorName(operatorName)
                .setOperatorProfileImgUrl(operatorProfileImgUrl)
                .setIntegratorChatStarted(true)
                .setChatInputMode(ChatInputMode.ENABLED)
                .createChatState();
    }

    public ChatState stop() {
        return new Builder()
                .copyFrom(this)
                .setQueueTicketId(null)
                .setHistoryLoaded(false)
                .setOperatorName(null)
                .setOperatorProfileImgUrl(null)
                .setIsVisible(false)
                .setIntegratorChatStarted(false)
                .createChatState();
    }

    public ChatState historyLoaded(List<ChatItem> chatItems) {
        return new Builder()
                .copyFrom(this)
                .setChatInputMode(ChatInputMode.ENABLED_NO_ENGAGEMENT)
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

    public ChatState changeTimerItem(List<ChatItem> newItems, MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem) {
        return new Builder()
                .copyFrom(changeItems(newItems))
                .setMediaUpgradeStartedItem(mediaUpgradeStartedTimerItem)
                .createChatState();
    }

    public ChatState changeVisibility(boolean isVisible) {
        return new Builder()
                .copyFrom(this)
                .setIsVisible(isVisible)
                .createChatState();
    }

    public ChatState drawOverlayPermissionsDialogShown() {
        return new Builder()
                .copyFrom(this)
                .setOverlaysPermissionDialogShown(true)
                .createChatState();
    }

    public ChatState chatInputChanged(String text) {
        return new Builder()
                .copyFrom(this)
                .setLastTypedText(text)
                .createChatState();
    }

    public ChatState chatInputModeChanged(ChatInputMode chatInputMode) {
        return new Builder()
                .copyFrom(this)
                .setChatInputMode(chatInputMode)
                .createChatState();
    }

    public ChatState isInBottomChanged(boolean isChatInBottom) {
        return new Builder()
                .copyFrom(this)
                .setIsChatInBottom(isChatInBottom)
                .createChatState();
    }

    public ChatState messagesNotSeenChanged(int messagesNotSeen) {
        return new Builder()
                .copyFrom(this)
                .setMessagesNotSeen(messagesNotSeen)
                .createChatState();
    }

    public ChatState isNavigationPendingChanged(boolean isNavigationPending) {
        return new Builder()
                .copyFrom(this)
                .setIsNavigationPending(isNavigationPending)
                .createChatState();
    }

    public ChatState changeUnsentMessages(List<String> unsentMessages) {
        return new Builder()
                .copyFrom(this)
                .setUnsentMessages(Collections.unmodifiableList(unsentMessages))
                .createChatState();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatState chatState = (ChatState) o;
        return integratorChatStarted == chatState.integratorChatStarted &&
                isVisible == chatState.isVisible &&
                historyLoaded == chatState.historyLoaded &&
                overlaysPermissionDialogShown == chatState.overlaysPermissionDialogShown &&
                Objects.equals(queueTicketId, chatState.queueTicketId) &&
                Objects.equals(operatorName, chatState.operatorName) &&
                Objects.equals(operatorProfileImgUrl, chatState.operatorProfileImgUrl) &&
                Objects.equals(companyName, chatState.companyName) &&
                Objects.equals(queueId, chatState.queueId) &&
                Objects.equals(contextUrl, chatState.contextUrl) &&
                Objects.equals(mediaUpgradeStartedTimerItem, chatState.mediaUpgradeStartedTimerItem) &&
                Objects.equals(chatItems, chatState.chatItems) &&
                Objects.equals(chatInputMode, chatState.chatInputMode) &&
                Objects.equals(lastTypedText, chatState.lastTypedText) &&
                isChatInBottom == chatState.isChatInBottom &&
                engagementRequested == chatState.engagementRequested &&
                isNavigationPending == chatState.isNavigationPending &&
                Objects.equals(messagesNotSeen, chatState.messagesNotSeen) &&
                Objects.equals(chatItems, chatState.chatItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(integratorChatStarted, isVisible, isChatInBottom, queueTicketId, historyLoaded, operatorName, operatorProfileImgUrl, companyName, queueId, contextUrl, overlaysPermissionDialogShown, mediaUpgradeStartedTimerItem, chatItems, chatInputMode, lastTypedText, messagesNotSeen, engagementRequested, isNavigationPending, unsentMessages);
    }

    @Override
    public String toString() {
        return "ChatState{" +
                "integratorChatStarted=" + integratorChatStarted +
                ", isVisible=" + isVisible +
                ", queueTicketId='" + queueTicketId + '\'' +
                ", historyLoaded=" + historyLoaded +
                ", operatorName='" + operatorName + '\'' +
                ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                ", companyName='" + companyName + '\'' +
                ", queueId='" + queueId + '\'' +
                ", contextUrl='" + contextUrl + '\'' +
                ", overlaysPermissionDialogShown=" + overlaysPermissionDialogShown +
                ", mediaUpgradeStartedTimerItem=" + mediaUpgradeStartedTimerItem +
                ", chatInputMode=" + chatInputMode +
                ", lastTypedText: " + lastTypedText +
                ", messagesNotSeen: " + messagesNotSeen +
                ", isChatInBottom: " + isChatInBottom +
                ", engagementRequested: " + engagementRequested +
                ", isNavigationPending: " + isNavigationPending +
                ", unsentMessages: " + unsentMessages +
                ", chatItems=" + chatItems +
                '}';
    }
}
