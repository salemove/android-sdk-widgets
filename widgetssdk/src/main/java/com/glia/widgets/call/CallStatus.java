package com.glia.widgets.call;

import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.widgets.helper.Utils;

import java.util.Objects;

public interface CallStatus {

    String getOperatorName();

    String getFormattedOperatorName();

    String getTime();

    OperatorMediaState getOperatorMediaState();

    class NotOngoing implements CallStatus {

        @Override
        public String getOperatorName() {
            throw new UnsupportedOperationException("Not supposed to happen!");
        }

        @Override
        public String getFormattedOperatorName() {
            throw new UnsupportedOperationException("Not supposed to happen!");
        }

        @Override
        public String getTime() {
            throw new UnsupportedOperationException("Not supposed to happen!");
        }

        @Override
        public OperatorMediaState getOperatorMediaState() {
            throw new UnsupportedOperationException("Not supposed to happen!");
        }
    }

    class Ongoing implements CallStatus {
        private final String operatorName;
        private final String time;

        public Ongoing(String operatorName, String time) {
            this.operatorName = operatorName;
            this.time = time;
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
        public String getTime() {
            return time;
        }

        @Override
        public OperatorMediaState getOperatorMediaState() {
            return null;
        }

        @Override
        public String toString() {
            return "Ongoing{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ongoing ongoing = (Ongoing) o;
            return Objects.equals(operatorName, ongoing.operatorName) &&
                    Objects.equals(time, ongoing.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operatorName, time);
        }
    }

    class StartedAudioCall implements CallStatus {
        private final String operatorName;
        private final String time;
        private final OperatorMediaState operatorMediaState;

        public StartedAudioCall(String operatorName, String time, OperatorMediaState operatorMediaState) {
            this.operatorName = operatorName;
            this.time = time;
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
        public String getTime() {
            return time;
        }

        @Override
        public OperatorMediaState getOperatorMediaState() {
            return operatorMediaState;
        }

        @Override
        public String toString() {
            return "StartedAudioCall{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    ", operatorMediaState=" + operatorMediaState +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StartedAudioCall that = (StartedAudioCall) o;
            return Objects.equals(operatorName, that.operatorName) &&
                    Objects.equals(time, that.time) &&
                    Objects.equals(operatorMediaState, that.operatorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operatorName, time, operatorMediaState);
        }
    }

    class StartedVideoCall implements CallStatus {
        private final String operatorName;
        private final String time;
        private final OperatorMediaState operatorMediaState;
        private final VisitorMediaState visitorMediaState;

        public StartedVideoCall(
                String operatorName,
                String time,
                OperatorMediaState operatorMediaState,
                VisitorMediaState visitorMediaState
        ) {
            this.operatorName = operatorName;
            this.time = time;
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
        public String toString() {
            return "StartedVideoCall{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    ", operatorMediaState=" + operatorMediaState +
                    ", visitorMediaState=" + visitorMediaState +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StartedVideoCall that = (StartedVideoCall) o;
            return Objects.equals(operatorName, that.operatorName) &&
                    Objects.equals(time, that.time) &&
                    Objects.equals(operatorMediaState, that.operatorMediaState) &&
                    Objects.equals(visitorMediaState, that.visitorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operatorName, time, operatorMediaState, visitorMediaState);
        }
    }
}
