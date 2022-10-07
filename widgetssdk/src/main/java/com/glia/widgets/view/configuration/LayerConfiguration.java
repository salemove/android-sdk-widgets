package com.glia.widgets.view.configuration;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Nullable;

import com.glia.widgets.R;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.ResourceProvider;

public class LayerConfiguration implements Parcelable {
    @Nullable
    private final ColorConfiguration backgroundColor;

    @Nullable
    private final ColorConfiguration borderColor;

    // Border width in DP.
    @Nullable
    private final Integer borderWidth;

    // Layer corner radius in DP.
    @Nullable
    private final Integer cornerRadius;

    private LayerConfiguration(Builder builder) {
        this.backgroundColor = builder.backgroundColor;
        this.borderColor = builder.borderColor;
        this.borderWidth = builder.borderWidth;
        this.cornerRadius = builder.cornerRadius;
    }

    @Nullable
    public ColorConfiguration getBackgroundColorConfiguration() {
        return backgroundColor;
    }

    @Deprecated // Remove after refactoring
    public String getBackgroundColor() {
        return backgroundColor.getColor();
    }

    @Nullable
    public ColorConfiguration getBorderColorConfiguration() {
        return borderColor;
    }

    @Deprecated // Remove after refactoring
    public String getBorderColor() {
        return borderColor.getColor();
    }

    @Nullable
    public Integer getBorderWidth() {
        return borderWidth;
    }

    @Nullable
    public Integer getCornerRadius() {
        return cornerRadius;
    }

    public static class Builder {
        private ColorConfiguration backgroundColor;
        private ColorConfiguration borderColor;
        // Border width in DP.
        private Integer borderWidth;
        // Layer corner radius in DP.
        private Integer cornerRadius;

        public Builder layerConfiguration(LayerConfiguration configuration) {
            this.backgroundColor = configuration.backgroundColor;
            this.borderColor = configuration.borderColor;
            this.borderWidth = configuration.borderWidth;
            this.cornerRadius = configuration.cornerRadius;
            return this;
        }

        public Builder backgroundColor(String backgroundColor) {
            this.backgroundColor = new ColorConfiguration.Builder()
                    .setColor(android.graphics.Color.parseColor(backgroundColor))
                    .build();
            return this;
        }

        public Builder backgroundColor(@ColorRes int colorResource) {
            this.backgroundColor = new ColorConfiguration.Builder()
                    .setColorResource(colorResource)
                    .build();
            return this;
        }

        public Builder backgroundColor(ColorConfiguration backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder borderColor(String borderColor) {
            this.borderColor = new ColorConfiguration.Builder()
                    .setColor(android.graphics.Color.parseColor(borderColor))
                    .build();
            return this;
        }

        public Builder borderColor(@ColorRes int colorResource) {
            this.borderColor = new ColorConfiguration.Builder()
                    .setColorResource(colorResource)
                    .build();
            return this;
        }

        public Builder borderColor(ColorConfiguration borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        public Builder borderWidth(int borderWidth) {
            this.borderWidth = borderWidth;
            return this;
        }

        public Builder borderWidthDimension(@DimenRes int dimenRes) {
            return borderWidth(Dependencies.getResourceProvider().getDimension(dimenRes));
        }

        public Builder cornerRadius(int cornerRadius) {
            this.cornerRadius = cornerRadius;
            return this;
        }

        public Builder cornerRadiusDimension(@DimenRes int dimenRes) {
            return cornerRadius(Dependencies.getResourceProvider().getDimension(dimenRes));
        }

        @SuppressLint("ResourceType")
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
        dest.writeParcelable(this.backgroundColor, flags);
        dest.writeParcelable(this.borderColor, flags);
        dest.writeValue(this.borderWidth);
        dest.writeValue(this.cornerRadius);
    }

    protected LayerConfiguration(Parcel in) {
        this.backgroundColor = in.readParcelable(ColorConfiguration.class.getClassLoader());
        this.borderColor = in.readParcelable(ColorConfiguration.class.getClassLoader());
        this.borderWidth = (Integer) in.readValue(Integer.class.getClassLoader());
        this.cornerRadius = (Integer) in.readValue(Integer.class.getClassLoader());
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
