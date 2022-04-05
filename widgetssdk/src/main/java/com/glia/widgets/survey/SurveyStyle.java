package com.glia.widgets.survey;

import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.configuration.SurveyBooleanOption;
import com.glia.widgets.view.configuration.SurveyInputOption;
import com.glia.widgets.view.configuration.SurveyScaleOption;
import com.glia.widgets.view.configuration.SurveySingleOption;
import com.glia.widgets.view.configuration.Text;

public class SurveyStyle implements Parcelable {
    private String bgColor;
    private Text title;
    private SurveyBooleanOption booleanOption;
    private SurveyScaleOption scaleOption;
    private SurveySingleOption singleOption;
    private SurveyInputOption inputOption;

    public String getBgColor() {
        return bgColor;
    }

    public Text getTitle() {
        return title;
    }

    public SurveyBooleanOption getBooleanOption() {
        return booleanOption;
    }

    public SurveyScaleOption getScaleOption() {
        return scaleOption;
    }

    public SurveySingleOption getSingleOption() {
        return singleOption;
    }

    public SurveyInputOption getInputOption() {
        return inputOption;
    }

    private SurveyStyle(
            Builder builder
    ) {
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
        private SurveyBooleanOption booleanOption;
        private SurveyScaleOption scaleOption;
        private SurveySingleOption singleOption;
        private SurveyInputOption inputOption;

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

        public Builder booleanOption(SurveyBooleanOption booleanOption) {
            this.booleanOption = booleanOption;
            return this;
        }

        public Builder scaleOption(SurveyScaleOption scaleOption) {
            this.scaleOption = scaleOption;
            return this;
        }

        public Builder singleOption(SurveySingleOption singleOption) {
            this.singleOption = singleOption;
            return this;
        }

        public Builder inputOption(SurveyInputOption inputOption) {
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
        this.booleanOption = source.readParcelable(SurveyBooleanOption.class.getClassLoader());
        this.scaleOption = source.readParcelable(SurveyScaleOption.class.getClassLoader());
        this.singleOption = source.readParcelable(SurveySingleOption.class.getClassLoader());
        this.inputOption = source.readParcelable(SurveyInputOption.class.getClassLoader());
    }

    protected SurveyStyle(Parcel in) {
        this.bgColor = in.readString();
        this.title = in.readParcelable(Text.class.getClassLoader());
        this.booleanOption = in.readParcelable(SurveyBooleanOption.class.getClassLoader());
        this.scaleOption = in.readParcelable(SurveyScaleOption.class.getClassLoader());
        this.singleOption = in.readParcelable(SurveySingleOption.class.getClassLoader());
        this.inputOption = in.readParcelable(SurveyInputOption.class.getClassLoader());
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
