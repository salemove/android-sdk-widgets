package com.glia.widgets.view.configuration.survey;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.view.configuration.FontConfiguration;
import com.glia.widgets.view.configuration.TextRuntimeConfiguration;

public class SurveyScaleOptionConfiguration implements Parcelable {
    private TextRuntimeConfiguration title;
    private String borderColor;
    private String highlightedColor; /* use for border and not-selected option on validation */
    private FontConfiguration textFieldFont;

    private SurveyScaleOptionConfiguration(Builder builder) {
        this.title = builder.title;
        this.borderColor = builder.borderColor;
        this.highlightedColor = builder.highlightedColor;
        this.textFieldFont = builder.textFieldFont;
    }

    public TextRuntimeConfiguration getTitle() {
        return title;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public String getHighlightedColor() {
        return highlightedColor;
    }

    public FontConfiguration getTextFieldFont() {
        return textFieldFont;
    }

    public static class Builder {
        private TextRuntimeConfiguration title;
        private String borderColor;
        private String highlightedColor; /* use for border and not-selected option on validation */
        private FontConfiguration textFieldFont;

        public Builder(){
        }

        public Builder title(TextRuntimeConfiguration title) {
            this.title = title;
            return this;
        }

        public Builder borderColor(String borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        public Builder highlightedColor(String highlightedColor) {
            this.highlightedColor = highlightedColor;
            return this;
        }

        public Builder textFieldFont(FontConfiguration textFieldFont) {
            this.textFieldFont = textFieldFont;
            return this;
        }

        public SurveyScaleOptionConfiguration build() {
            return new SurveyScaleOptionConfiguration(this);
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
        dest.writeString(this.borderColor);
        dest.writeString(this.highlightedColor);
        dest.writeParcelable(this.textFieldFont, flags);
    }

    public void readFromParcel(Parcel source) {
        this.title = source.readParcelable(TextRuntimeConfiguration.class.getClassLoader());
        this.borderColor = source.readString();
        this.highlightedColor = source.readString();
        this.textFieldFont = source.readParcelable(FontConfiguration.class.getClassLoader());
    }

    protected SurveyScaleOptionConfiguration(Parcel in) {
        this.title = in.readParcelable(TextRuntimeConfiguration.class.getClassLoader());
        this.borderColor = in.readString();
        this.highlightedColor = in.readString();
        this.textFieldFont = in.readParcelable(FontConfiguration.class.getClassLoader());
    }

    public static final Creator<SurveyScaleOptionConfiguration> CREATOR = new Creator<SurveyScaleOptionConfiguration>() {
        @Override
        public SurveyScaleOptionConfiguration createFromParcel(Parcel source) {
            return new SurveyScaleOptionConfiguration(source);
        }

        @Override
        public SurveyScaleOptionConfiguration[] newArray(int size) {
            return new SurveyScaleOptionConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
