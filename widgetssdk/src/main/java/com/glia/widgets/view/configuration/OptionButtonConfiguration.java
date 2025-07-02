package com.glia.widgets.view.configuration;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.ResourceProvider;

/**
 * Please use remote configurations {@link com.glia.widgets.GliaWidgetsConfig.Builder#setUiJsonRemoteConfig(String)}
 */
@Deprecated
public class OptionButtonConfiguration implements Parcelable {
    // Title text for normal state.
    private TextConfiguration normalText;
    // Option layer for normal state.
    private LayerConfiguration normalLayer;
    // Title text style when option is selected.
    private TextConfiguration selectedText;
    // Option layer for selected state.
    private LayerConfiguration selectedLayer;
    // Title text style when option is highlighted.
    private TextConfiguration highlightedText;
    // Option layer for highlighted state.
    private LayerConfiguration highlightedLayer;

    public OptionButtonConfiguration(Builder builder) {
        this.normalText = builder.normalText;
        this.normalLayer = builder.normalLayer;
        this.selectedText = builder.selectedText;
        this.selectedLayer = builder.selectedLayer;
        this.highlightedText = builder.highlightedText;
        this.highlightedLayer = builder.highlightedLayer;
    }

    public TextConfiguration getNormalText() {
        return normalText;
    }

    public LayerConfiguration getNormalLayer() {
        return normalLayer;
    }

    public TextConfiguration getSelectedText() {
        return selectedText;
    }

    public LayerConfiguration getSelectedLayer() {
        return selectedLayer;
    }

    public TextConfiguration getHighlightedText() {
        return highlightedText;
    }

    public LayerConfiguration getHighlightedLayer() {
        return highlightedLayer;
    }

    /**
     * Please use remote configurations {@link com.glia.widgets.GliaWidgetsConfig.Builder#setUiJsonRemoteConfig(String)}
     */
    @Deprecated
    public static class Builder {
        private final String TAG = OptionButtonConfiguration.Builder.class.getSimpleName();

        // Title text for normal state.
        private TextConfiguration normalText;
        // Option layer for normal state.
        private LayerConfiguration normalLayer;
        // Title text style when option is selected.
        private TextConfiguration selectedText;
        // Option layer for selected state.
        private LayerConfiguration selectedLayer;
        // Title text style when option is highlighted.
        private TextConfiguration highlightedText;
        // Option layer for highlighted state.
        private LayerConfiguration highlightedLayer;

        public Builder normalText(TextConfiguration normalText) {
            this.normalText = normalText;
            return this;
        }

        public Builder normalLayer(LayerConfiguration normalLayer) {
            this.normalLayer = normalLayer;
            return this;
        }

        public Builder selectedText(TextConfiguration selectedText) {
            this.selectedText = selectedText;
            return this;
        }

        public Builder selectedLayer(LayerConfiguration selectedLayer) {
            this.selectedLayer = selectedLayer;
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
            Logger.logDeprecatedClassUse(OptionButtonConfiguration.class.getSimpleName() + "." + TAG);
            ResourceProvider resourceProvider = Dependencies.getResourceProvider();
            if (this.normalText == null) {
                this.normalText = prepareDefaultTextConfiguration(resourceProvider, R.color.glia_dark_color);
            }
            if (this.normalLayer == null) {
                this.normalLayer = prepareDefaultLayerConfiguration();
            }
            if (this.selectedText == null) {
                this.selectedText = prepareDefaultTextConfiguration(resourceProvider, R.color.glia_light_color);
            }
            if (this.selectedLayer == null) {
                this.selectedLayer = prepareDefaultLayerConfiguration();
            }
            if (this.highlightedText == null) {
                this.highlightedText = prepareDefaultTextConfiguration(resourceProvider, R.color.glia_dark_color);
            }
            if (this.highlightedLayer == null) {
                this.highlightedLayer = prepareDefaultLayerConfiguration();
            }
            return new OptionButtonConfiguration(this);
        }

        private LayerConfiguration prepareDefaultLayerConfiguration() {
            return new LayerConfiguration.Builder().build();
        }

        private TextConfiguration prepareDefaultTextConfiguration(ResourceProvider resourceProvider, int textColor) {
            return new TextConfiguration.Builder()
                .textColor(resourceProvider.getColorStateList(textColor))
                .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_text_size))
                .build(resourceProvider);
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
        dest.writeParcelable(this.selectedText, flags);
        dest.writeParcelable(this.selectedLayer, flags);
        dest.writeParcelable(this.highlightedText, flags);
        dest.writeParcelable(this.highlightedLayer, flags);
    }

    public void readFromParcel(Parcel source) {
        this.normalText = source.readParcelable(TextConfiguration.class.getClassLoader());
        this.normalLayer = source.readParcelable(LayerConfiguration.class.getClassLoader());
        this.selectedText = source.readParcelable(TextConfiguration.class.getClassLoader());
        this.selectedLayer = source.readParcelable(LayerConfiguration.class.getClassLoader());
        this.highlightedText = source.readParcelable(TextConfiguration.class.getClassLoader());
        this.highlightedLayer = source.readParcelable(LayerConfiguration.class.getClassLoader());
    }

    protected OptionButtonConfiguration(Parcel in) {
        this.normalText = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.normalLayer = in.readParcelable(LayerConfiguration.class.getClassLoader());
        this.selectedText = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.selectedLayer = in.readParcelable(LayerConfiguration.class.getClassLoader());
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
