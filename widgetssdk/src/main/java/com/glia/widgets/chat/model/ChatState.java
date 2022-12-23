package com.glia.widgets.chat.model;

import androidx.annotation.NonNull;

import com.glia.widgets.chat.model.history.ChatItem;
import com.glia.widgets.chat.model.history.MediaUpgradeStartedTimerItem;
import com.glia.widgets.chat.model.history.OperatorStatusItem;
import com.glia.widgets.chat.model.history.VisitorMessageItem;
import com.glia.widgets.helper.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatState {
    public final boolean integratorChatStarted;
    public final boolean isVisible;
    public final boolean isChatInBottom;
    public final Integer messagesNotSeen;
    public final String operatorName;
    public final String operatorProfileImgUrl;
    public final String companyName;
    public final String queueId;
    public final String visitorContextAssetId;
    public final MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem;
    public final List<ChatItem> chatItems;
    public final ChatInputMode chatInputMode;
    public final String lastTypedText;
    public final boolean engagementRequested;
    public final String pendingNavigationType;
    public final List<VisitorMessageItem> unsentMessages;
    public final OperatorStatusItem operatorStatusItem;
    public final boolean showSendButton;
    public final boolean isAttachmentButtonEnabled;
    public final boolean isAttachmentButtonNeeded;
    public final boolean isOperatorTyping;
    public final boolean isAttachmentAllowed;
    public final boolean isSecureMessaging;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatState chatState = (ChatState) o;
        return integratorChatStarted == chatState.integratorChatStarted &&
                isVisible == chatState.isVisible &&
                Objects.equals(operatorName, chatState.operatorName) &&
                Objects.equals(operatorProfileImgUrl, chatState.operatorProfileImgUrl) &&
                Objects.equals(companyName, chatState.companyName) &&
                Objects.equals(queueId, chatState.queueId) &&
                Objects.equals(visitorContextAssetId, chatState.visitorContextAssetId) &&
                Objects.equals(mediaUpgradeStartedTimerItem, chatState.mediaUpgradeStartedTimerItem) &&
                Objects.equals(chatInputMode, chatState.chatInputMode) &&
                Objects.equals(lastTypedText, chatState.lastTypedText) &&
                isChatInBottom == chatState.isChatInBottom &&
                engagementRequested == chatState.engagementRequested &&
                Objects.equals(pendingNavigationType, chatState.pendingNavigationType) &&
                Objects.equals(messagesNotSeen, chatState.messagesNotSeen) &&
                Objects.equals(operatorStatusItem, chatState.operatorStatusItem) &&
                Objects.equals(unsentMessages, chatState.unsentMessages) &&
                Objects.equals(chatItems, chatState.chatItems) &&
                showSendButton == chatState.showSendButton &&
                isOperatorTyping == chatState.isOperatorTyping &&
                isAttachmentButtonEnabled == chatState.isAttachmentButtonEnabled &&
                isAttachmentButtonNeeded == chatState.isAttachmentButtonNeeded &&
                isAttachmentAllowed == chatState.isAttachmentAllowed &&
                isSecureMessaging == chatState.isSecureMessaging;
    }

    @Override
    public int hashCode() {
        return Objects.hash(integratorChatStarted, isVisible, isChatInBottom, operatorName, operatorProfileImgUrl, companyName, queueId, visitorContextAssetId, mediaUpgradeStartedTimerItem, chatItems, chatInputMode, lastTypedText, messagesNotSeen, engagementRequested, pendingNavigationType, unsentMessages, showSendButton, isOperatorTyping, isAttachmentButtonEnabled, isAttachmentButtonNeeded);
    }

    @NonNull
    @Override
    public String toString() {
        return "ChatState{" +
                "integratorChatStarted=" + integratorChatStarted +
                ", isVisible=" + isVisible +
                ", operatorName='" + operatorName + '\'' +
                ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                ", companyName='" + companyName + '\'' +
                ", queueId='" + queueId + '\'' +
                ", visitorContextAssetId='" + visitorContextAssetId + '\'' +
                ", mediaUpgradeStartedTimerItem=" + mediaUpgradeStartedTimerItem +
                ", chatInputMode=" + chatInputMode +
                ", lastTypedText: " + lastTypedText +
                ", messagesNotSeen: " + messagesNotSeen +
                ", isChatInBottom: " + isChatInBottom +
                ", engagementRequested: " + engagementRequested +
                ", pendingNavigationType: " + pendingNavigationType +
                ", operatorStatusItem: " + operatorStatusItem +
                ", unsentMessages: " + unsentMessages +
                ", chatItems=" + chatItems +
                ", showSendButton=" + showSendButton +
                ", isOperatorTyping=" + isOperatorTyping +
                ", isAttachmentButtonEnabled=" + isAttachmentButtonEnabled +
                ", isAttachmentButtonEnabled=" + isAttachmentButtonEnabled +
                ", isAttachmentAllowed=" + isAttachmentAllowed +
                ", isSecureMessaging=" + isSecureMessaging +
                '}';
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

    public boolean isAudioCallStarted() {
        return isMediaUpgradeStarted() &&
                mediaUpgradeStartedTimerItem.type == MediaUpgradeStartedTimerItem.Type.AUDIO;
    }

    public boolean showMessagesUnseenIndicator() {
        return !isChatInBottom && messagesNotSeen != null && messagesNotSeen > 0;
    }

    public boolean isAttachmentButtonVisible() {
        return isAttachmentButtonNeeded && isAttachmentAllowed;
    }

    public ChatState initChat(String companyName,
                              String queueId,
                              String visitorContextAssetId) {
        return new Builder()
                .copyFrom(this)
                .setIntegratorChatStarted(true)
                .setCompanyName(companyName)
                .setQueueId(queueId)
                .setVisitorContextAssetId(visitorContextAssetId)
                .setIsVisible(true)
                .setShowSendButton(false)
                .setIsAttachmentButtonEnabled(true)
                .setIsAttachmentAllowed(true)
                .createChatState();
    }

    public ChatState queueingStarted(OperatorStatusItem operatorStatusItem) {
        return new Builder()
                .copyFrom(this)
                .setOperatorName(null)
                .setOperatorProfileImgUrl(null)
                .setChatInputMode(ChatInputMode.ENABLED)
                .setEngagementRequested(true)
                .setOperatorStatusItem(operatorStatusItem)
                .createChatState();
    }

    public ChatState transferring() {
        return new Builder()
                .copyFrom(this)
                .setOperatorName(null)
                .setOperatorProfileImgUrl(null)
                .setEngagementRequested(true)
                .setOperatorStatusItem(OperatorStatusItem.TransferringStatusItem())
                .disableChatPanel()
                .createChatState();
    }

    public ChatState setSecureMessagingState() {
        return new Builder()
                .copyFrom(this)
                .setSecureMessaging(true)
                .enableChatPanel()
                .createChatState();
    }

    public ChatState setLiveChatState() {
        return new Builder()
                .copyFrom(this)
                .setSecureMessaging(false)
                .createChatState();
    }

    public ChatState allowSendAttachmentStateChanged(boolean isAttachmentAllowed) {
        return new Builder()
                .copyFrom(this)
                .setIsAttachmentAllowed(isAttachmentAllowed)
                .createChatState();
    }

    public ChatState engagementStarted() {
        return new Builder()
                .copyFrom(this)
                .enableChatPanel()
                .setEngagementRequested(true)
                .createChatState();
    }

    public ChatState operatorConnected(String operatorName, String operatorProfileImgUrl) {
        return new Builder()
                .copyFrom(this)
                .setOperatorName(operatorName)
                .setOperatorProfileImgUrl(operatorProfileImgUrl)
//                .enableChatPanel() // Why is this here?
                .createChatState();
    }

    public ChatState stop() {
        return new Builder()
                .copyFrom(this)
                .setOperatorName(null)
                .setOperatorProfileImgUrl(null)
                .setIsVisible(false)
                .setIntegratorChatStarted(false)
                .setIsAttachmentButtonNeeded(false)
                .createChatState();
    }

    public ChatState historyLoaded(List<ChatItem> chatItems) {
        return new Builder()
                .copyFrom(this)
                .setChatInputMode(ChatInputMode.ENABLED_NO_ENGAGEMENT)
                .setIsAttachmentButtonNeeded(false)
                .setChatItems(chatItems)
                .createChatState();
    }

    public ChatState changeItems(List<ChatItem> newItems) {
        return new Builder()
                .copyFrom(this)
                .setChatItems(newItems)
                .createChatState();
    }

    public ChatState changeTimerItem(
            List<ChatItem> newItems,
            MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem
    ) {
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

    public ChatState setLastTypedText(String text) {
        return new Builder()
                .copyFrom(this)
                .setLastTypedText(text)
                .createChatState();
    }

    public ChatState chatInputModeChanged(ChatInputMode chatInputMode) {
        return new Builder()
                .copyFrom(this)
                .setChatInputMode(chatInputMode)
                .setIsAttachmentButtonNeeded(chatInputMode == ChatInputMode.ENABLED)
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

    public ChatState setPendingNavigationType(String pendingNavigationType) {
        return new Builder()
                .copyFrom(this)
                .setPendingNavigationType(pendingNavigationType)
                .createChatState();
    }

    public ChatState changeUnsentMessages(List<VisitorMessageItem> unsentMessages) {
        return new Builder()
                .copyFrom(this)
                .setUnsentMessages(Collections.unmodifiableList(unsentMessages))
                .createChatState();
    }

    public ChatState setShowSendButton(boolean isShow) {
        return new Builder()
                .copyFrom(this)
                .setShowSendButton(isShow)
                .createChatState();
    }

    public ChatState setIsOperatorTyping(boolean isOperatorTyping) {
        return new Builder()
                .copyFrom(this)
                .setIsOperatorTyping(isOperatorTyping)
                .createChatState();
    }

    public ChatState setIsAttachmentButtonEnabled(boolean isAttachmentButtonEnabled) {
        return new Builder()
                .copyFrom(this)
                .setIsAttachmentButtonEnabled(isAttachmentButtonEnabled)
                .createChatState();
    }

    private ChatState(Builder builder) {
        this.operatorName = builder.operatorName;
        this.operatorProfileImgUrl = builder.operatorProfileImgUrl;
        this.companyName = builder.companyName;
        this.queueId = builder.queueId;
        this.visitorContextAssetId = builder.visitorContextAssetId;
        this.isVisible = builder.isVisible;
        this.integratorChatStarted = builder.integratorChatStarted;
        this.mediaUpgradeStartedTimerItem = builder.mediaUpgradeStartedTimerItem;
        this.chatItems = Collections.unmodifiableList(builder.chatItems);
        this.chatInputMode = builder.chatInputMode;
        this.lastTypedText = builder.lastTypedText;
        this.isChatInBottom = builder.isChatInBottom;
        this.messagesNotSeen = builder.messagesNotSeen;
        this.engagementRequested = builder.engagementRequested;
        this.pendingNavigationType = builder.pendingNavigationType;
        this.unsentMessages = builder.unsentMessages;
        this.operatorStatusItem = builder.operatorStatusItem;
        this.showSendButton = builder.showSendButton;
        this.isOperatorTyping = builder.isOperatorTyping;
        this.isAttachmentButtonEnabled = builder.isAttachmentButtonEnabled;
        this.isAttachmentButtonNeeded = builder.isAttachmentButtonNeeded;
        this.isAttachmentAllowed = builder.isAttachmentAllowed;
        this.isSecureMessaging = builder.isSecureMessaging;
    }

    public static class Builder {
        public boolean isOperatorTyping;
        public boolean isAttachmentButtonEnabled;
        public boolean isAttachmentButtonNeeded;
        private boolean isChatInBottom;
        private String operatorName;
        private String operatorProfileImgUrl;
        private String companyName;
        private String queueId;
        private boolean isVisible;
        private boolean integratorChatStarted;
        private MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem;
        private List<ChatItem> chatItems;
        private ChatInputMode chatInputMode;
        private String lastTypedText;
        private Integer messagesNotSeen;
        private boolean engagementRequested;
        private String pendingNavigationType;
        private List<VisitorMessageItem> unsentMessages;
        private OperatorStatusItem operatorStatusItem;
        private boolean showSendButton;
        private boolean isAttachmentAllowed;
        private String visitorContextAssetId;
        private boolean isSecureMessaging;

        public Builder copyFrom(ChatState chatState) {
            isChatInBottom = chatState.isChatInBottom;
            operatorName = chatState.operatorName;
            operatorProfileImgUrl = chatState.operatorProfileImgUrl;
            companyName = chatState.companyName;
            queueId = chatState.queueId;
            visitorContextAssetId = chatState.visitorContextAssetId;
            isVisible = chatState.isVisible;
            integratorChatStarted = chatState.integratorChatStarted;
            mediaUpgradeStartedTimerItem = chatState.mediaUpgradeStartedTimerItem;
            chatItems = chatState.chatItems;
            chatInputMode = chatState.chatInputMode;
            lastTypedText = chatState.lastTypedText;
            messagesNotSeen = chatState.messagesNotSeen;
            engagementRequested = chatState.engagementRequested;
            pendingNavigationType = chatState.pendingNavigationType;
            unsentMessages = chatState.unsentMessages;
            operatorStatusItem = chatState.operatorStatusItem;
            showSendButton = chatState.showSendButton;
            isOperatorTyping = chatState.isOperatorTyping;
            isAttachmentButtonEnabled = chatState.isAttachmentButtonEnabled;
            isAttachmentButtonNeeded = chatState.isAttachmentButtonNeeded;
            isAttachmentAllowed = chatState.isAttachmentAllowed;
            isSecureMessaging = chatState.isSecureMessaging;
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

        public Builder setIsVisible(boolean isVisible) {
            this.isVisible = isVisible;
            return this;
        }

        public Builder setIntegratorChatStarted(boolean integratorChatStarted) {
            this.integratorChatStarted = integratorChatStarted;
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

        public Builder setEngagementRequested(boolean engagementRequested) {
            this.engagementRequested = engagementRequested;
            return this;
        }

        public Builder setPendingNavigationType(String pendingNavigationType) {
            this.pendingNavigationType = pendingNavigationType;
            return this;
        }

        public Builder setUnsentMessages(List<VisitorMessageItem> unsentMessages) {
            this.unsentMessages = unsentMessages;
            return this;
        }

        public Builder setOperatorStatusItem(OperatorStatusItem operatorStatusItem) {
            this.operatorStatusItem = operatorStatusItem;
            return this;
        }

        public Builder setShowSendButton(boolean isShow) {
            this.showSendButton = isShow;
            return this;
        }

        public Builder setIsOperatorTyping(boolean isOperatorTyping) {
            this.isOperatorTyping = isOperatorTyping;
            return this;
        }

        public Builder setIsAttachmentButtonEnabled(boolean isAttachmentButtonEnabled) {
            this.isAttachmentButtonEnabled = isAttachmentButtonEnabled;
            return this;
        }

        public Builder setIsAttachmentButtonNeeded(boolean isAttachmentButtonNeeded) {
            this.isAttachmentButtonNeeded = isAttachmentButtonNeeded;
            return this;
        }

        public Builder setIsAttachmentAllowed(boolean isAttachmentAllowed) {
            this.isAttachmentAllowed = isAttachmentAllowed;
            return this;
        }

        public ChatState createChatState() {
            return new ChatState(this);
        }

        public Builder setVisitorContextAssetId(String visitorContextAssetId) {
            this.visitorContextAssetId = visitorContextAssetId;
            return this;
        }

        public Builder setSecureMessaging(boolean secureMessaging) {
            this.isSecureMessaging = secureMessaging;
            return this;
        }

        public Builder enableChatPanel() {
            setChatInputMode(ChatInputMode.ENABLED);
            setIsAttachmentButtonNeeded(true);
            return this;
        }

        public Builder disableChatPanel() {
            setChatInputMode(ChatInputMode.DISABLED);
            setShowSendButton(false);
            setIsAttachmentButtonNeeded(false);
            return this;
        }
    }
}
