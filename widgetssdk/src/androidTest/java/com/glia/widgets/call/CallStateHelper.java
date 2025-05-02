package com.glia.widgets.call;

import com.glia.widgets.engagement.MediaType;

public class CallStateHelper {
    public final boolean integratorCallStarted;
    public final boolean isVisible;
    public final int messagesNotSeen;
    public final CallStatus callStatus;
    public final boolean landscapeLayoutControlsVisible;
    public final boolean isMuted;
    public final boolean hasVideo;
    public final String companyName;
    public final MediaType requestedMediaType;
    public final boolean isSpeakerOn;

    public CallStateHelper(boolean integratorCallStarted,
                           boolean isVisible,
                           int messagesNotSeen,
                           CallStatus callStatus,
                           boolean landscapeLayoutControlsVisible,
                           boolean isMuted,
                           boolean hasVideo,
                           String queueTicketId,
                           String companyName,
                           MediaType requestedMediaType,
                           boolean isSpeakerOn) {
        this.integratorCallStarted = integratorCallStarted;
        this.isVisible = isVisible;
        this.messagesNotSeen = messagesNotSeen;
        this.callStatus = callStatus;
        this.landscapeLayoutControlsVisible = landscapeLayoutControlsVisible;
        this.isMuted = isMuted;
        this.hasVideo = hasVideo;
        this.companyName = companyName;
        this.requestedMediaType = requestedMediaType;
        this.isSpeakerOn = isSpeakerOn;
    }

    public static class Builder {
        private boolean integratorCallStarted;
        private boolean isVisible;
        private int messagesNotSeen;
        private CallStatus callStatus;
        private boolean landscapeLayoutControlsVisible;
        private boolean isMuted;
        private boolean hasVideo;
        private String queueTicketId;
        private String companyName;
        private MediaType requestedMediaType;
        private boolean isSpeakerOn;

        public CallStateHelper.Builder setIntegratorCallStarted(boolean integratorCallStarted) {
            this.integratorCallStarted = integratorCallStarted;
            return this;
        }

        public CallStateHelper.Builder setVisible(boolean visible) {
            isVisible = visible;
            return this;
        }

        public CallStateHelper.Builder setMessagesNotSeen(int messagesNotSeen) {
            this.messagesNotSeen = messagesNotSeen;
            return this;
        }

        public CallStateHelper.Builder setCallStatus(CallStatus callStatus) {
            this.callStatus = callStatus;
            return this;
        }

        public CallStateHelper.Builder setLandscapeLayoutControlsVisible(boolean visible) {
            this.landscapeLayoutControlsVisible = visible;
            return this;
        }

        public CallStateHelper.Builder setIsMuted(boolean isMuted) {
            this.isMuted = isMuted;
            return this;
        }

        public CallStateHelper.Builder setHasVideo(boolean hasVideo) {
            this.hasVideo = hasVideo;
            return this;
        }

        public CallStateHelper.Builder setQueueTicketId(String queueTicketId) {
            this.queueTicketId = queueTicketId;
            return this;
        }

        public CallStateHelper.Builder setCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public CallStateHelper.Builder setRequestedMediaType(MediaType requestedMediaType) {
            this.requestedMediaType = requestedMediaType;
            return this;
        }

        public CallStateHelper.Builder setIsSpeakerOn(boolean isSpeakerOn) {
            this.isSpeakerOn = isSpeakerOn;
            return this;
        }

        public CallStateHelper.Builder copyFrom(CallState callState) {
            integratorCallStarted = callState.integratorCallStarted;
            isVisible = callState.isVisible;
            messagesNotSeen = callState.messagesNotSeen;
            callStatus = callState.callStatus;
            landscapeLayoutControlsVisible = callState.landscapeLayoutControlsVisible;
            isMuted = callState.isMuted;
            hasVideo = callState.hasVideo;
            requestedMediaType = callState.requestedMediaType;
            isSpeakerOn = callState.isSpeakerOn;
            return this;
        }

        public CallStateHelper.Builder copyFrom(CallStateHelper callState) {
            integratorCallStarted = callState.integratorCallStarted;
            isVisible = callState.isVisible;
            messagesNotSeen = callState.messagesNotSeen;
            callStatus = callState.callStatus;
            landscapeLayoutControlsVisible = callState.landscapeLayoutControlsVisible;
            isMuted = callState.isMuted;
            hasVideo = callState.hasVideo;
            companyName = callState.companyName;
            requestedMediaType = callState.requestedMediaType;
            isSpeakerOn = callState.isSpeakerOn;
            return this;
        }

        public CallStateHelper build() {
            return new CallStateHelper(
                    integratorCallStarted,
                    isVisible,
                    messagesNotSeen,
                    callStatus,
                    landscapeLayoutControlsVisible,
                    isMuted,
                    hasVideo,
                    queueTicketId,
                    companyName,
                    requestedMediaType,
                    isSpeakerOn
            );
        }
    }

    public CallState makeCallState() {
        return new CallState.Builder()
                .setIntegratorCallStarted(integratorCallStarted)
                .setVisible(isVisible)
                .setMessagesNotSeen(messagesNotSeen)
                .setCallStatus(callStatus)
                .setLandscapeLayoutControlsVisible(landscapeLayoutControlsVisible)
                .setIsMuted(isMuted)
                .setHasVideo(hasVideo)
                .setRequestedMediaType(requestedMediaType)
                .setIsSpeakerOn(isSpeakerOn)
                .createCallState();
    }
}
