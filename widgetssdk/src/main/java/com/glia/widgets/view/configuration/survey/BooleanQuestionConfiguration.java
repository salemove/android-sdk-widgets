package com.glia.widgets.view.configuration.survey;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.OptionButtonConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;

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

    public static class Builder {
        private TextConfiguration title;
        private OptionButtonConfiguration optionButton;

        @SuppressLint("ResourceType")
        public Builder(ResourceProvider resourceProvider){
            // Default configuration
            ColorStateList normalTextColor = resourceProvider.getColorStateList(R.color.glia_base_dark_color);
            this.title = new TextConfiguration.Builder(resourceProvider)
                    .textColor(normalTextColor)
                    .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_title_text_size))
                    .build();

            ColorStateList selectedTextColor = resourceProvider.getColorStateList(R.color.glia_base_light_color);
            TextConfiguration normalText = new TextConfiguration.Builder(resourceProvider)
                    .textColor(normalTextColor)
                    .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_text_size))
                    .build();
            TextConfiguration selectedText = new TextConfiguration.Builder(resourceProvider)
                    .textColor(selectedTextColor)
                    .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_text_size))
                    .build();
            TextConfiguration highlightedText = new TextConfiguration.Builder(resourceProvider)
                    .textColor(normalTextColor)
                    .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_text_size))
                    .build();

            LayerConfiguration normalLayer = new LayerConfiguration.Builder(resourceProvider)
                    .backgroundColor(resourceProvider.getString(R.color.glia_base_light_color))
                    .borderColor(resourceProvider.getString(R.color.glia_stroke_gray))
                    .cornerRadius(resourceProvider.getDimension(R.dimen.glia_survey_default_corner_radius))
                    .build();
            LayerConfiguration selectedLayer = new LayerConfiguration.Builder(resourceProvider)
                    .backgroundColor(resourceProvider.getString(R.color.glia_brand_primary_color))
                    .borderColor(resourceProvider.getString(R.color.glia_brand_primary_color))
                    .cornerRadius(resourceProvider.getDimension(R.dimen.glia_survey_default_corner_radius))
                    .build();
            LayerConfiguration highlightedLayer = new LayerConfiguration.Builder(resourceProvider)
                    .backgroundColor(resourceProvider.getString(R.color.glia_base_light_color))
                    .borderColor(resourceProvider.getString(R.color.glia_system_negative_color))
                    .cornerRadius(resourceProvider.getDimension(R.dimen.glia_survey_default_corner_radius))
                    .build();

            this.optionButton = new OptionButtonConfiguration.Builder(resourceProvider)
                    .normalText(normalText)
                    .normalLayer(normalLayer)
                    .selectedText(selectedText)
                    .selectedLayer(selectedLayer)
                    .highlightedText(highlightedText)
                    .highlightedLayer(highlightedLayer)
                    .build();
        }

        public Builder title(TextConfiguration title) {
            this.title = title;
            return this;
        }

        public Builder optionButton(OptionButtonConfiguration optionButton) {
            this.optionButton = optionButton;
            return this;
        }

        public BooleanQuestionConfiguration build() {
            return new BooleanQuestionConfiguration(this);
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
