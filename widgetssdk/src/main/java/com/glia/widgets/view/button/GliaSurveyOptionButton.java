package com.glia.widgets.view.button;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.configuration.ButtonConfiguration;
import com.glia.widgets.view.configuration.survey.SurveyStyle;

public class GliaSurveyOptionButton extends BaseConfigurableButton {
    private boolean isError = false;
    private SurveyStyle surveyStyle;

    @Override
    public ButtonConfiguration getButtonConfigurationFromTheme(UiTheme theme) {
        return theme.getGliaNeutralButtonConfiguration();
    }

    public GliaSurveyOptionButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.buttonSurveyOptionButtonStyle);
    }

    public void setError(boolean error) {
        isError = error;
        applyView();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        applyView();
    }

    public void setSurveyStyle(SurveyStyle surveyStyle) {
        this.surveyStyle = surveyStyle;
    }

    private void applyView() {
        ColorStateList strokeColor =
                isError ? ContextCompat.getColorStateList(getContext(), R.color.glia_system_negative_color) :
                        isSelected() ?
                                ContextCompat.getColorStateList(getContext(), R.color.glia_brand_primary_color) :
                                ContextCompat.getColorStateList(getContext(), R.color.glia_stroke_gray);

        int backgroundColor = isSelected() ?
                ContextCompat.getColor(getContext(), R.color.glia_brand_primary_color) :
                surveyStyle != null ?
                        Color.parseColor(surveyStyle.getBgColor()) :
                        ContextCompat.getColor(getContext(), R.color.glia_base_light_color);

        int textColor = isSelected() ?
                ContextCompat.getColor(getContext(), R.color.glia_base_light_color) :
                ContextCompat.getColor(getContext(), R.color.glia_base_dark_color);

        setStrokeColor(strokeColor);
        setBackgroundColor(backgroundColor);
        setTextColor(textColor);
    }
}
