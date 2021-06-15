package com.glia.widgets.call;

import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.widgets.helper.Utils;

import java.util.Objects;

public interface CallStatus {

    String getOperatorName();

    String getFormattedOperatorName();

    String getOperatorProfileImageUrl();

    /**
     * In case of {@link OngoingNoOperator} the time displays the time it takes to upgrade to
     * {@link StartedVideoCall} or {@link StartedAudioCall}.
     * In case of {@link StartedVideoCall} or {@link StartedAudioCall} the time is the ongoing
     * call duration
     *
     * @return A string value of the time. Either 0,1,2,3 in case of {@link OngoingNoOperator}
     * or MM:ss in case of {@link StartedAudioCall} or {@link StartedVideoCall}
     */
    String getTime();

    OperatorMediaState getOperatorMediaState();

    VisitorMediaState getVisitorMediaState();

    void setVisitorMediaState(VisitorMediaState visitorMediaState);

    class NotOngoing implements CallStatus {
        private VisitorMediaState visitorMediaState;

        public NotOngoing(VisitorMediaState visitorMediaState) {
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public String getOperatorName() {
            return null;
        }

        @Override
        public String getFormattedOperatorName() {
            return null;
        }

        @Override
        public String getOperatorProfileImageUrl() {
            return null;
        }

        @Override
        public String getTime() {
            return null;
        }

        @Override
        public OperatorMediaState getOperatorMediaState() {
            return null;
        }

        @Override
        public VisitorMediaState getVisitorMediaState() {
            return this.visitorMediaState;
        }

        @Override
        public void setVisitorMediaState(VisitorMediaState visitorMediaState) {
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NotOngoing that = (NotOngoing) o;
            return Objects.equals(visitorMediaState, that.visitorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(visitorMediaState);
        }

        @Override
        public String toString() {
            return "NotOngoing{" +
                    "visitorMediaState=" + visitorMediaState +
                    '}';
        }
    }

    class OngoingNoOperator implements CallStatus {
        private final String operatorName;
        private final String time;
        private final String operatorProfileImgUrl;
        private VisitorMediaState visitorMediaState;

        public OngoingNoOperator(String operatorName,
                                 String time,
                                 String operatorProfileImgUrl,
                                 VisitorMediaState visitorMediaState
        ) {
            this.operatorName = operatorName;
            this.time = time;
            this.operatorProfileImgUrl = operatorProfileImgUrl;
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public String getOperatorName() {
            return operatorName;
        }

        @Override
        public String getFormattedOperatorName() {
            return Utils.formatOperatorName(operatorName);
        }

        @Override
        public String getOperatorProfileImageUrl() {
            return operatorProfileImgUrl;
        }

        @Override
        public String getTime() {
            return time;
        }

        @Override
        public OperatorMediaState getOperatorMediaState() {
            return null;
        }

        @Override
        public VisitorMediaState getVisitorMediaState() {
            return null;
        }

        @Override
        public void setVisitorMediaState(VisitorMediaState visitorMediaState) {
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OngoingNoOperator that = (OngoingNoOperator) o;
            return Objects.equals(operatorName, that.operatorName) &&
                    Objects.equals(time, that.time) &&
                    Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl) &&
                    Objects.equals(visitorMediaState, that.visitorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operatorName, time, operatorProfileImgUrl, visitorMediaState);
        }

        @Override
        public String toString() {
            return "OngoingNoOperator{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                    ", visitorMediaState=" + visitorMediaState +
                    '}';
        }
    }

    class StartedAudioCall implements CallStatus {
        private final String operatorName;
        private final String time;
        private final String operatorProfileImgUrl;
        private final OperatorMediaState operatorMediaState;
        private VisitorMediaState visitorMediaState;

        public StartedAudioCall(
                String operatorName,
                String time,
                String operatorProfileImgUrl,
                OperatorMediaState operatorMediaState,
                VisitorMediaState visitorMediaState
        ) {
            this.operatorName = operatorName;
            this.time = time;
            this.operatorProfileImgUrl = operatorProfileImgUrl;
            this.operatorMediaState = operatorMediaState;
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public String getOperatorName() {
            return operatorName;
        }

        @Override
        public String getFormattedOperatorName() {
            return Utils.formatOperatorName(operatorName);
        }

        @Override
        public String getOperatorProfileImageUrl() {
            return operatorProfileImgUrl;
        }

        @Override
        public String getTime() {
            return time;
        }

        @Override
        public OperatorMediaState getOperatorMediaState() {
            return operatorMediaState;
        }

        @Override
        public VisitorMediaState getVisitorMediaState() {
            return visitorMediaState;
        }

        @Override
        public void setVisitorMediaState(VisitorMediaState visitorMediaState) {
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StartedAudioCall that = (StartedAudioCall) o;
            return Objects.equals(operatorName, that.operatorName) &&
                    Objects.equals(time, that.time) &&
                    Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl) &&
                    Objects.equals(operatorMediaState, that.operatorMediaState) &&
                    Objects.equals(visitorMediaState, that.visitorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operatorName, time, operatorProfileImgUrl, operatorMediaState, visitorMediaState);
        }

        @Override
        public String toString() {
            return "StartedAudioCall{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                    ", operatorMediaState=" + operatorMediaState +
                    ", visitorMediaState=" + visitorMediaState +
                    '}';
        }
    }

    class StartedVideoCall implements CallStatus {
        private final String operatorName;
        private final String time;
        private final String operatorProfileImgUrl;
        private final OperatorMediaState operatorMediaState;
        private VisitorMediaState visitorMediaState;

        public StartedVideoCall(
                String operatorName,
                String time,
                String operatorProfileImgUrl,
                OperatorMediaState operatorMediaState,
                VisitorMediaState visitorMediaState
        ) {
            this.operatorName = operatorName;
            this.time = time;
            this.operatorProfileImgUrl = operatorProfileImgUrl;
            this.operatorMediaState = operatorMediaState;
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public String getOperatorName() {
            return operatorName;
        }

        @Override
        public String getFormattedOperatorName() {
            return Utils.formatOperatorName(operatorName);
        }

        @Override
        public String getOperatorProfileImageUrl() {
            return operatorProfileImgUrl;
        }

        @Override
        public String getTime() {
            return time;
        }

        @Override
        public OperatorMediaState getOperatorMediaState() {
            return operatorMediaState;
        }

        @Override
        public VisitorMediaState getVisitorMediaState() {
            return visitorMediaState;
        }

        @Override
        public void setVisitorMediaState(VisitorMediaState visitorMediaState) {
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StartedVideoCall that = (StartedVideoCall) o;
            return Objects.equals(operatorName, that.operatorName) &&
                    Objects.equals(time, that.time) &&
                    Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl) &&
                    Objects.equals(operatorMediaState, that.operatorMediaState) &&
                    Objects.equals(visitorMediaState, that.visitorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operatorName, time, operatorProfileImgUrl, operatorMediaState, visitorMediaState);
        }

        @Override
        public String toString() {
            return "StartedVideoCall{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                    ", operatorMediaState=" + operatorMediaState +
                    ", visitorMediaState=" + visitorMediaState +
                    '}';
        }
    }
}
