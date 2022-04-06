package com.glia.widgets.view.configuration.survey;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.view.configuration.FontConfiguration;
import com.glia.widgets.view.configuration.Text;

public class SurveyScaleOptionConfiguration implements Parcelable {
    public Text title;
    public String borderColor;
    public String highlightedColor; /* use for border and not-selected option on validation */
    public FontConfiguration textFieldFont;

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
        this.title = source.readParcelable(Text.class.getClassLoader());
        this.borderColor = source.readString();
        this.highlightedColor = source.readString();
        this.textFieldFont = source.readParcelable(FontConfiguration.class.getClassLoader());
    }

    public SurveyScaleOptionConfiguration() {
    }

    protected SurveyScaleOptionConfiguration(Parcel in) {
        this.title = in.readParcelable(Text.class.getClassLoader());
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
