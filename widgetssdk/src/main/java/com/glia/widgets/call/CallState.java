package com.glia.widgets.call;

import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.OperatorMediaState;
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

    private CallState(boolean integratorCallStarted,
                      boolean isVisible,
                      int messagesNotSeen,
                      CallStatus callStatus,
                      boolean landscapeLayoutControlsVisible,
                      boolean isMuted,
                      boolean hasVideo) {
        this.integratorCallStarted = integratorCallStarted;
        this.isVisible = isVisible;
        this.messagesNotSeen = messagesNotSeen;
        this.callStatus = callStatus;
        this.landscapeLayoutControlsVisible = landscapeLayoutControlsVisible;
        this.isMuted = isMuted;
        this.hasVideo = hasVideo;
    }

    public boolean showOperatorStatusView() {
        return isCallNotOngoing() || isCallOngoig() || isAudioCall();
    }

    public boolean showRippleAnimation() {
        return isCallNotOngoing() || isCallOngoig();
    }

    public boolean isMediaEngagementStarted() {
        return isCallOngoig() || hasMedia();
    }

    public boolean isCallNotOngoing() {
        return callStatus instanceof CallStatus.NotOngoing;
    }

    public boolean isCallOngoig() {
        return callStatus instanceof CallStatus.Ongoing;
    }

    public boolean hasMedia() {
        return isAudioCall() || isVideoCall();
    }

    public boolean isVideoCall() {
        return callStatus instanceof CallStatus.StartedVideoCall;
    }

    public boolean is2WayVideoCall() {
        return callStatus instanceof CallStatus.StartedVideoCall &&
                callStatus.getVisitorMediaState() != null &&
                callStatus.getVisitorMediaState().getVideo() != null;
    }

    public boolean isAudioCall() {
        return callStatus instanceof CallStatus.StartedAudioCall;
    }

    public CallState initCall() {
        return new Builder()
                .copyFrom(this)
                .setIntegratorCallStarted(true)
                .setVisible(true)
                .createCallState();
    }

    public CallState stop() {
        return new Builder()
                .copyFrom(this)
                .setIntegratorCallStarted(false)
                .setCallStatus(new CallStatus.NotOngoing())
                .createCallState();
    }

    public CallState changeNumberOfMessages(int numberOfMessages) {
        return new Builder()
                .copyFrom(this)
                .setMessagesNotSeen(numberOfMessages)
                .createCallState();
    }

    public CallState engagementStarted(
            String operatorName,
            String operatorProfileImgUrl,
            String formatedTimeValue) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.Ongoing(
                                operatorName,
                                formatedTimeValue,
                                operatorProfileImgUrl)
                )
                .createCallState();
    }

    public CallState backToOngoing() {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.Ongoing(
                                callStatus.getOperatorName(),
                                callStatus.getTime(),
                                callStatus.getOperatorProfileImageUrl()
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

    public CallState videoCallOperatorVideoStarted(OperatorMediaState operatorMediaState) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.StartedVideoCall(
                                callStatus.getOperatorName(),
                                callStatus.getTime(),
                                callStatus.getOperatorProfileImageUrl(),
                                operatorMediaState,
                                callStatus instanceof CallStatus.StartedVideoCall ?
                                        ((CallStatus.StartedVideoCall) callStatus).getVisitorMediaState() :
                                        null)
                )
                .setLandscapeLayoutControlsVisible(false)
                .createCallState();
    }

    public CallState visitorMediaStateChanged(VisitorMediaState visitorMediaState) {
        callStatus.setVisitorMediaState(visitorMediaState);
        return new Builder()
                .copyFrom(this)
                .setHasVideo(
                        visitorMediaState.getVideo() != null &&
                                visitorMediaState.getVideo().getStatus() == Media.Status.PLAYING
                )
                .setIsMuted(false)
                .createCallState();
    }

    public CallState audioCallStarted(OperatorMediaState operatorMediaState) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.StartedAudioCall(
                                callStatus.getOperatorName(),
                                callStatus.getTime(),
                                callStatus.getOperatorProfileImageUrl(),
                                operatorMediaState,
                                callStatus.getVisitorMediaState())
                )
                .setLandscapeLayoutControlsVisible(true)
                .createCallState();
    }

    public CallState newTimerValue(String formatedTimeValue) {
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
                                    ((CallStatus.StartedVideoCall) callStatus).getVisitorMediaState()
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
                Objects.equals(callStatus, callState.callStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(integratorCallStarted, isVisible, messagesNotSeen,
                callStatus, landscapeLayoutControlsVisible, isMuted, hasVideo);
    }

    public static class Builder {
        private boolean integratorCallStarted;
        private boolean isVisible;
        private int messagesNotSeen;
        private CallStatus callStatus;
        private boolean landscapeLayoutControlsVisible;
        private boolean isMuted;
        private boolean hasVideo;

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

        public Builder copyFrom(CallState callState) {
            integratorCallStarted = callState.integratorCallStarted;
            isVisible = callState.isVisible;
            messagesNotSeen = callState.messagesNotSeen;
            callStatus = callState.callStatus;
            landscapeLayoutControlsVisible = callState.landscapeLayoutControlsVisible;
            isMuted = callState.isMuted;
            hasVideo = callState.hasVideo;
            return this;
        }

        public CallState createCallState() {
            return new CallState(
                    integratorCallStarted,
                    isVisible,
                    messagesNotSeen,
                    callStatus,
                    landscapeLayoutControlsVisible,
                    isMuted,
                    hasVideo);
        }
    }
}
