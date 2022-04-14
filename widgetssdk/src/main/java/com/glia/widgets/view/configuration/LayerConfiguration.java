package com.glia.widgets.view.configuration;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.helper.ResourceProvider;

public class LayerConfiguration implements Parcelable {
    private static final int DEFAULT_BORDER_WIDTH = 1;
    private static final int DEFAULT_CORNER_RADIUS = 0;

    // Background hex color.
    private String backgroundColor;
    // Border hex color.
    private String borderColor;
    // Border width.
    private int borderWidth;
    // Layer corner radius.
    private int cornerRadius;

    public LayerConfiguration(Builder builder) {
        this.backgroundColor = builder.backgroundColor;
        this.borderColor = builder.borderColor;
        this.borderWidth = builder.borderWidth;
        this.cornerRadius = builder.cornerRadius;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public static class Builder {
        // Background hex color.
        private String backgroundColor;
        // Border hex color.
        private String borderColor;
        // Border width.
        private int borderWidth;
        // Layer corner radius.
        private int cornerRadius;

        @SuppressLint("ResourceType")
        public Builder(ResourceProvider resourceProvider) {
            // Default configuration
            this.backgroundColor = resourceProvider.getString(R.color.glia_base_light_color);
            this.borderColor = resourceProvider.getString(R.color.glia_stroke_gray);
            this.borderWidth = DEFAULT_BORDER_WIDTH;
            this.cornerRadius = DEFAULT_CORNER_RADIUS;
        }

        public Builder backgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }
        public Builder borderColor(String borderColor) {
            this.borderColor = borderColor;
            return this;
        }
        public Builder borderWidth(int borderWidth) {
            this.borderWidth = borderWidth;
            return this;
        }
        public Builder cornerRadius(int cornerRadius) {
            this.cornerRadius = cornerRadius;
            return this;
        }

        public LayerConfiguration build() {
            return new LayerConfiguration(this);
        }
    }

    /* BEGIN: Parcelable related */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.backgroundColor);
        dest.writeString(this.borderColor);
        dest.writeInt(this.borderWidth);
        dest.writeInt(this.cornerRadius);
    }

    public void readFromParcel(Parcel source) {
        this.backgroundColor = source.readString();
        this.borderColor = source.readString();
        this.borderWidth = source.readInt();
        this.cornerRadius = source.readInt();
    }

    protected LayerConfiguration(Parcel in) {
        this.backgroundColor = in.readString();
        this.borderColor = in.readString();
        this.borderWidth = in.readInt();
        this.cornerRadius = in.readInt();
    }

    public static final Creator<LayerConfiguration> CREATOR = new Creator<LayerConfiguration>() {
        @Override
        public LayerConfiguration createFromParcel(Parcel source) {
            return new LayerConfiguration(source);
        }

        @Override
        public LayerConfiguration[] newArray(int size) {
            return new LayerConfiguration[size];
        }
    };
    /* END: Parcelable related */
}