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
     * In case of {@link EngagementOngoingOperatorIsConnecting} the time displays the time it takes to upgrade to
     * {@link EngagementOngoingVideoCallStarted} or {@link EngagementOngoingAudioCallStarted}.
     * In case of {@link EngagementOngoingVideoCallStarted} or {@link EngagementOngoingAudioCallStarted} the time is the ongoing
     * call duration
     *
     * @return A string value of the time. Either 0,1,2,3 in case of {@link EngagementOngoingOperatorIsConnecting}
     * or MM:ss in case of {@link EngagementOngoingAudioCallStarted} or {@link EngagementOngoingVideoCallStarted}
     */
    String getTime();

    OperatorMediaState getOperatorMediaState();

    VisitorMediaState getVisitorMediaState();

    void setVisitorMediaState(VisitorMediaState visitorMediaState);

    class EngagementNotOngoing implements CallStatus {
        private VisitorMediaState visitorMediaState;

        public EngagementNotOngoing(VisitorMediaState visitorMediaState) {
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
            EngagementNotOngoing that = (EngagementNotOngoing) o;
            return Objects.equals(visitorMediaState, that.visitorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(visitorMediaState);
        }

        @Override
        public String toString() {
            return "EngagementNotOngoing{" +
                    "visitorMediaState=" + visitorMediaState +
                    '}';
        }
    }

    class EngagementOngoingOperatorIsConnecting implements CallStatus {
        private final String operatorName;
        private final String time;
        private final String operatorProfileImgUrl;
        private VisitorMediaState visitorMediaState;

        public EngagementOngoingOperatorIsConnecting(String operatorName,
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
            EngagementOngoingOperatorIsConnecting that = (EngagementOngoingOperatorIsConnecting) o;
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
            return "EngagementOngoingOperatorIsConnecting{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                    ", visitorMediaState=" + visitorMediaState +
                    '}';
        }
    }

    class EngagementOngoingAudioCallStarted implements CallStatus {
        private final String operatorName;
        private final String time;
        private final String operatorProfileImgUrl;
        private final OperatorMediaState operatorMediaState;
        private VisitorMediaState visitorMediaState;

        public EngagementOngoingAudioCallStarted(
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
            EngagementOngoingAudioCallStarted that = (EngagementOngoingAudioCallStarted) o;
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
            return "EngagementOngoingAudioCallStarted{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                    ", operatorMediaState=" + operatorMediaState +
                    ", visitorMediaState=" + visitorMediaState +
                    '}';
        }
    }

    class EngagementOngoingVideoCallStarted implements CallStatus {
        private final String operatorName;
        private final String time;
        private final String operatorProfileImgUrl;
        private final OperatorMediaState operatorMediaState;
        private VisitorMediaState visitorMediaState;

        public EngagementOngoingVideoCallStarted(
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
            EngagementOngoingVideoCallStarted that = (EngagementOngoingVideoCallStarted) o;
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
            return "EngagementOngoingVideoCallStarted{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                    ", operatorMediaState=" + operatorMediaState +
                    ", visitorMediaState=" + visitorMediaState +
                    '}';
        }
    }

    class EngagementOngoingTransferring implements CallStatus {
        private final String time;
        private VisitorMediaState visitorMediaState;

        public EngagementOngoingTransferring(
                String time,
                VisitorMediaState visitorMediaState
        ) {
            setVisitorMediaState(visitorMediaState);
            this.time = time;
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
            return time;
        }

        @Override
        public OperatorMediaState getOperatorMediaState() {
            return null;
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
            EngagementOngoingTransferring that = (EngagementOngoingTransferring) o;
            return Objects.equals(time, that.time) &&
                    Objects.equals(visitorMediaState, that.visitorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(time, visitorMediaState);
        }

        @Override
        public String toString() {
            return "EngagementOngoingTransferring{" +
                    ", time='" + time + '\'' +
                    ", visitorMediaState=" + visitorMediaState +
                    '}';
        }
    }
}
