package com.glia.widgets.view.configuration;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.helper.ResourceProvider;

public class OptionButtonConfiguration implements Parcelable {
    // Title text for normal state.
    private TextConfiguration normalText;
    // Option layer for normal state.
    private LayerConfiguration normalLayer;
    // Title text style when option is highlighted.
    private TextConfiguration highlightedText;
    // Layer style when option is highlighted.
    private LayerConfiguration highlightedLayer;

    public OptionButtonConfiguration(Builder builder) {
        this.normalText = builder.normalText;
        this.normalLayer = builder.normalLayer;
        this.highlightedText = builder.highlightedText;
        this.highlightedLayer = builder.highlightedLayer;
    }

    public TextConfiguration getNormalText() {
        return normalText;
    }

    public LayerConfiguration getNormalLayer() {
        return normalLayer;
    }

    public TextConfiguration getHighlightedText() {
        return highlightedText;
    }

    public LayerConfiguration getHighlightedLayer() {
        return highlightedLayer;
    }

    public static class Builder {
        // Title text for normal state.
        private TextConfiguration normalText;
        // Option layer for normal state.
        private LayerConfiguration normalLayer;
        // Title text style when option is highlighted.
        private TextConfiguration highlightedText;
        // Layer style when option is highlighted.
        private LayerConfiguration highlightedLayer;

        public Builder(ResourceProvider resourceProvider) {
            // Default configuration
            this.normalText = new TextConfiguration.Builder().build();
            this.normalLayer = new LayerConfiguration.Builder(resourceProvider).build();
            this.highlightedText = new TextConfiguration.Builder().build();
            this.highlightedLayer = new LayerConfiguration.Builder(resourceProvider).build();
        }

        public Builder normalText(TextConfiguration normalText) {
            this.normalText = normalText;
            return this;
        }

        public Builder normalLayer(LayerConfiguration normalLayer) {
            this.normalLayer = normalLayer;
            return this;
        }

        public Builder highlightedText(TextConfiguration highlightedText) {
            this.highlightedText = highlightedText;
            return this;
        }

        public Builder highlightedLayer(LayerConfiguration highlightedLayer) {
            this.highlightedLayer = highlightedLayer;
            return this;
        }

        public OptionButtonConfiguration build() {
            return new OptionButtonConfiguration(this);
        }
    }

    /* BEGIN: Parcelable related */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.normalText, flags);
        dest.writeParcelable(this.normalLayer, flags);
        dest.writeParcelable(this.highlightedText, flags);
        dest.writeParcelable(this.highlightedLayer, flags);
    }

    public void readFromParcel(Parcel source) {
        this.normalText = source.readParcelable(TextConfiguration.class.getClassLoader());
        this.normalLayer = source.readParcelable(LayerConfiguration.class.getClassLoader());
        this.highlightedText = source.readParcelable(TextConfiguration.class.getClassLoader());
        this.highlightedLayer = source.readParcelable(LayerConfiguration.class.getClassLoader());
    }

    protected OptionButtonConfiguration(Parcel in) {
        this.normalText = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.normalLayer = in.readParcelable(LayerConfiguration.class.getClassLoader());
        this.highlightedText = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.highlightedLayer = in.readParcelable(LayerConfiguration.class.getClassLoader());
    }

    public static final Creator<OptionButtonConfiguration> CREATOR = new Creator<OptionButtonConfiguration>() {
        @Override
        public OptionButtonConfiguration createFromParcel(Parcel source) {
            return new OptionButtonConfiguration(source);
        }

        @Override
        public OptionButtonConfiguration[] newArray(int size) {
            return new OptionButtonConfiguration[size];
        }
    };
    /* END: Parcelable related */
}