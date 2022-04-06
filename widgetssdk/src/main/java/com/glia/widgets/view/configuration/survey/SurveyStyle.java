package com.glia.widgets.view.configuration.survey;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.configuration.Text;

public class SurveyStyle implements Parcelable {
    private String bgColor;
    private Text title;
    private SurveyBooleanOptionConfiguration booleanOption;
    private SurveyScaleOptionConfiguration scaleOption;
    private SurveySingleOptionConfiguration singleOption;
    private SurveyInputOptionConfiguration inputOption;

    public String getBgColor() {
        return bgColor;
    }

    public Text getTitle() {
        return title;
    }

    public SurveyBooleanOptionConfiguration getBooleanOption() {
        return booleanOption;
    }

    public SurveyScaleOptionConfiguration getScaleOption() {
        return scaleOption;
    }

    public SurveySingleOptionConfiguration getSingleOption() {
        return singleOption;
    }

    public SurveyInputOptionConfiguration getInputOption() {
        return inputOption;
    }

    private SurveyStyle(Builder builder) {
        this.bgColor = builder.bgColor;
        this.title = builder.title;
        this.booleanOption = builder.booleanOption;
        this.scaleOption = builder.scaleOption;
        this.singleOption = builder.singleOption;
        this.inputOption = builder.inputOption;
    }

    public static class Builder {
        private String bgColor;
        private Text title;
        private SurveyBooleanOptionConfiguration booleanOption;
        private SurveyScaleOptionConfiguration scaleOption;
        private SurveySingleOptionConfiguration singleOption;
        private SurveyInputOptionConfiguration inputOption;

        public Builder(ResourceProvider resourceProvider) {
            // Default values
            this.bgColor = resourceProvider.getString(R.color.glia_base_light_color);
        }

        public Builder bgColor(String bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public Builder title(Text title) {
            this.title = title;
            return this;
        }

        public Builder booleanOption(SurveyBooleanOptionConfiguration booleanOption) {
            this.booleanOption = booleanOption;
            return this;
        }

        public Builder scaleOption(SurveyScaleOptionConfiguration scaleOption) {
            this.scaleOption = scaleOption;
            return this;
        }

        public Builder singleOption(SurveySingleOptionConfiguration singleOption) {
            this.singleOption = singleOption;
            return this;
        }

        public Builder inputOption(SurveyInputOptionConfiguration inputOption) {
            this.inputOption = inputOption;
            return this;
        }

        public SurveyStyle build() {
            return new SurveyStyle(this);
        }
    }

    /* BEGIN: Parcelable related */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.bgColor);
        dest.writeParcelable(this.title, flags);
        dest.writeParcelable(this.booleanOption, flags);
        dest.writeParcelable(this.scaleOption, flags);
        dest.writeParcelable(this.singleOption, flags);
        dest.writeParcelable(this.inputOption, flags);
    }

    public void readFromParcel(Parcel source) {
        this.bgColor = source.readString();
        this.title = source.readParcelable(Text.class.getClassLoader());
        this.booleanOption = source.readParcelable(SurveyBooleanOptionConfiguration.class.getClassLoader());
        this.scaleOption = source.readParcelable(SurveyScaleOptionConfiguration.class.getClassLoader());
        this.singleOption = source.readParcelable(SurveySingleOptionConfiguration.class.getClassLoader());
        this.inputOption = source.readParcelable(SurveyInputOptionConfiguration.class.getClassLoader());
    }

    protected SurveyStyle(Parcel in) {
        this.bgColor = in.readString();
        this.title = in.readParcelable(Text.class.getClassLoader());
        this.booleanOption = in.readParcelable(SurveyBooleanOptionConfiguration.class.getClassLoader());
        this.scaleOption = in.readParcelable(SurveyScaleOptionConfiguration.class.getClassLoader());
        this.singleOption = in.readParcelable(SurveySingleOptionConfiguration.class.getClassLoader());
        this.inputOption = in.readParcelable(SurveyInputOptionConfiguration.class.getClassLoader());
    }

    public static final Creator<SurveyStyle> CREATOR = new Creator<SurveyStyle>() {
        @Override
        public SurveyStyle createFromParcel(Parcel source) {
            return new SurveyStyle(source);
        }

        @Override
        public SurveyStyle[] newArray(int size) {
            return new SurveyStyle[size];
        }
    };
    /* END: Parcelable related */
}
