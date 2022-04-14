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

        @SuppressLint("ResourceType")
        public Builder(ResourceProvider resourceProvider) {
            // Default configuration
            ColorStateList color = resourceProvider.getColorStateList(R.color.glia_base_dark_color);
            this.title = new TextConfiguration.Builder().textColor(color).build();

            String normalColor = resourceProvider.getString(R.color.glia_stroke_gray);
            LayerConfiguration normalLayer = new LayerConfiguration.Builder(resourceProvider)
                    .borderColor(normalColor)
                    .build();

            String errorColor = resourceProvider.getString(R.color.glia_system_negative_color);
            LayerConfiguration errorLayer = new LayerConfiguration.Builder(resourceProvider)
                    .borderColor(errorColor)
                    .build();

            this.optionButton = new OptionButtonConfiguration.Builder(resourceProvider)
                    .normalLayer(normalLayer)
                    .highlightedLayer(errorLayer)
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

        public InputQuestionConfiguration build() {
            return new InputQuestionConfiguration(this);
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
