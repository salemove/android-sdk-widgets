package com.glia.widgets.call;

import androidx.annotation.NonNull;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.comms.Video;
import com.glia.androidsdk.comms.VisitorMediaState;

import java.util.Objects;

class CallState {
    public final boolean integratorCallStarted;
    public final boolean isVisible;
    public final int messagesNotSeen;
    public final CallStatus callStatus;
    public final boolean landscapeLayoutControlsVisible;
    public final boolean isMuted;
    public final boolean hasVideo;
    public final String companyName;
    public final Engagement.MediaType requestedMediaType;
    public final boolean isSpeakerOn;
    public final boolean isOnHold;

    @NonNull
    @Override
    public String toString() {
        return "CallState{" +
                "integratorCallStarted=" + integratorCallStarted +
                ", isVisible=" + isVisible +
                ", messagesNotSeen=" + messagesNotSeen +
                ", callStatus=" + callStatus +
                ", landscapeLayoutControlsVisible=" + landscapeLayoutControlsVisible +
                ", isMuted=" + isMuted +
                ", hasVideo=" + hasVideo +
                ", companyName: " + companyName +
                ", requestedMediaType: " + requestedMediaType +
                ", isSpeakerOn: " + isSpeakerOn +
                ", isOnHold: " + isOnHold +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallState callState = (CallState) o;
        return integratorCallStarted == callState.integratorCallStarted &&
                isVisible == callState.isVisible &&
                messagesNotSeen == callState.messagesNotSeen &&
                landscapeLayoutControlsVisible == callState.landscapeLayoutControlsVisible &&
                isMuted == callState.isMuted &&
                hasVideo == callState.hasVideo &&
                Objects.equals(callStatus, callState.callStatus) &&
                Objects.equals(companyName, callState.companyName) &&
                Objects.equals(requestedMediaType, callState.requestedMediaType) &&
                isSpeakerOn == callState.isSpeakerOn &&
                isOnHold == callState.isOnHold;
    }

    @Override
    public int hashCode() {
        return Objects.hash(integratorCallStarted, isVisible, messagesNotSeen,
                callStatus, landscapeLayoutControlsVisible, isMuted, hasVideo,
                companyName, requestedMediaType, isSpeakerOn, isOnHold);
    }

    public boolean showOperatorStatusView() {
        return isCallNotOngoing() || isCallOngoingAndOperatorIsConnecting() || isAudioCall() || showOnHold() || isTransferring();
    }

    public boolean isMediaEngagementStarted() {
        return isCallOngoingAndOperatorIsConnecting() || isCallOngoingAndOperatorConnected() || isTransferring();
    }

    public boolean isCallNotOngoing() {
        return callStatus instanceof CallStatus.EngagementNotOngoing;
    }

    public boolean isCallOngoingAndOperatorIsConnecting() {
        return callStatus instanceof CallStatus.EngagementOngoingOperatorIsConnecting;
    }

    public boolean isCallOngoingAndOperatorConnected() {
        return isAudioCall() || isVideoCall();
    }

    public boolean isTransferring() {
        return callStatus instanceof CallStatus.EngagementOngoingTransferring;
    }

    public boolean isVideoCall() {
        return callStatus instanceof CallStatus.EngagementOngoingVideoCallStarted;
    }

    public boolean is2WayVideoCall() {
        return callStatus instanceof CallStatus.EngagementOngoingVideoCallStarted && isVisitorVideoAvailable();
    }

    public boolean isVideoCallAndOperatorVideoIsConnected() {
        if (isVideoCall() && isOperatorVideoAvailable()) {
            Video operatorVideo = callStatus.getOperatorMediaState().getVideo();
            return operatorVideo.getStatus() == Media.Status.PLAYING || operatorVideo.getStatus() == Media.Status.PAUSED;
        } else {
            return false;
        }
    }

    public boolean isAudioCall() {
        return callStatus instanceof CallStatus.EngagementOngoingAudioCallStarted;
    }

    public CallState initCall(String companyName, Engagement.MediaType requestedMediaType) {
        return new Builder()
                .copyFrom(this)
                .setIntegratorCallStarted(true)
                .setVisible(true)
                .setIsOnHold(false)
                .setCompanyName(companyName)
                .setRequestedMediaType(requestedMediaType)
                .createCallState();
    }

    public CallState stop() {
        return new Builder()
                .copyFrom(this)
                .setIntegratorCallStarted(false)
                .setVisible(false)
                .setIsOnHold(false)
                .setCallStatus(new CallStatus.EngagementNotOngoing(callStatus.getVisitorMediaState()))
                .createCallState();
    }

    public CallState changeNumberOfMessages(int numberOfMessages) {
        return new Builder()
                .copyFrom(this)
                .setMessagesNotSeen(numberOfMessages)
                .createCallState();
    }

    public CallState changeRequestedMediaType(Engagement.MediaType requestedMediaType) {
        return new Builder()
                .copyFrom(this)
                .setRequestedMediaType(requestedMediaType)
                .createCallState();
    }

    public CallState operatorConnecting(String name, String url) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.EngagementOngoingOperatorIsConnecting(
                                name,
                                "0",
                                url,
                                callStatus.getVisitorMediaState()
                        )
                ).createCallState();
    }

    public CallState engagementStarted() {
        return new Builder()
                .copyFrom(this)
                .setIsOnHold(false)
                .createCallState();
    }

    public CallState backToOngoing() {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.EngagementOngoingOperatorIsConnecting(
                                callStatus.getOperatorName(),
                                "0",
                                callStatus.getOperatorProfileImageUrl(),
                                callStatus.getVisitorMediaState()
                        )
                )
                .createCallState();
    }

    public CallState changeVisibility(boolean isVisible) {
        return new Builder()
                .copyFrom(this)
                .setVisible(isVisible)
                .createCallState();
    }

    public CallState videoCallOperatorVideoStarted(
            OperatorMediaState operatorMediaState,
            String formattedTime
    ) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.EngagementOngoingVideoCallStarted(
                                callStatus.getOperatorName(),
                                formattedTime,
                                callStatus.getOperatorProfileImageUrl(),
                                operatorMediaState,
                                callStatus.getVisitorMediaState()))
                .setLandscapeLayoutControlsVisible(true)
                .createCallState();
    }

    public CallState visitorMediaStateChanged(VisitorMediaState visitorMediaState) {
        callStatus.setVisitorMediaState(visitorMediaState);
        return new Builder()
                .copyFrom(this)
                .setHasVideo(isVisitorVideoPlaying(visitorMediaState))
                .setIsMuted(isMuted(visitorMediaState))
                .createCallState();
    }

    public CallState audioCallStarted(OperatorMediaState operatorMediaState, String formattedTime) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.EngagementOngoingAudioCallStarted(
                                callStatus.getOperatorName(),
                                formattedTime,
                                callStatus.getOperatorProfileImageUrl(),
                                operatorMediaState,
                                callStatus.getVisitorMediaState())
                )
                .setLandscapeLayoutControlsVisible(true)
                .createCallState();
    }

    public CallState newStartedCallTimerValue(String formattedTimeValue) {
        if (isAudioCall()) {
            return new Builder()
                    .copyFrom(this)
                    .setCallStatus(
                            new CallStatus.EngagementOngoingAudioCallStarted(
                                    callStatus.getOperatorName(),
                                    formattedTimeValue,
                                    callStatus.getOperatorProfileImageUrl(),
                                    callStatus.getOperatorMediaState(),
                                    callStatus.getVisitorMediaState()
                            )
                    )
                    .createCallState();
        } else if (isVideoCall()) {
            return new Builder()
                    .copyFrom(this)
                    .setCallStatus(
                            new CallStatus.EngagementOngoingVideoCallStarted(
                                    callStatus.getOperatorName(),
                                    formattedTimeValue,
                                    callStatus.getOperatorProfileImageUrl(),
                                    callStatus.getOperatorMediaState(),
                                    callStatus.getVisitorMediaState()
                            )
                    )
                    .createCallState();
        } else if (isTransferring()) {
            return new Builder()
                    .copyFrom(this)
                    .setCallStatus(
                            new CallStatus.EngagementOngoingTransferring(
                                    formattedTimeValue,
                                    callStatus.getVisitorMediaState()
                            )
                    )
                    .createCallState();
        } else {
            return this;
        }
    }

    public CallState connectingTimerValueChanged(String timeValue) {
        if (isCallOngoingAndOperatorIsConnecting()) {
            return new Builder()
                    .copyFrom(this)
                    .setCallStatus(
                            new CallStatus.EngagementOngoingOperatorIsConnecting(
                                    callStatus.getOperatorName(),
                                    timeValue,
                                    callStatus.getOperatorProfileImageUrl(),
                                    callStatus.getVisitorMediaState()
                            )
                    )
                    .createCallState();
        } else {
            return this;
        }
    }

    public CallState landscapeControlsVisibleChanged(boolean visible) {
        return new Builder()
                .copyFrom(this)
                .setLandscapeLayoutControlsVisible(visible)
                .createCallState();
    }

    public CallState speakerValueChanged(boolean isSpeakerOn) {
        return new Builder()
                .copyFrom(this)
                .setIsSpeakerOn(isSpeakerOn)
                .createCallState();
    }

    public CallState setOnHold(boolean isOnHold) {
        return new Builder()
                .copyFrom(this)
                .setIsOnHold(isOnHold)
                .createCallState();
    }

    public CallState setTransferring() {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.EngagementOngoingTransferring(
                                callStatus.getTime(),
                                callStatus.getVisitorMediaState()
                        )
                )
                .createCallState();
    }

    public boolean isMuteButtonEnabled() {
        return (isAudioCall() || is2WayVideoCall()) && !showOnHold();
    }

    public boolean isVideoButtonEnabled() {
        return is2WayVideoCall() && !showOnHold();
    }

    public boolean isSpeakerButtonEnabled() {
        return isAudioCall() || is2WayVideoCall();
    }

    public boolean showCallTimerView() {
        return isCallOngoingAndOperatorConnected() && !showOnHold() || isTransferring();
    }

    public boolean showOperatorVideo() {
        return isVideoCallAndOperatorVideoIsConnected() && !showOnHold();
    }

    public boolean showContinueBrowsingView() {
        return isCallOngoingAndOperatorIsConnecting() || isCallNotOngoing() || showOnHold();
    }

    public boolean showOperatorNameView() {
        return isCallOngoingAndOperatorConnected() || isTransferring();
    }

    public boolean showCompanyNameView() {
        return !isMediaEngagementStarted();
    }

    public boolean showOperatorStatusViewRippleAnimation() {
        return isCallNotOngoing() || isCallOngoingAndOperatorIsConnecting() || isTransferring();
    }

    public boolean showOnHold() {
        return isOnHold && !isTransferring() && !isCallOngoingAndOperatorIsConnecting();
    }

    private boolean isVisitorVideoAvailable() {
        return callStatus.getVisitorMediaState() != null &&
                callStatus.getVisitorMediaState().getVideo() != null;
    }

    private boolean isOperatorVideoAvailable() {
        return callStatus.getOperatorMediaState() != null &&
                callStatus.getOperatorMediaState().getVideo() != null;
    }

    private boolean isVisitorVideoPlaying(VisitorMediaState visitorMediaState) {
        return visitorMediaState != null &&
                visitorMediaState.getVideo() != null &&
                visitorMediaState.getVideo().getStatus() == Media.Status.PLAYING;
    }

    private boolean isMuted(VisitorMediaState visitorMediaState) {
        return visitorMediaState == null ||
                visitorMediaState.getAudio() == null ||
                visitorMediaState.getAudio().getStatus() != Media.Status.PLAYING;
    }

    public static class Builder {
        private boolean integratorCallStarted;
        private boolean isVisible;
        private int messagesNotSeen;
        private CallStatus callStatus;
        private boolean landscapeLayoutControlsVisible;
        private boolean isMuted;
        private boolean hasVideo;
        private String companyName;
        private Engagement.MediaType requestedMediaType;
        private boolean isSpeakerOn;
        private boolean isOnHold;

        public Builder setIntegratorCallStarted(boolean integratorCallStarted) {
            this.integratorCallStarted = integratorCallStarted;
            return this;
        }

        public Builder setVisible(boolean visible) {
            isVisible = visible;
            return this;
        }

        public Builder setMessagesNotSeen(int messagesNotSeen) {
            this.messagesNotSeen = messagesNotSeen;
            return this;
        }

        public Builder setCallStatus(CallStatus callStatus) {
            this.callStatus = callStatus;
            return this;
        }

        public Builder setLandscapeLayoutControlsVisible(boolean visible) {
            this.landscapeLayoutControlsVisible = visible;
            return this;
        }

        public Builder setIsMuted(boolean isMuted) {
            this.isMuted = isMuted;
            return this;
        }

        public Builder setHasVideo(boolean hasVideo) {
            this.hasVideo = hasVideo;
            return this;
        }

        public Builder setCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder setRequestedMediaType(Engagement.MediaType requestedMediaType) {
            this.requestedMediaType = requestedMediaType;
            return this;
        }

        public Builder setIsSpeakerOn(boolean isSpeakerOn) {
            this.isSpeakerOn = isSpeakerOn;
            return this;
        }

        public Builder setIsOnHold(boolean isOnHold) {
            this.isOnHold = isOnHold;
            return this;
        }

        public Builder copyFrom(CallState callState) {
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
            isOnHold = callState.isOnHold;
            return this;
        }

        public CallState createCallState() {
            return new CallState(this);
        }
    }

    private CallState(Builder builder) {
        this.integratorCallStarted = builder.integratorCallStarted;
        this.isVisible = builder.isVisible;
        this.messagesNotSeen = builder.messagesNotSeen;
        this.callStatus = builder.callStatus;
        this.landscapeLayoutControlsVisible = builder.landscapeLayoutControlsVisible;
        this.isMuted = builder.isMuted;
        this.hasVideo = builder.hasVideo;
        this.companyName = builder.companyName;
        this.requestedMediaType = builder.requestedMediaType;
        this.isSpeakerOn = builder.isSpeakerOn;
        this.isOnHold = builder.isOnHold;
    }
}
