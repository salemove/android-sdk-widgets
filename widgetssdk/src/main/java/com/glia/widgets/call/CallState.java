package com.glia.widgets.call;

import com.glia.widgets.helper.Logger;

import java.util.Objects;

class CallState {

    public final boolean integratorCallStarted;
    public final boolean isVisible;
    public final int messagesNotSeen;
    public final boolean hasOverlayPermissions;
    public final CallStatus callStatus;

    private CallState(boolean integratorCallStarted,
                      boolean isVisible,
                      int messagesNotSeen,
                      boolean hasOverlayPermissions,
                      CallStatus callStatus) {
        this.integratorCallStarted = integratorCallStarted;
        this.isVisible = isVisible;
        this.messagesNotSeen = messagesNotSeen;
        this.hasOverlayPermissions = hasOverlayPermissions;
        this.callStatus = callStatus;
    }

    public boolean isCallOngoing() {
        Logger.d("isCallOngoing", Boolean.valueOf(callStatus instanceof CallStatus.StartedAudioCall).toString());
        return callStatus instanceof CallStatus.StartedAudioCall;
    }

    public CallState stop() {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(new CallStatus.NotOngoing())
                .createCallState();
    }

    public CallState newMessage() {
        return new Builder()
                .copyFrom(this)
                .setMessagesNotSeen(this.messagesNotSeen + 1)
                .createCallState();
    }

    public CallState engagementStarted(String operatorName, String formatedTimeValue) {
        return new Builder()
                .copyFrom(this)
                .setCallStatus(new CallStatus.StartedAudioCall(operatorName, formatedTimeValue))
                .createCallState();
    }

    public CallState hide() {
        return new Builder()
                .copyFrom(this)
                .setVisible(false)
                .createCallState();
    }

    public CallState initCall() {
        return new Builder()
                .copyFrom(this)
                .setVisible(true)
                .createCallState();
    }

    public CallState drawOverlaysPermissionChanged(boolean hasOverlayPermissions) {
        return new Builder()
                .copyFrom(this)
                .setHasOverlayPermissions(hasOverlayPermissions)
                .createCallState();
    }

    public CallState newTimerValue(String formatedTimeValue) {
        if (callStatus instanceof CallStatus.StartedAudioCall) {
            return new Builder()
                    .copyFrom(this)
                    .setCallStatus(
                            new CallStatus.StartedAudioCall(
                                    ((CallStatus.StartedAudioCall) callStatus).operatorName,
                                    formatedTimeValue
                            )
                    )
                    .createCallState();
        } else {
            return this;
        }
    }


    @Override
    public String toString() {
        return "CallState{" +
                "integratorCallStarted=" + integratorCallStarted +
                ", isVisible=" + isVisible +
                ", messagesNotSeen=" + messagesNotSeen +
                ", hasOverlayPermissions=" + hasOverlayPermissions +
                ", callStatus=" + callStatus +
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
                Objects.equals(callStatus, callState.callStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(integratorCallStarted, isVisible, messagesNotSeen, hasOverlayPermissions, callStatus);
    }

    public static class Builder {
        private boolean integratorCallStarted;
        private boolean isVisible;
        private int messagesNotSeen;
        private boolean hasOverlayPermissions;
        private CallStatus callStatus;

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

        public Builder copyFrom(CallState callState) {
            integratorCallStarted = callState.integratorCallStarted;
            isVisible = callState.isVisible;
            messagesNotSeen = callState.messagesNotSeen;
            hasOverlayPermissions = callState.hasOverlayPermissions;
            callStatus = callState.callStatus;
            return this;
        }

        public CallState createCallState() {
            return new CallState(
                    integratorCallStarted,
                    isVisible,
                    messagesNotSeen,
                    hasOverlayPermissions,
                    callStatus);
        }
    }
}
