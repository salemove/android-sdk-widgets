package com.glia.widgets.call;

import androidx.annotation.NonNull;

import com.glia.androidsdk.comms.MediaState;

import java.util.Objects;

/**
 * @hide
 */
public interface CallStatus {

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

    MediaState getOperatorMediaState();

    MediaState getVisitorMediaState();

    void setVisitorMediaState(MediaState visitorMediaState);

    /**
     * @hide
     */
    class EngagementNotOngoing implements CallStatus {
        private MediaState visitorMediaState;

        public EngagementNotOngoing(MediaState visitorMediaState) {
            this.visitorMediaState = visitorMediaState;
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
        public MediaState getOperatorMediaState() {
            return null;
        }

        @Override
        public MediaState getVisitorMediaState() {
            return this.visitorMediaState;
        }

        @Override
        public void setVisitorMediaState(MediaState visitorMediaState) {
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

        @NonNull
        @Override
        public String toString() {
            return "EngagementNotOngoing{" +
                "visitorMediaState=" + visitorMediaState +
                '}';
        }
    }

    /**
     * @hide
     */
    class EngagementOngoingOperatorIsConnecting implements CallStatus {
        private final String formattedOperatorName;
        private final String time;
        private final String operatorProfileImgUrl;
        private MediaState visitorMediaState;

        public EngagementOngoingOperatorIsConnecting(
            String formattedOperatorName,
            String time,
            String operatorProfileImgUrl,
            MediaState visitorMediaState
        ) {
            this.formattedOperatorName = formattedOperatorName;
            this.time = time;
            this.operatorProfileImgUrl = operatorProfileImgUrl;
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public String getFormattedOperatorName() {
            return formattedOperatorName;
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
        public MediaState getOperatorMediaState() {
            return null;
        }

        @Override
        public MediaState getVisitorMediaState() {
            return visitorMediaState;
        }

        @Override
        public void setVisitorMediaState(MediaState visitorMediaState) {
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EngagementOngoingOperatorIsConnecting that = (EngagementOngoingOperatorIsConnecting) o;
            return Objects.equals(formattedOperatorName, that.formattedOperatorName) &&
                Objects.equals(time, that.time) &&
                Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl) &&
                Objects.equals(visitorMediaState, that.visitorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(formattedOperatorName, time, operatorProfileImgUrl, visitorMediaState);
        }

        @NonNull
        @Override
        public String toString() {
            return "EngagementOngoingOperatorIsConnecting{" +
                "operatorName='" + formattedOperatorName + '\'' +
                ", time='" + time + '\'' +
                ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                ", visitorMediaState=" + visitorMediaState +
                '}';
        }
    }

    /**
     * @hide
     */
    class EngagementOngoingAudioCallStarted implements CallStatus {
        private final String formattedOperatorName;
        private final String time;
        private final String operatorProfileImgUrl;
        private final MediaState operatorMediaState;
        private MediaState visitorMediaState;

        public EngagementOngoingAudioCallStarted(
            String formattedOperatorName,
            String time,
            String operatorProfileImgUrl,
            MediaState operatorMediaState,
            MediaState visitorMediaState
        ) {
            this.formattedOperatorName = formattedOperatorName;
            this.time = time;
            this.operatorProfileImgUrl = operatorProfileImgUrl;
            this.operatorMediaState = operatorMediaState;
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public String getFormattedOperatorName() {
            return formattedOperatorName;
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
        public MediaState getOperatorMediaState() {
            return operatorMediaState;
        }

        @Override
        public MediaState getVisitorMediaState() {
            return visitorMediaState;
        }

        @Override
        public void setVisitorMediaState(MediaState visitorMediaState) {
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EngagementOngoingAudioCallStarted that = (EngagementOngoingAudioCallStarted) o;
            return Objects.equals(formattedOperatorName, that.formattedOperatorName) &&
                Objects.equals(time, that.time) &&
                Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl) &&
                Objects.equals(operatorMediaState, that.operatorMediaState) &&
                Objects.equals(visitorMediaState, that.visitorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(formattedOperatorName, time, operatorProfileImgUrl, operatorMediaState, visitorMediaState);
        }

        @NonNull
        @Override
        public String toString() {
            return "EngagementOngoingAudioCallStarted{" +
                "operatorName='" + formattedOperatorName + '\'' +
                ", time='" + time + '\'' +
                ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                ", operatorMediaState=" + operatorMediaState +
                ", visitorMediaState=" + visitorMediaState +
                '}';
        }
    }

    /**
     * @hide
     */
    class EngagementOngoingVideoCallStarted implements CallStatus {
        private final String formattedOperatorName;
        private final String time;
        private final String operatorProfileImgUrl;
        private final MediaState operatorMediaState;
        private MediaState visitorMediaState;

        public EngagementOngoingVideoCallStarted(
            String formattedOperatorName,
            String time,
            String operatorProfileImgUrl,
            MediaState operatorMediaState,
            MediaState visitorMediaState
        ) {
            this.formattedOperatorName = formattedOperatorName;
            this.time = time;
            this.operatorProfileImgUrl = operatorProfileImgUrl;
            this.operatorMediaState = operatorMediaState;
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public String getFormattedOperatorName() {
            return formattedOperatorName;
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
        public MediaState getOperatorMediaState() {
            return operatorMediaState;
        }

        @Override
        public MediaState getVisitorMediaState() {
            return visitorMediaState;
        }

        @Override
        public void setVisitorMediaState(MediaState visitorMediaState) {
            this.visitorMediaState = visitorMediaState;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EngagementOngoingVideoCallStarted that = (EngagementOngoingVideoCallStarted) o;
            return Objects.equals(formattedOperatorName, that.formattedOperatorName) &&
                Objects.equals(time, that.time) &&
                Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl) &&
                Objects.equals(operatorMediaState, that.operatorMediaState) &&
                Objects.equals(visitorMediaState, that.visitorMediaState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(formattedOperatorName, time, operatorProfileImgUrl, operatorMediaState, visitorMediaState);
        }

        @NonNull
        @Override
        public String toString() {
            return "EngagementOngoingVideoCallStarted{" +
                "operatorName='" + formattedOperatorName + '\'' +
                ", time='" + time + '\'' +
                ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                ", operatorMediaState=" + operatorMediaState +
                ", visitorMediaState=" + visitorMediaState +
                '}';
        }
    }

    /**
     * @hide
     */
    class EngagementOngoingTransferring implements CallStatus {
        private final String time;
        private MediaState visitorMediaState;

        public EngagementOngoingTransferring(String time, MediaState visitorMediaState) {
            setVisitorMediaState(visitorMediaState);
            this.time = time;
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
        public MediaState getOperatorMediaState() {
            return null;
        }

        @Override
        public MediaState getVisitorMediaState() {
            return visitorMediaState;
        }

        @Override
        public void setVisitorMediaState(MediaState visitorMediaState) {
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

        @NonNull
        @Override
        public String toString() {
            return "EngagementOngoingTransferring{" +
                ", time='" + time + '\'' +
                ", visitorMediaState=" + visitorMediaState +
                '}';
        }
    }
}
