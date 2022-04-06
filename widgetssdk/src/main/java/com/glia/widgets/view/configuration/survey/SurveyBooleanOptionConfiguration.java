package com.glia.widgets.view.configuration.survey;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.view.configuration.FontConfiguration;
import com.glia.widgets.view.configuration.Text;

public class SurveyBooleanOptionConfiguration implements Parcelable {
    public Text title;
    public String selectedColor;    /* use for border and background for selected option */
    public String highlightedColor; /* use for border and not-selected option on validation */
    public FontConfiguration font;

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
        this.title = source.readParcelable(Text.class.getClassLoader());
        this.selectedColor = source.readString();
        this.highlightedColor = source.readString();
        this.font = source.readParcelable(FontConfiguration.class.getClassLoader());
    }

    public SurveyBooleanOptionConfiguration() {
    }

    protected SurveyBooleanOptionConfiguration(Parcel in) {
        this.title = in.readParcelable(Text.class.getClassLoader());
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
