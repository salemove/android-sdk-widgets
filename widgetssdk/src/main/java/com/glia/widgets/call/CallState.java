package com.glia.widgets.call;

import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.comms.VisitorMediaState;

import java.util.Objects;

class CallState {

    public final boolean integratorCallStarted;
    public final boolean isVisible;
    public final int messagesNotSeen;
    public final boolean hasOverlayPermissions;
    public final CallStatus callStatus;
    public final boolean landscapeLayoutControlsVisible;

    private CallState(boolean integratorCallStarted,
                      boolean isVisible,
                      int messagesNotSeen,
                      boolean hasOverlayPermissions,
                      CallStatus callStatus,
                      boolean landscapeLayoutControlsVisible) {
        this.integratorCallStarted = integratorCallStarted;
        this.isVisible = isVisible;
        this.messagesNotSeen = messagesNotSeen;
        this.hasOverlayPermissions = hasOverlayPermissions;
        this.callStatus = callStatus;
        this.landscapeLayoutControlsVisible = landscapeLayoutControlsVisible;
    }

    public boolean isCallOngoing() {
        return callStatus instanceof CallStatus.Ongoing || isAudioCall() || isVideoCall();
    }

    public boolean isVideoCall() {
        return callStatus instanceof CallStatus.StartedVideoCall;
    }

    public boolean is2WayVideoCall() {
        return callStatus instanceof CallStatus.StartedVideoCall &&
                ((CallStatus.StartedVideoCall) callStatus).getVisitorMediaState() != null &&
                ((CallStatus.StartedVideoCall) callStatus).getVisitorMediaState().getVideo() != null;
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

    public CallState engagementStarted(String operatorName, String formatedTimeValue) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(new CallStatus.Ongoing(operatorName, formatedTimeValue))
                .createCallState();
    }

    public CallState changeVisibility(boolean isVisible) {
        return new Builder()
                .copyFrom(this)
                .setVisible(isVisible)
                .createCallState();
    }

    public CallState drawOverlaysPermissionChanged(boolean hasOverlayPermissions) {
        return new Builder()
                .copyFrom(this)
                .setHasOverlayPermissions(hasOverlayPermissions)
                .createCallState();
    }

    public CallState videoCallOperatorVideoStarted(OperatorMediaState operatorMediaState) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.StartedVideoCall(
                                callStatus.getOperatorName(),
                                callStatus.getTime(),
                                operatorMediaState,
                                callStatus instanceof CallStatus.StartedVideoCall ?
                                        ((CallStatus.StartedVideoCall) callStatus).getVisitorMediaState() :
                                        null)
                )
                .setLandscapeLayoutControlsVisible(false)
                .createCallState();
    }

    public CallState videoCallVisitorVideoStarted(VisitorMediaState visitorMediaState) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.StartedVideoCall(
                                callStatus.getOperatorName(),
                                callStatus.getTime(),
                                callStatus.getOperatorMediaState(),
                                visitorMediaState)
                )
                .setLandscapeLayoutControlsVisible(false)
                .createCallState();
    }

    public CallState audioCallStarted(OperatorMediaState operatorMediaState) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(
                        new CallStatus.StartedAudioCall(
                                callStatus.getOperatorName(),
                                callStatus.getTime(),
                                operatorMediaState)
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
                                    callStatus.getOperatorMediaState()
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

    @Override
    public String toString() {
        return "CallState{" +
                "integratorCallStarted=" + integratorCallStarted +
                ", isVisible=" + isVisible +
                ", messagesNotSeen=" + messagesNotSeen +
                ", hasOverlayPermissions=" + hasOverlayPermissions +
                ", callStatus=" + callStatus +
                ", landscapeLayoutControlsVisible=" + landscapeLayoutControlsVisible +
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
                hasOverlayPermissions == callState.hasOverlayPermissions &&
                landscapeLayoutControlsVisible == callState.landscapeLayoutControlsVisible &&
                Objects.equals(callStatus, callState.callStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(integratorCallStarted, isVisible, messagesNotSeen, hasOverlayPermissions, callStatus, landscapeLayoutControlsVisible);
    }

    public static class Builder {
        private boolean integratorCallStarted;
        private boolean isVisible;
        private int messagesNotSeen;
        private boolean hasOverlayPermissions;
        private CallStatus callStatus;
        private boolean landscapeLayoutControlsVisible;

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

        public Builder setHasOverlayPermissions(boolean hasOverlayPermissions) {
            this.hasOverlayPermissions = hasOverlayPermissions;
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

        public Builder copyFrom(CallState callState) {
            integratorCallStarted = callState.integratorCallStarted;
            isVisible = callState.isVisible;
            messagesNotSeen = callState.messagesNotSeen;
            hasOverlayPermissions = callState.hasOverlayPermissions;
            callStatus = callState.callStatus;
            landscapeLayoutControlsVisible = callState.landscapeLayoutControlsVisible;
            return this;
        }

        public CallState createCallState() {
            return new CallState(
                    integratorCallStarted,
                    isVisible,
                    messagesNotSeen,
                    hasOverlayPermissions,
                    callStatus,
                    landscapeLayoutControlsVisible);
        }
    }
}
