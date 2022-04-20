package com.glia.widgets.view.configuration.survey;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.configuration.ButtonConfiguration;
import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;

public class SurveyStyle implements Parcelable {
    // Layer style.
    private LayerConfiguration layer;
    // Header text style.
    private TextConfiguration title;
    // Submit button style.
    private ButtonConfiguration submitButton;
    // Cancel button style.
    private ButtonConfiguration cancelButton;
    // "Boolean" question view style.
    private BooleanQuestionConfiguration booleanQuestion;
    // "Scale" question view style.
    private ScaleQuestionConfiguration scaleQuestion;
    // "Single" question view style.
    private SingleQuestionConfiguration singleQuestion;
    // "Input" question view style.
    private InputQuestionConfiguration inputQuestion;

    public LayerConfiguration getLayer() {
        return layer;
    }

    public TextConfiguration getTitle() {
        return title;
    }

    public ButtonConfiguration getSubmitButton() {
        return submitButton;
    }

    public ButtonConfiguration getCancelButton() {
        return cancelButton;
    }

    public BooleanQuestionConfiguration getBooleanQuestion() {
        return booleanQuestion;
    }

    public ScaleQuestionConfiguration getScaleQuestion() {
        return scaleQuestion;
    }

    public SingleQuestionConfiguration getSingleQuestion() {
        return singleQuestion;
    }

    public InputQuestionConfiguration getInputQuestion() {
        return inputQuestion;
    }

    private SurveyStyle(Builder builder) {
        this.layer = builder.layerConfiguration;
        this.title = builder.title;
        this.submitButton = builder.submitButton;
        this.cancelButton = builder.cancelButton;
        this.booleanQuestion = builder.booleanQuestion;
        this.scaleQuestion = builder.scaleQuestion;
        this.singleQuestion = builder.singleQuestion;
        this.inputQuestion = builder.inputQuestion;
    }

    public static class Builder {
        // Layer style.
        private LayerConfiguration layerConfiguration;
        // Header text style.
        private TextConfiguration title;
        // Submit button style.
        private ButtonConfiguration submitButton;
        // Cancel button style.
        private ButtonConfiguration cancelButton;
        // "Boolean" question view style.
        private BooleanQuestionConfiguration booleanQuestion;
        // "Scale" question view style.
        private ScaleQuestionConfiguration scaleQuestion;
        // "Single" question view style.
        private SingleQuestionConfiguration singleQuestion;
        // "Input" question view style.
        private InputQuestionConfiguration inputQuestion;

        @SuppressLint("ResourceType")
        public Builder(ResourceProvider resourceProvider) {
            // Default survey background
            this.layerConfiguration = new LayerConfiguration.Builder(resourceProvider)
                    .cornerRadius(resourceProvider.getDimension(R.dimen.glia_survey_default_survey_corner_radius))
                    .build();

            // Default survey title configuration
            ColorStateList color = resourceProvider.getColorStateList(R.color.glia_base_dark_color);
            this.title = new TextConfiguration.Builder(resourceProvider)
                    .textColor(color)
                    .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_survey_title_text_size))
                    .build();

            // Default buttons configuration
            ColorStateList buttonTexColor = resourceProvider.getColorStateList(R.color.glia_base_light_color);
            TextConfiguration textConfiguration =
                    new TextConfiguration.Builder(resourceProvider).textColor(buttonTexColor).build();
            this.submitButton = new ButtonConfiguration.Builder()
                    .backgroundColor(resourceProvider.getColorStateList(R.color.glia_brand_primary_color))
                    .textConfiguration(textConfiguration)
                    .build();
            this.cancelButton = new ButtonConfiguration.Builder()
                    .backgroundColor(resourceProvider.getColorStateList(R.color.glia_system_negative_color))
                    .textConfiguration(textConfiguration)
                    .build();

            // Default questions configuration
            this.booleanQuestion = new BooleanQuestionConfiguration.Builder(resourceProvider).build();
            this.inputQuestion = new InputQuestionConfiguration.Builder(resourceProvider).build();
            this.scaleQuestion = new ScaleQuestionConfiguration.Builder(resourceProvider).build();
            this.singleQuestion = new SingleQuestionConfiguration.Builder(resourceProvider).build();
        }

        public Builder layer(LayerConfiguration layerConfiguration) {
            this.layerConfiguration = layerConfiguration;
            return this;
        }

        public Builder title(TextConfiguration title) {
            this.title = title;
            return this;
        }

        public Builder submitButton(ButtonConfiguration submitButton) {
            this.submitButton = submitButton;
            return this;
        }

        public Builder cancelButton(ButtonConfiguration cancelButton) {
            this.cancelButton = cancelButton;
            return this;
        }

        public Builder booleanQuestion(BooleanQuestionConfiguration booleanQuestion) {
            this.booleanQuestion = booleanQuestion;
            return this;
        }

        public Builder scaleQuestion(ScaleQuestionConfiguration scaleQuestion) {
            this.scaleQuestion = scaleQuestion;
            return this;
        }

        public Builder singleQuestion(SingleQuestionConfiguration singleQuestion) {
            this.singleQuestion = singleQuestion;
            return this;
        }

        public Builder inputQuestion(InputQuestionConfiguration inputQuestion) {
            this.inputQuestion = inputQuestion;
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
        dest.writeParcelable(this.layer, flags);
        dest.writeParcelable(this.title, flags);
        dest.writeParcelable(this.submitButton, flags);
        dest.writeParcelable(this.cancelButton, flags);
        dest.writeParcelable(this.booleanQuestion, flags);
        dest.writeParcelable(this.scaleQuestion, flags);
        dest.writeParcelable(this.singleQuestion, flags);
        dest.writeParcelable(this.inputQuestion, flags);
    }

    public void readFromParcel(Parcel source) {
        this.layer = source.readParcelable(LayerConfiguration.class.getClassLoader());
        this.title = source.readParcelable(TextConfiguration.class.getClassLoader());
        this.submitButton = source.readParcelable(ButtonConfiguration.class.getClassLoader());
        this.cancelButton = source.readParcelable(ButtonConfiguration.class.getClassLoader());
        this.booleanQuestion = source.readParcelable(BooleanQuestionConfiguration.class.getClassLoader());
        this.scaleQuestion = source.readParcelable(ScaleQuestionConfiguration.class.getClassLoader());
        this.singleQuestion = source.readParcelable(SingleQuestionConfiguration.class.getClassLoader());
        this.inputQuestion = source.readParcelable(InputQuestionConfiguration.class.getClassLoader());
    }

    protected SurveyStyle(Parcel in) {
        this.layer = in.readParcelable(LayerConfiguration.class.getClassLoader());
        this.title = in.readParcelable(TextConfiguration.class.getClassLoader());
        this.submitButton = in.readParcelable(ButtonConfiguration.class.getClassLoader());
        this.cancelButton = in.readParcelable(ButtonConfiguration.class.getClassLoader());
        this.booleanQuestion = in.readParcelable(BooleanQuestionConfiguration.class.getClassLoader());
        this.scaleQuestion = in.readParcelable(ScaleQuestionConfiguration.class.getClassLoader());
        this.singleQuestion = in.readParcelable(SingleQuestionConfiguration.class.getClassLoader());
        this.inputQuestion = in.readParcelable(InputQuestionConfiguration.class.getClassLoader());
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
