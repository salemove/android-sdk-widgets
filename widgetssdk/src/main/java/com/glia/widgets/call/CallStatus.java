package com.glia.widgets.call;

import com.glia.widgets.helper.Utils;

import java.util.Objects;

public interface CallStatus {

    class NotOngoing implements CallStatus {

    }

    class StartedAudioCall implements CallStatus {
        final String operatorName;
        final String time;

        public StartedAudioCall(String operatorName, String time) {
            this.operatorName = operatorName;
            this.time = time;
        }

        public String getFormattedOperatorName() {
            return Utils.formatOperatorName(operatorName);
        }

        @Override
        public String toString() {
            return "StartedAudioCall{" +
                    "operatorName='" + operatorName + '\'' +
                    ", time='" + time + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StartedAudioCall that = (StartedAudioCall) o;
            return Objects.equals(operatorName, that.operatorName) &&
                    Objects.equals(time, that.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operatorName, time);
        }
    }
}
