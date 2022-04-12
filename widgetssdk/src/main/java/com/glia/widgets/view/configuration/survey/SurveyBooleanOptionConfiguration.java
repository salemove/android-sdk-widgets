package com.glia.widgets.view.configuration.survey;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.configuration.FontConfiguration;
import com.glia.widgets.view.configuration.TextRuntimeConfiguration;

public class SurveyBooleanOptionConfiguration implements Parcelable {
    private TextRuntimeConfiguration title;
    private String selectedColor;    /* use for border and background for selected option */
    private String highlightedColor; /* use for border and not-selected option on validation */
    private FontConfiguration font;

    private SurveyBooleanOptionConfiguration(Builder builder) {
        this.title = builder.title;
        this.selectedColor = builder.selectedColor;
        this.highlightedColor = builder.highlightedColor;
        this.font = builder.font;
    }

    public TextRuntimeConfiguration getTitle() {
        return title;
    }

    public String getSelectedColor() {
        return selectedColor;
    }

    public String getHighlightedColor() {
        return highlightedColor;
    }

    public FontConfiguration getFont() {
        return font;
    }

    public static class Builder {
        private TextRuntimeConfiguration title;
        private String selectedColor;    /* use for border and background for selected option */
        private String highlightedColor; /* use for border and not-selected option on validation */
        private FontConfiguration font;

        @SuppressLint("ResourceType")
        public Builder(ResourceProvider resourceProvider){
            FontConfiguration titleFont = new FontConfiguration(
                    FontConfiguration.FontSize.BODY,
                    FontConfiguration.FontWeight.MEDIUM);
            String titleColorString = resourceProvider.getString(R.color.glia_base_dark_color);
            this.title = new TextRuntimeConfiguration(
                    titleFont,
                    titleColorString,
                    titleColorString);
            this.selectedColor = resourceProvider.getString(R.color.glia_brand_primary_color);
            this.highlightedColor = resourceProvider.getString(R.color.glia_system_negative_color);
            this.font = new FontConfiguration(
                    FontConfiguration.FontSize.BODY,
                    FontConfiguration.FontWeight.REGULAR);
        }

        public Builder title(TextRuntimeConfiguration title) {
            this.title = title;
            return this;
        }

        public Builder selectedColor(String selectedColor) {
            this.selectedColor = selectedColor;
            return this;
        }

        public Builder highlightedColor(String highlightedColor) {
            this.highlightedColor = highlightedColor;
            return this;
        }

        public Builder font(FontConfiguration font) {
            this.font = font;
            return this;
        }

        public SurveyBooleanOptionConfiguration build() {
            return new SurveyBooleanOptionConfiguration(this);
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
        dest.writeString(this.selectedColor);
        dest.writeString(this.highlightedColor);
        dest.writeParcelable(this.font, flags);
    }

    public void readFromParcel(Parcel source) {
        this.title = source.readParcelable(TextRuntimeConfiguration.class.getClassLoader());
        this.selectedColor = source.readString();
        this.highlightedColor = source.readString();
        this.font = source.readParcelable(FontConfiguration.class.getClassLoader());
    }

    protected SurveyBooleanOptionConfiguration(Parcel in) {
        this.title = in.readParcelable(TextRuntimeConfiguration.class.getClassLoader());
        this.selectedColor = in.readString();
        this.highlightedColor = in.readString();
        this.font = in.readParcelable(FontConfiguration.class.getClassLoader());
    }

    public static final Creator<SurveyBooleanOptionConfiguration> CREATOR = new Creator<SurveyBooleanOptionConfiguration>() {
        @Override
        public SurveyBooleanOptionConfiguration createFromParcel(Parcel source) {
            return new SurveyBooleanOptionConfiguration(source);
        }

        @Override
        public SurveyBooleanOptionConfiguration[] newArray(int size) {
            return new SurveyBooleanOptionConfiguration[size];
        }
    };
    /* END: Parcelable related */
}
