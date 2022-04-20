package com.glia.widgets.view.configuration.survey;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.configuration.TextConfiguration;

public class SingleQuestionConfiguration implements Parcelable {
    private TextConfiguration title;
    private TextConfiguration optionText;

    private SingleQuestionConfiguration(Builder builder) {
        this.title = builder.title;
        this.optionText = builder.optionText;
    }

    public TextConfiguration getTitle() {
        return title;
    }

    public TextConfiguration getOptionText() {
        return optionText;
    }

    public static class Builder {
        private TextConfiguration title;
        private TextConfiguration optionText;

        @SuppressLint("ResourceType")
        public Builder(ResourceProvider resourceProvider) {
            // Default configuration
            ColorStateList titleColor = resourceProvider.getColorStateList(R.color.glia_base_dark_color);
            this.title = new TextConfiguration.Builder(resourceProvider)
                    .textColor(titleColor)
                    .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_text_size))
                    .build();
            ColorStateList optionTextColor = resourceProvider.getColorStateList(R.color.glia_base_dark_color);
            this.optionText = new TextConfiguration.Builder(resourceProvider)
                    .textColor(optionTextColor)
                    .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_text_size))
                    .build();
        }

        public Builder title(TextConfiguration title) {
            this.title = title;
            return this;
        }

        public Builder optionText(TextConfiguration optionText) {
            this.optionText = optionText;
            return this;
        }

        public SingleQuestionConfiguration build() {
            return new SingleQuestionConfiguration(this);
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
        dest.writeParcelable(this.optionText, flags);
    }

    public void readFromParcel(Parcel source) {
        this.title = source.readParcelable(TextConfiguration.class.getClassLoader());
        this.optionText = source.readParcelable(TextConfiguration.class.getClassLoader());
    }

    protected SingleQuestionConfiguration(Parcel in) {
        this.title = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.optionText = in.readParcelable(TextConfiguration.class.getClassLoader());
    }

    public static final Creator<SingleQuestionConfiguration> CREATOR = new Creator<SingleQuestionConfiguration>() {
        @Override
        public SingleQuestionConfiguration createFromParcel(Parcel source) {
            return new SingleQuestionConfiguration(source);
        }

        @Override
        public SingleQuestionConfiguration[] newArray(int size) {
            return new SingleQuestionConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
