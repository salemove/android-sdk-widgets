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

public class InputQuestionConfiguration implements Parcelable {
    private TextConfiguration title;
    private OptionButtonConfiguration optionButton;

    private InputQuestionConfiguration(Builder builder) {
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

        public Builder title(TextConfiguration title) {
            this.title = title;
            return this;
        }

        public Builder optionButton(OptionButtonConfiguration optionButton) {
            this.optionButton = optionButton;
            return this;
        }

        public InputQuestionConfiguration build(ResourceProvider resourceProvider) {
            if (this.title == null) {
                this.title = prepareDefaultTitleConfiguration(resourceProvider);
            }
            if (this.optionButton == null) {
                this.optionButton = prepareDefaultButtonConfiguration(resourceProvider);
            }
            return new InputQuestionConfiguration(this);
        }

        @SuppressLint("ResourceType")
        private OptionButtonConfiguration prepareDefaultButtonConfiguration(ResourceProvider resourceProvider) {
            String normalColor = resourceProvider.getString(R.color.glia_stroke_gray);
            LayerConfiguration normalLayer = new LayerConfiguration.Builder()
                    .borderColor(normalColor)
                    .build(resourceProvider);

            String errorColor = resourceProvider.getString(R.color.glia_system_negative_color);
            LayerConfiguration errorLayer = new LayerConfiguration.Builder()
                    .borderColor(errorColor)
                    .build(resourceProvider);
            return new OptionButtonConfiguration.Builder()
                    .normalLayer(normalLayer)
                    .highlightedLayer(errorLayer)
                    .build(resourceProvider);
        }

        private TextConfiguration prepareDefaultTitleConfiguration(ResourceProvider resourceProvider) {
            ColorStateList color = resourceProvider.getColorStateList(R.color.glia_base_dark_color);
            return new TextConfiguration.Builder()
                    .textColor(color)
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

    protected InputQuestionConfiguration(Parcel in) {
        this.title = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.optionButton = in.readParcelable(OptionButtonConfiguration.class.getClassLoader());
    }

    public static final Creator<InputQuestionConfiguration> CREATOR = new Creator<InputQuestionConfiguration>() {
        @Override
        public InputQuestionConfiguration createFromParcel(Parcel source) {
            return new InputQuestionConfiguration(source);
        }

        @Override
        public InputQuestionConfiguration[] newArray(int size) {
            return new InputQuestionConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
