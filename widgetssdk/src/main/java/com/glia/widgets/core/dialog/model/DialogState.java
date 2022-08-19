package com.glia.widgets.core.dialog.model;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.widgets.core.dialog.Dialog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class DialogState {
    @Dialog.Mode
    private final int mode;

    public DialogState(@Dialog.Mode int mode) {
        this.mode = mode;
    }

    @Dialog.Mode
    public int getMode() {
        return mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DialogState that = (DialogState) o;
        return mode == that.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode);
    }

    @NonNull
    @Override
    public String toString() {
        return "DialogState{" +
                "mode=" + mode +
                '}';
    }

    public static class OperatorName extends DialogState {
        @NonNull
        private final String operatorName;

        public OperatorName(@Dialog.Mode int mode, @NonNull String operatorName) {
            super(mode);
            this.operatorName = operatorName;
        }

        @NonNull
        public String getOperatorName() {
            return operatorName;
        }

        @NonNull
        @Override
        public String toString() {
            return "OperatorName{" +
                    "operatorName='" + operatorName + '\'' +
                    "} " + super.toString();
        }
    }

    public static final class MediaUpgrade extends OperatorName {
        public static final int MODE_AUDIO = 0;
        public static final int MODE_VIDEO_ONE_WAY = 1;
        public static final int MODE_VIDEO_TWO_WAY = 2;
        @NonNull
        private final MediaUpgradeOffer mediaUpgradeOffer;
        private final @MediaUpgrade.Mode int mediaUpgradeMode;

        public MediaUpgrade(@NonNull MediaUpgradeOffer mediaUpgradeOffer, @NonNull String operatorName, @MediaUpgrade.Mode int mediaUpgradeMode) {
            super(Dialog.MODE_MEDIA_UPGRADE, operatorName);
            this.mediaUpgradeOffer = mediaUpgradeOffer;
            this.mediaUpgradeMode = mediaUpgradeMode;
        }

        @NonNull
        public MediaUpgradeOffer getMediaUpgradeOffer() {
            return mediaUpgradeOffer;
        }

        @MediaUpgrade.Mode
        public int getMediaUpgradeMode() {
            return mediaUpgradeMode;
        }

        @NonNull
        @Override
        public String toString() {
            return "MediaUpgrade{" +
                    "mediaUpgradeOffer=" + mediaUpgradeOffer +
                    ", mediaUpgradeMode=" + mediaUpgradeMode +
                    "} " + super.toString();
        }

        @IntDef({MODE_AUDIO, MODE_VIDEO_ONE_WAY, MODE_VIDEO_TWO_WAY})
        @Retention(RetentionPolicy.SOURCE)
        public @interface Mode {
        }
    }
}
