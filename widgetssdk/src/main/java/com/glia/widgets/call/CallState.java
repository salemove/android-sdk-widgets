package com.glia.widgets.call;

import com.glia.widgets.helper.Utils;

import java.util.Objects;

class CallState {

    public final boolean integratorCallStarted;
    public final boolean isVisible;
    public final String operatorName;
    public final int messagesNotSeen;
    public final boolean hasOverlayPermissions;

    private CallState(boolean integratorCallStarted,
                      boolean isVisible,
                      String operatorName,
                      int messagesNotSeen,
                      boolean hasOverlayPermissions) {
        this.integratorCallStarted = integratorCallStarted;
        this.isVisible = isVisible;
        this.operatorName = operatorName;
        this.messagesNotSeen = messagesNotSeen;
        this.hasOverlayPermissions = hasOverlayPermissions;
    }

    public boolean isOperatorOnline() {
        return operatorName != null;
    }

    public String getFormattedOperatorName() {
        return Utils.formatOperatorName(operatorName);
    }

    public CallState stop() {
        return new Builder()
                .copyFrom(this)
                .setOperatorName(null)
                .createCallState();
    }

    public CallState newMessage() {
        return new Builder()
                .copyFrom(this)
                .setMessagesNotSeen(this.messagesNotSeen + 1)
                .createCallState();
    }

    public CallState engagementStarted(String operatorName) {
        return new Builder()
                .copyFrom(this)
                .setOperatorName(operatorName)
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

    @Override
    public String toString() {
        return "CallState{" +
                "integratorCallStarted=" + integratorCallStarted +
                ", isVisible=" + isVisible +
                ", operatorName='" + operatorName + '\'' +
                ", messagesNotSeen=" + messagesNotSeen +
                ", hasOverlayPermissions=" + hasOverlayPermissions +
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
                Objects.equals(operatorName, callState.operatorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(integratorCallStarted, isVisible, operatorName, messagesNotSeen, hasOverlayPermissions);
    }

    public static class Builder {
        private boolean integratorCallStarted;
        private boolean isVisible;
        private String operatorName;
        private int messagesNotSeen;
        private boolean hasOverlayPermissions;

        public Builder setIntegratorCallStarted(boolean integratorCallStarted) {
            this.integratorCallStarted = integratorCallStarted;
            return this;
        }

        public Builder setVisible(boolean visible) {
            isVisible = visible;
            return this;
        }

        public Builder setOperatorName(String operatorName) {
            this.operatorName = operatorName;
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

        public Builder copyFrom(CallState callState) {
            integratorCallStarted = callState.integratorCallStarted;
            isVisible = callState.isVisible;
            operatorName = callState.operatorName;
            messagesNotSeen = callState.messagesNotSeen;
            hasOverlayPermissions = callState.hasOverlayPermissions;
            return this;
        }

        public CallState createCallState() {
            return new CallState(
                    integratorCallStarted,
                    isVisible,
                    operatorName,
                    messagesNotSeen,
                    hasOverlayPermissions);
        }
    }
}
