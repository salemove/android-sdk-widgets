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
    public final String queueTicketId;
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
                ", queueTicketId: " + queueTicketId +
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
                Objects.equals(queueTicketId, callState.queueTicketId) &&
                Objects.equals(companyName, callState.companyName) &&
                Objects.equals(requestedMediaType, callState.requestedMediaType) &&
                isSpeakerOn == callState.isSpeakerOn &&
                isOnHold == callState.isOnHold;
    }

    @Override
    public int hashCode() {
        return Objects.hash(integratorCallStarted, isVisible, messagesNotSeen,
                callStatus, landscapeLayoutControlsVisible, isMuted, hasVideo, queueTicketId,
                companyName, requestedMediaType, isSpeakerOn, isOnHold);
    }

    public boolean showOperatorStatusView() {
        return isCallNotOngoing() || isCallOngoingAndOperatorIsConnecting() || isAudioCall() || isOnHold;
    }

    public boolean isMediaEngagementStarted() {
        return isCallOngoingAndOperatorIsConnecting() || isCallOngoingAndOperatorConnected();
    }

    public boolean isCallNotOngoing() {
        return callStatus instanceof CallStatus.NotOngoing;
    }

    public boolean isCallOngoingAndOperatorIsConnecting() {
        return callStatus instanceof CallStatus.OngoingNoOperator;
    }

    public boolean isCallOngoingAndOperatorConnected() {
        return isAudioCall() || isVideoCall();
    }

    public boolean isVideoCall() {
        return callStatus instanceof CallStatus.StartedVideoCall;
    }

    public boolean is2WayVideoCall() {
        return callStatus instanceof CallStatus.StartedVideoCall && isVisitorVideoAvailable();
    }

    public boolean is2WayVideoCallAndVisitorVideoIsConnected() {
        if (is2WayVideoCall()) {
            Video visitorVideo = callStatus.getVisitorMediaState().getVideo();
            return visitorVideo.getStatus() == Media.Status.PLAYING || visitorVideo.getStatus() == Media.Status.PAUSED;
        } else {
            return false;
        }
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
        return callStatus instanceof CallStatus.StartedAudioCall;
    }

    public CallState initCall(String companyName, Engagement.MediaType requestedMediaType) {
        return new Builder()
                .copyFrom(this)
                .setIntegratorCallStarted(true)
                .setVisible(true)
                .setCompanyName(companyName)
                .setRequestedMediaType(requestedMediaType)
                .createCallState();
    }

    public CallState ticketLoaded(String queueTicketId) {
        return new Builder()
                .copyFrom(this)
                .setQueueTicketId(queueTicketId)
                .createCallState();
    }

    public CallState stop() {
        return new Builder()
                .copyFrom(this)
                .setIntegratorCallStarted(false)
                .setVisible(false)
                .setCallStatus(new CallStatus.NotOngoing(callStatus.getVisitorMediaState()))
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

    public CallState engagementStarted(
            String operatorName,
            String operatorProfileImgUrl
    ) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.OngoingNoOperator(
                                operatorName,
                                "0",
                                operatorProfileImgUrl,
                                callStatus.getVisitorMediaState())
                )
                .setQueueTicketId(null)
                .createCallState();
    }

    public CallState backToOngoing() {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.OngoingNoOperator(
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
                        new CallStatus.StartedVideoCall(
                                callStatus.getOperatorName(),
                                formattedTime,
                                callStatus.getOperatorProfileImageUrl(),
                                operatorMediaState,
                                callStatus.getVisitorMediaState()))
                .setLandscapeLayoutControlsVisible(false)
                .createCallState();
    }

    public CallState visitorMediaStateChanged(VisitorMediaState visitorMediaState) {
        callStatus.setVisitorMediaState(visitorMediaState);
        return new Builder()
                .copyFrom(this)
                .setHasVideo(isVisitorVideoPlaying(visitorMediaState))
                .setIsMuted(false)
                .createCallState();
    }

    public CallState audioCallStarted(OperatorMediaState operatorMediaState, String formattedTime) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.StartedAudioCall(
                                callStatus.getOperatorName(),
                                formattedTime,
                                callStatus.getOperatorProfileImageUrl(),
                                operatorMediaState,
                                callStatus.getVisitorMediaState())
                )
                .setLandscapeLayoutControlsVisible(true)
                .createCallState();
    }

    public CallState newStartedCallTimerValue(String formatedTimeValue) {
        if (isAudioCall()) {
            return new Builder()
                    .copyFrom(this)
                    .setCallStatus(
                            new CallStatus.StartedAudioCall(
                                    callStatus.getOperatorName(),
                                    formatedTimeValue,
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
                            new CallStatus.StartedVideoCall(
                                    callStatus.getOperatorName(),
                                    formatedTimeValue,
                                    callStatus.getOperatorProfileImageUrl(),
                                    callStatus.getOperatorMediaState(),
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
                            new CallStatus.OngoingNoOperator(
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

    public CallState muteStatusChanged(boolean isMuted) {
        return new Builder()
                .copyFrom(this)
                .setIsMuted(isMuted)
                .createCallState();
    }

    public CallState hasVideoChanged(boolean hasVideo) {
        return new Builder()
                .copyFrom(this)
                .setHasVideo(hasVideo)
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

    public boolean isMuteButtonEnabled() {
        return (isAudioCall() || is2WayVideoCall()) && !isOnHold;
    }

    public boolean isVideoButtonEnabled() {
        return is2WayVideoCall() && !isOnHold;
    }

    public boolean isSpeakerButtonEnabled() {
        return isAudioCall() || is2WayVideoCall();
    }

    public boolean isShowCallTimerView() {
        return isCallOngoingAndOperatorConnected() && !isOnHold;
    }

    public boolean showOperatorVideo() {
        return isVideoCallAndOperatorVideoIsConnected() && !isOnHold;
    }

    public boolean showContinueBrowsingView() {
        return isCallOngoingAndOperatorIsConnecting() || isCallNotOngoing() || isOnHold;
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

        public Builder setQueueTicketId(String queueTicketId) {
            this.queueTicketId = queueTicketId;
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
            queueTicketId = callState.queueTicketId;
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
        this.queueTicketId = builder.queueTicketId;
        this.companyName = builder.companyName;
        this.requestedMediaType = builder.requestedMediaType;
        this.isSpeakerOn = builder.isSpeakerOn;
        this.isOnHold = builder.isOnHold;
    }
}
