package com.glia.widgets.head;

import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.widgets.UiTheme;

import java.util.Objects;

class ChatHeadState {

    public final int messageCount;
    public final String operatorProfileImgUrl;
    public final UiTheme theme;
    public final OperatorMediaState operatorMediaState;
    public final String returnDestination;
    public final boolean areIntegratedViewsVisible;
    public final boolean isOverlayVisible;
    public final boolean useOverlays;
    public final boolean hasOverlayPermissions;
    // if false then chat heads are off completely
    public final boolean useChatHeads;

    private ChatHeadState(
            int messageCount,
            String operatorProfileImgUrl,
            UiTheme theme,
            OperatorMediaState operatorMediaState,
            String returnDestination,
            boolean areIntegratedViewsVisible,
            boolean isOverlayVisible,
            boolean useOverlays,
            boolean hasOverlayPermissions,
            boolean useChatHeads) {
        this.messageCount = messageCount;
        this.operatorProfileImgUrl = operatorProfileImgUrl;
        this.theme = theme;
        this.operatorMediaState = operatorMediaState;
        this.returnDestination = returnDestination;
        this.areIntegratedViewsVisible = areIntegratedViewsVisible;
        this.isOverlayVisible = isOverlayVisible;
        this.useOverlays = useOverlays;
        this.hasOverlayPermissions = hasOverlayPermissions;
        this.useChatHeads = useChatHeads;
    }

    public ChatHeadState onNewMessage(int messageCount) {
        return new Builder()
                .copyFrom(this)
                .setMessageCount(messageCount)
                .createChatHeadState();
    }

    public ChatHeadState setOperatorProfileImgUrl(String operatorProfileImgUrl) {
        return new Builder()
                .copyFrom(this)
                .setOperatorProfileImgUrl(operatorProfileImgUrl)
                .createChatHeadState();
    }

    public ChatHeadState setOperatorMediaState(OperatorMediaState operatorMediaState) {
        return new Builder()
                .copyFrom(this)
                .setOperatorMediaState(operatorMediaState)
                .createChatHeadState();
    }

    public ChatHeadState themeChanged(UiTheme theme) {
        return new Builder().copyFrom(this).setUiTheme(theme).createChatHeadState();
    }

    public ChatHeadState changeVisibility(boolean isVisible, String returnDestination) {
        return new Builder()
                .copyFrom(this)
                .setReturnDestination(returnDestination)
                .setIntegratedViewsVisible(!useOverlays && isVisible)
                .setIsOverlayVisible(useOverlays && isVisible)
                .createChatHeadState();
    }

    public ChatHeadState setUseOverlays(boolean useOverlays) {
        return new Builder().copyFrom(this).setUseOverlays(useOverlays).createChatHeadState();
    }

    public ChatHeadState setHasOverlayPermissions(boolean hasOverlayPermissions) {
        return new Builder().copyFrom(this).setHasOverlayPermissions(hasOverlayPermissions).createChatHeadState();
    }

    public ChatHeadState enableChatHeadsChanged(boolean enableChatHeads) {
        return new Builder().copyFrom(this).setUseChatHeads(enableChatHeads).createChatHeadState();
    }

    public static class Builder {
        private int messageCount;
        private String operatorProfileImgUrl;
        private UiTheme theme;
        private OperatorMediaState operatorMediaState;
        private String returnDestination;
        private boolean areIntegratedViewsVisible;
        private boolean isOverlayVisible;
        private boolean useOverlays;
        private boolean hasOverlayPermissions;
        // if false then chat heads are off completely
        private boolean useChatHeads;

        public Builder copyFrom(ChatHeadState chatHeadState) {
            messageCount = chatHeadState.messageCount;
            operatorProfileImgUrl = chatHeadState.operatorProfileImgUrl;
            theme = chatHeadState.theme;
            operatorMediaState = chatHeadState.operatorMediaState;
            returnDestination = chatHeadState.returnDestination;
            areIntegratedViewsVisible = chatHeadState.areIntegratedViewsVisible;
            isOverlayVisible = chatHeadState.isOverlayVisible;
            useOverlays = chatHeadState.useOverlays;
            hasOverlayPermissions = chatHeadState.hasOverlayPermissions;
            useChatHeads = chatHeadState.useChatHeads;
            return this;
        }

        public Builder setMessageCount(int messageCount) {
            this.messageCount = messageCount;
            return this;
        }

        public Builder setOperatorProfileImgUrl(String operatorProfileImgUrl) {
            this.operatorProfileImgUrl = operatorProfileImgUrl;
            return this;
        }

        public Builder setUiTheme(UiTheme theme) {
            this.theme = theme;
            return this;
        }

        public Builder setOperatorMediaState(OperatorMediaState operatorMediaState) {
            this.operatorMediaState = operatorMediaState;
            return this;
        }

        public Builder setReturnDestination(String returnDestination) {
            this.returnDestination = returnDestination;
            return this;
        }

        public Builder setIntegratedViewsVisible(boolean isVisible) {
            this.areIntegratedViewsVisible = isVisible;
            return this;
        }

        public Builder setIsOverlayVisible(boolean isOverlayVisible) {
            this.isOverlayVisible = isOverlayVisible;
            return this;
        }

        public Builder setUseOverlays(boolean useOverlays) {
            this.useOverlays = useOverlays;
            return this;
        }

        public Builder setHasOverlayPermissions(boolean hasOverlayPermissions) {
            this.hasOverlayPermissions = hasOverlayPermissions;
            return this;
        }

        public Builder setUseChatHeads(boolean useChatHeads) {
            this.useChatHeads = useChatHeads;
            return this;
        }

        public ChatHeadState createChatHeadState() {
            return new ChatHeadState(
                    messageCount,
                    operatorProfileImgUrl,
                    theme,
                    operatorMediaState,
                    returnDestination,
                    areIntegratedViewsVisible,
                    isOverlayVisible,
                    useOverlays,
                    hasOverlayPermissions,
                    useChatHeads
            );
        }
    }

    @Override
    public String toString() {
        return "ChatHeadState{" +
                "messageCount=" + messageCount +
                ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                ", theme=" + theme +
                ", operatorMediaState=" + operatorMediaState +
                ", returnDestination='" + returnDestination + '\'' +
                ", areIntegratedViewsVisible=" + areIntegratedViewsVisible +
                ", isOverlayVisible=" + isOverlayVisible +
                ", useOverlays=" + useOverlays +
                ", hasOverlayPermissions=" + hasOverlayPermissions +
                ", useChatHeads=" + useChatHeads +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatHeadState that = (ChatHeadState) o;
        return messageCount == that.messageCount &&
                areIntegratedViewsVisible == that.areIntegratedViewsVisible &&
                isOverlayVisible == that.isOverlayVisible &&
                useOverlays == that.useOverlays &&
                hasOverlayPermissions == that.hasOverlayPermissions &&
                Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl) &&
                Objects.equals(theme, that.theme) &&
                Objects.equals(operatorMediaState, that.operatorMediaState) &&
                Objects.equals(returnDestination, that.returnDestination) &&
                Objects.equals(useChatHeads, that.useChatHeads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageCount, operatorProfileImgUrl, theme, operatorMediaState, returnDestination, areIntegratedViewsVisible, isOverlayVisible, useOverlays, hasOverlayPermissions, useChatHeads);
    }
}
