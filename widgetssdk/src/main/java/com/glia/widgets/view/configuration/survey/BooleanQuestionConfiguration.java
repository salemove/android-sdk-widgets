package com.glia.widgets.view.configuration.survey;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.OptionButtonConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;

/**
 * Please use remote configurations {@link com.glia.widgets.GliaWidgetsConfig.Builder#setUiJsonRemoteConfig(String)}
 */
@Deprecated
public class BooleanQuestionConfiguration implements Parcelable {
    private TextConfiguration title;
    private OptionButtonConfiguration optionButton;

    private BooleanQuestionConfiguration(Builder builder) {
        this.title = builder.title;
        this.optionButton = builder.optionButton;
    }

    public TextConfiguration getTitle() {
        return title;
    }

    public OptionButtonConfiguration getOptionButton() {
        return optionButton;
    }

    /**
     * Please use remote configurations {@link com.glia.widgets.GliaWidgetsConfig.Builder#setUiJsonRemoteConfig(String)}
     */
    @Deprecated
    public static class Builder {
        private final String TAG = BooleanQuestionConfiguration.Builder.class.getSimpleName();

        private TextConfiguration title;
        private OptionButtonConfiguration optionButton;

        /**
         * Use this method to configure the question title style
         *
         * @param title - question title style
         */
        public Builder title(TextConfiguration title) {
            this.title = title;
            return this;
        }

        /**
         * Use this method to configure the question button view style
         *
         * @param optionButton - question button view style
         */
        public Builder optionButton(OptionButtonConfiguration optionButton) {
            this.optionButton = optionButton;
            return this;
        }

        public BooleanQuestionConfiguration build() {
            Logger.logDeprecatedClassUse(BooleanQuestionConfiguration.class.getSimpleName() + "." + TAG);
            ResourceProvider resourceProvider = Dependencies.getResourceProvider();
            if (this.title == null) {
                this.title = prepareDefaultTitleConfiguration(resourceProvider);
            }
            if (this.optionButton == null) {
                this.optionButton = prepareDefaultButtonConfiguration(resourceProvider);
            }
            return new BooleanQuestionConfiguration(this);
        }

        @SuppressLint("ResourceType")
        private OptionButtonConfiguration prepareDefaultButtonConfiguration(ResourceProvider resourceProvider) {
            ColorStateList normalTextColor = resourceProvider.getColorStateList(R.color.glia_base_dark_color);
            ColorStateList selectedTextColor = resourceProvider.getColorStateList(R.color.glia_base_light_color);
            TextConfiguration normalText = new TextConfiguration.Builder()
                    .textColor(normalTextColor)
                    .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_text_size))
                    .build(resourceProvider);
            TextConfiguration selectedText = new TextConfiguration.Builder()
                    .textColor(selectedTextColor)
                    .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_text_size))
                    .build(resourceProvider);
            TextConfiguration highlightedText = new TextConfiguration.Builder()
                    .textColor(normalTextColor)
                    .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_text_size))
                    .build(resourceProvider);

            LayerConfiguration normalLayer = new LayerConfiguration.Builder()
                    .backgroundColor(resourceProvider.getString(R.color.glia_base_light_color))
                    .borderColor(resourceProvider.getString(R.color.glia_stroke_gray))
                    .cornerRadius(resourceProvider.getDimension(R.dimen.glia_survey_default_corner_radius))
                    .build();
            LayerConfiguration selectedLayer = new LayerConfiguration.Builder()
                    .backgroundColor(resourceProvider.getString(R.color.glia_brand_primary_color))
                    .borderColor(resourceProvider.getString(R.color.glia_brand_primary_color))
                    .cornerRadius(resourceProvider.getDimension(R.dimen.glia_survey_default_corner_radius))
                    .build();
            LayerConfiguration highlightedLayer = new LayerConfiguration.Builder()
                    .backgroundColor(resourceProvider.getString(R.color.glia_base_light_color))
                    .borderColor(resourceProvider.getString(R.color.glia_system_negative_color))
                    .cornerRadius(resourceProvider.getDimension(R.dimen.glia_survey_default_corner_radius))
                    .build();
            return new OptionButtonConfiguration.Builder()
                    .normalText(normalText)
                    .normalLayer(normalLayer)
                    .selectedText(selectedText)
                    .selectedLayer(selectedLayer)
                    .highlightedText(highlightedText)
                    .highlightedLayer(highlightedLayer)
                    .build();
        }

        private TextConfiguration prepareDefaultTitleConfiguration(ResourceProvider resourceProvider) {
            ColorStateList normalTextColor = resourceProvider.getColorStateList(R.color.glia_base_dark_color);
            return new TextConfiguration.Builder()
                    .textColor(normalTextColor)
                    .bold(true)
                    .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_title_text_size))
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
        dest.writeParcelable(this.title, flags);
        dest.writeParcelable(this.optionButton, flags);
    }

    public void readFromParcel(Parcel source) {
        this.title = source.readParcelable(TextConfiguration.class.getClassLoader());
        this.optionButton = source.readParcelable(OptionButtonConfiguration.class.getClassLoader());
    }

    protected BooleanQuestionConfiguration(Parcel in) {
        this.title = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.optionButton = in.readParcelable(OptionButtonConfiguration.class.getClassLoader());
    }

    public static final Creator<BooleanQuestionConfiguration> CREATOR = new Creator<BooleanQuestionConfiguration>() {
        @Override
        public BooleanQuestionConfiguration createFromParcel(Parcel source) {
            return new BooleanQuestionConfiguration(source);
        }

        @Override
        public BooleanQuestionConfiguration[] newArray(int size) {
            return new BooleanQuestionConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
