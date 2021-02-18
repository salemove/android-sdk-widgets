package com.glia.widgets.call;

import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.widgets.helper.Utils;

import java.util.Objects;

public interface CallStatus {

    String getOperatorName();

    String getFormattedOperatorName();

    String getOperatorProfileImageUrl();

    String getTime();

    OperatorMediaState getOperatorMediaState();

    class NotOngoing implements CallStatus {

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
    }

    class Ongoing implements CallStatus {
        private final String operatorName;
        private final String time;
        private final String operatorProfileImgUrl;

        public Ongoing(String operatorName, String time, String operatorProfileImgUrl) {
            this.operatorName = operatorName;
            this.time = time;
            this.operatorProfileImgUrl = operatorProfileImgUrl;
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ongoing ongoing = (Ongoing) o;
            return Objects.equals(operatorName, ongoing.operatorName) &&
                    Objects.equals(time, ongoing.time) &&
                    Objects.equals(operatorProfileImgUrl, ongoing.operatorProfileImgUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operatorName, time, operatorProfileImgUrl);
        }

        @Override
        public String toString() {
            return "Ongoing{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                    '}';
        }
    }

    class StartedAudioCall implements CallStatus {
        private final String operatorName;
        private final String time;
        private final String operatorProfileImgUrl;
        private final OperatorMediaState operatorMediaState;

        public StartedAudioCall(
                String operatorName,
                String time,
                String operatorProfileImgUrl,
                OperatorMediaState operatorMediaState
        ) {
            this.operatorName = operatorName;
            this.time = time;
            this.operatorProfileImgUrl = operatorProfileImgUrl;
            this.operatorMediaState = operatorMediaState;
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StartedAudioCall that = (StartedAudioCall) o;
            return Objects.equals(operatorName, that.operatorName) &&
                    Objects.equals(time, that.time) &&
                    Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl) &&
                    Objects.equals(operatorMediaState, that.operatorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operatorName, time, operatorProfileImgUrl, operatorMediaState);
        }

        @Override
        public String toString() {
            return "StartedAudioCall{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                    ", operatorMediaState=" + operatorMediaState +
                    '}';
        }
    }

    class StartedVideoCall implements CallStatus {
        private final String operatorName;
        private final String time;
        private final String operatorProfileImgUrl;
        private final OperatorMediaState operatorMediaState;
        private final VisitorMediaState visitorMediaState;

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

        public VisitorMediaState getVisitorMediaState() {
            return visitorMediaState;
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
