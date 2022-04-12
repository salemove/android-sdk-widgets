package com.glia.widgets.view.configuration.survey;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.configuration.FontConfiguration;
import com.glia.widgets.view.configuration.TextRuntimeConfiguration;

public class SurveySingleOptionConfiguration implements Parcelable {
    private TextRuntimeConfiguration title;
    private String highlightedColor; /* use for border and not-selected option on validation */
    private FontConfiguration textFieldFont;

    private SurveySingleOptionConfiguration(Builder builder) {
        this.title = builder.title;
        this.highlightedColor = builder.highlightedColor;
        this.textFieldFont = builder.textFieldFont;
    }

    public TextRuntimeConfiguration getTitle() {
        return title;
    }

    public String getHighlightedColor() {
        return highlightedColor;
    }

    public FontConfiguration getTextFieldFont() {
        return textFieldFont;
    }

    public static class Builder {
        private TextRuntimeConfiguration title;
        private String highlightedColor; /* use for border and not-selected option on validation */
        private FontConfiguration textFieldFont;

        @SuppressLint("ResourceType")
        public Builder(ResourceProvider resourceProvider) {
            FontConfiguration titleFont = new FontConfiguration(
                    FontConfiguration.FontSize.BODY,
                    FontConfiguration.FontWeight.MEDIUM);
            String titleColorString = resourceProvider.getString(R.color.glia_base_dark_color);
            this.title = new TextRuntimeConfiguration(
                    titleFont,
                    titleColorString,
                    titleColorString);
            this.highlightedColor = resourceProvider.getString(R.color.glia_system_negative_color);
            this.textFieldFont = new FontConfiguration(
                    FontConfiguration.FontSize.BODY,
                    FontConfiguration.FontWeight.REGULAR);
        }

        public Builder title(TextRuntimeConfiguration title) {
            this.title = title;
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

        public SurveySingleOptionConfiguration build() {
            return new SurveySingleOptionConfiguration(this);
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
        dest.writeString(this.highlightedColor);
        dest.writeParcelable(this.textFieldFont, flags);
    }

    public void readFromParcel(Parcel source) {
        this.title = source.readParcelable(TextRuntimeConfiguration.class.getClassLoader());
        this.highlightedColor = source.readString();
        this.textFieldFont = source.readParcelable(FontConfiguration.class.getClassLoader());
    }

    protected SurveySingleOptionConfiguration(Parcel in) {
        this.title = in.readParcelable(TextRuntimeConfiguration.class.getClassLoader());
        this.highlightedColor = in.readString();
        this.textFieldFont = in.readParcelable(FontConfiguration.class.getClassLoader());
    }

    public static final Creator<SurveySingleOptionConfiguration> CREATOR = new Creator<SurveySingleOptionConfiguration>() {
        @Override
        public SurveySingleOptionConfiguration createFromParcel(Parcel source) {
            return new SurveySingleOptionConfiguration(source);
        }

        @Override
        public SurveySingleOptionConfiguration[] newArray(int size) {
            return new SurveySingleOptionConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
