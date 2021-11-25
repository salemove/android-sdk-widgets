package com.glia.widgets.view.configuration;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class ButtonConfiguration implements Parcelable {
    private final ColorStateList backgroundColor;
    private final ColorStateList strokeColor;
    private final Integer strokeWidth;
    private final TextConfiguration textConfiguration;

    private ButtonConfiguration(
            Builder builder
    ) {
        backgroundColor = builder.backgroundColor;
        strokeColor = builder.strokeColor;
        strokeWidth = builder.strokeWidth;
        textConfiguration = builder.textConfiguration;
    }

    protected ButtonConfiguration(Parcel in) {
        backgroundColor = in.readParcelable(ColorStateList.class.getClassLoader());
        strokeColor = in.readParcelable(ColorStateList.class.getClassLoader());
        if (in.readByte() == 0) {
            strokeWidth = null;
        } else {
            strokeWidth = in.readInt();
        }
        textConfiguration = in.readParcelable(TextConfiguration.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(backgroundColor, flags);
        dest.writeParcelable(strokeColor, flags);
        if (strokeWidth == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(strokeWidth);
        }
        dest.writeParcelable(textConfiguration, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ButtonConfiguration> CREATOR = new Creator<ButtonConfiguration>() {
        @Override
        public ButtonConfiguration createFromParcel(Parcel in) {
            return new ButtonConfiguration(in);
        }

        @Override
        public ButtonConfiguration[] newArray(int size) {
            return new ButtonConfiguration[size];
        }
    };

    @Nullable
    public ColorStateList getBackgroundColor() {
        return backgroundColor;
    }

    @Nullable
    public ColorStateList getStrokeColor() {
        return strokeColor;
    }

    @Nullable
    public Integer getStrokeWidth() {
        return strokeWidth;
    }

    public TextConfiguration getTextConfiguration() {
        return textConfiguration;
    }

    public static class Builder {
        private ColorStateList backgroundColor;
        private ColorStateList strokeColor;
        private Integer strokeWidth;
        private TextConfiguration textConfiguration;

        public Builder() {
        }

        public Builder(ButtonConfiguration buildTime) {
            backgroundColor = buildTime.backgroundColor;
            strokeColor = buildTime.strokeColor;
            strokeWidth = buildTime.strokeWidth;
            textConfiguration = buildTime.textConfiguration;
        }

        public Builder backgroundColor(ColorStateList colorStateList) {
            this.backgroundColor = colorStateList;
            return this;
        }

        public Builder strokeColor(ColorStateList colorStateList) {
            this.strokeColor = colorStateList;
            return this;
        }

        public Builder strokeWidth(Integer strokeWidth) {
            this.strokeWidth = strokeWidth;
            return this;
        }

        public Builder textConfiguration(TextConfiguration textConfiguration) {
            this.textConfiguration = textConfiguration;
            return this;
        }

        public ButtonConfiguration build() {
            return new ButtonConfiguration(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ButtonConfiguration configuration) {
        return new Builder(configuration);
    }
}
