package com.glia.widgets.view.configuration.call;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ButtonBarConfiguration implements Parcelable {
    @Nullable
    private final BarButtonStatesConfiguration chatButton;

    @Nullable
    private final BarButtonStatesConfiguration minimizeButton;

    @Nullable
    private final BarButtonStatesConfiguration muteButton;

    @Nullable
    private final BarButtonStatesConfiguration speakerButton;

    @Nullable
    private final BarButtonStatesConfiguration videoButton;

    private ButtonBarConfiguration(@NonNull Builder builder) {
        this.chatButton = builder.chatButton;
        this.minimizeButton = builder.minimizeButton;
        this.muteButton = builder.muteButton;
        this.speakerButton = builder.speakerButton;
        this.videoButton = builder.videoButton;
    }

    @Nullable
    public BarButtonStatesConfiguration getChatButton() {
        return chatButton;
    }

    @Nullable
    public BarButtonStatesConfiguration getMinimizeButton() {
        return minimizeButton;
    }

    @Nullable
    public BarButtonStatesConfiguration getMuteButton() {
        return muteButton;
    }

    @Nullable
    public BarButtonStatesConfiguration getSpeakerButton() {
        return speakerButton;
    }

    @Nullable
    public BarButtonStatesConfiguration getVideoButton() {
        return videoButton;
    }

    public static class Builder {
        @Nullable
        private BarButtonStatesConfiguration chatButton;

        @Nullable
        private BarButtonStatesConfiguration minimizeButton;

        @Nullable
        private BarButtonStatesConfiguration muteButton;

        @Nullable
        private BarButtonStatesConfiguration speakerButton;

        @Nullable
        private BarButtonStatesConfiguration videoButton;

        public Builder setButtonBarConfiguration(@NonNull ButtonBarConfiguration configuration) {
            this.chatButton = configuration.chatButton;
            this.minimizeButton = configuration.minimizeButton;
            this.muteButton = configuration.muteButton;
            this.speakerButton = configuration.speakerButton;
            this.videoButton = configuration.videoButton;
            return this;
        }

        public Builder setChatButton(@Nullable BarButtonStatesConfiguration chatButton) {
            this.chatButton = chatButton;
            return this;
        }

        public Builder setMinimizeButton(@Nullable BarButtonStatesConfiguration minimizeButton) {
            this.minimizeButton = minimizeButton;
            return this;
        }

        public Builder setMuteButton(@Nullable BarButtonStatesConfiguration muteButton) {
            this.muteButton = muteButton;
            return this;
        }

        public Builder setSpeakerButton(@Nullable BarButtonStatesConfiguration speakerButton) {
            this.speakerButton = speakerButton;
            return this;
        }

        public Builder setVideoButton(@Nullable BarButtonStatesConfiguration videoButton) {
            this.videoButton = videoButton;
            return this;
        }

        public ButtonBarConfiguration build() {
            return new ButtonBarConfiguration(this);
        }
    }

    public static ButtonBarConfiguration getDefaultButtonBarConfiguration() {
        BarButtonStatesConfiguration defaultBarButtonStatesConfiguration =
                BarButtonStatesConfiguration.getDefaultBarButtonStatesConfiguration();
        return new Builder()
                .setChatButton(defaultBarButtonStatesConfiguration)
                .setMinimizeButton(defaultBarButtonStatesConfiguration)
                .setMuteButton(defaultBarButtonStatesConfiguration)
                .setSpeakerButton(defaultBarButtonStatesConfiguration)
                .setVideoButton(defaultBarButtonStatesConfiguration)
                .build();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.chatButton, flags);
        dest.writeParcelable(this.minimizeButton, flags);
        dest.writeParcelable(this.muteButton, flags);
        dest.writeParcelable(this.speakerButton, flags);
        dest.writeParcelable(this.videoButton, flags);
    }

    protected ButtonBarConfiguration(Parcel in) {
        this.chatButton = in.readParcelable(BarButtonStatesConfiguration.class.getClassLoader());
        this.minimizeButton = in.readParcelable(BarButtonStatesConfiguration.class.getClassLoader());
        this.muteButton = in.readParcelable(BarButtonStatesConfiguration.class.getClassLoader());
        this.speakerButton = in.readParcelable(BarButtonStatesConfiguration.class.getClassLoader());
        this.videoButton = in.readParcelable(BarButtonStatesConfiguration.class.getClassLoader());
    }

    public static final Creator<ButtonBarConfiguration> CREATOR = new Creator<ButtonBarConfiguration>() {
        @Override
        public ButtonBarConfiguration createFromParcel(Parcel source) {
            return new ButtonBarConfiguration(source);
        }

        @Override
        public ButtonBarConfiguration[] newArray(int size) {
            return new ButtonBarConfiguration[size];
        }
    };
}
