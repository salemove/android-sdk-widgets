package com.glia.widgets.view.configuration.survey;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

import com.glia.widgets.R;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.configuration.ButtonConfiguration;
import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;

/**
 * Please use remote configurations {@link com.glia.widgets.GliaWidgetsConfig.Builder#setUiJsonRemoteConfig(String)}
 */
@Deprecated
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

    @Deprecated
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

    /**
     * Please use remote configurations {@link com.glia.widgets.GliaWidgetsConfig.Builder#setUiJsonRemoteConfig(String)}
     */
    @Deprecated
    public static class Builder {
        private final String TAG = SurveyStyle.Builder.class.getSimpleName();

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

        /**
         * Use this method to configure the survey header layer style
         *
         * @param layerConfiguration - header layer style
         */
        public Builder layer(LayerConfiguration layerConfiguration) {
            this.layerConfiguration = layerConfiguration;
            return this;
        }

        /**
         * Use this method to configure the survey header text style
         *
         * @param title - header text style
         */
        public Builder title(TextConfiguration title) {
            this.title = title;
            return this;
        }

        /**
         * Use this method to configure the survey submit button style
         *
         * @param submitButton - submit button style
         */
        public Builder submitButton(ButtonConfiguration submitButton) {
            this.submitButton = submitButton;
            return this;
        }

        /**
         * Use this method to configure the survey cancel button style
         *
         * @param cancelButton - cancel button style
         */
        public Builder cancelButton(ButtonConfiguration cancelButton) {
            this.cancelButton = cancelButton;
            return this;
        }

        /**
         * Use this method to configure the survey "Boolean" question view style
         *
         * @param booleanQuestion - "Boolean" question view style
         */
        public Builder booleanQuestion(BooleanQuestionConfiguration booleanQuestion) {
            this.booleanQuestion = booleanQuestion;
            return this;
        }

        /**
         * Use this method to configure the survey "Scale" question view style
         *
         * @param scaleQuestion - "Scale" question view style
         */
        public Builder scaleQuestion(ScaleQuestionConfiguration scaleQuestion) {
            this.scaleQuestion = scaleQuestion;
            return this;
        }

        /**
         * Use this method to configure the survey "Single" question view style
         *
         * @param singleQuestion - "Single" question view style
         */
        public Builder singleQuestion(SingleQuestionConfiguration singleQuestion) {
            this.singleQuestion = singleQuestion;
            return this;
        }

        /**
         * Use this method to configure the survey "Input" question view style
         *
         * @param inputQuestion - "Input" question view style
         */
        public Builder inputQuestion(InputQuestionConfiguration inputQuestion) {
            this.inputQuestion = inputQuestion;
            return this;
        }

        public SurveyStyle build() {
            Logger.logDeprecatedClassUse(SurveyStyle.class.getSimpleName() + "." + TAG);
            ResourceProvider resourceProvider = Dependencies.getResourceProvider();

            if (this.layerConfiguration == null) {
                this.layerConfiguration = prepareDefaultBackgroundConfiguration(resourceProvider);
            }
            if (this.title == null) {
                this.title = prepareDefaultTitleConfiguration(resourceProvider);
            }
            // Default questions configuration
            if (this.booleanQuestion == null) {
                this.booleanQuestion = new BooleanQuestionConfiguration.Builder().build();
            }
            if (this.inputQuestion == null) {
                this.inputQuestion = new InputQuestionConfiguration.Builder().build();
            }
            if (this.scaleQuestion == null) {
                this.scaleQuestion = new ScaleQuestionConfiguration.Builder().build();
            }
            if (this.singleQuestion == null) {
                this.singleQuestion = new SingleQuestionConfiguration.Builder().build();
            }

            return new SurveyStyle(this);
        }

        private ButtonConfiguration prepareDefaultButtonConfiguration(ResourceProvider resourceProvider,
                                                                      int backgroundColorId) {
            ColorStateList buttonTexColor = resourceProvider.getColorStateList(R.color.glia_light_color);
            TextConfiguration textConfiguration =
                new TextConfiguration.Builder().textColor(buttonTexColor).build(resourceProvider);
            return new ButtonConfiguration.Builder()
                .backgroundColor(resourceProvider.getColorStateList(backgroundColorId))
                .textConfiguration(textConfiguration)
                .build(resourceProvider);
        }

        private TextConfiguration prepareDefaultTitleConfiguration(ResourceProvider resourceProvider) {
            ColorStateList color = resourceProvider.getColorStateList(R.color.glia_dark_color);
            return new TextConfiguration.Builder()
                .textColor(color)
                .textSize(resourceProvider.getDimension(R.dimen.glia_survey_default_survey_title_text_size))
                .build(resourceProvider);
        }

        private LayerConfiguration prepareDefaultBackgroundConfiguration(ResourceProvider resourceProvider) {
            return new LayerConfiguration.Builder()
                .cornerRadius(resourceProvider.getDimension(R.dimen.glia_survey_default_survey_corner_radius))
                .build();
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
