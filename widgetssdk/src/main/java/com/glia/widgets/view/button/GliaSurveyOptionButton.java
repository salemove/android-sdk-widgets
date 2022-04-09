package com.glia.widgets.view.button;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.configuration.ButtonConfiguration;

public class GliaSurveyOptionButton extends BaseConfigurableButton {
    private boolean isError = false;

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

    private void applyView() {
        // TODO: get colors from theme
        ColorStateList strokeColor =
                isError ? ContextCompat.getColorStateList(getContext(), R.color.glia_system_negative_color) :
                        isSelected() ?
                                ContextCompat.getColorStateList(getContext(), R.color.glia_brand_primary_color) :
                                ContextCompat.getColorStateList(getContext(), R.color.glia_stroke_gray);

        ColorStateList backgroundColor =
                isSelected() ?
                        ContextCompat.getColorStateList(getContext(), R.color.glia_brand_primary_color) :
                        ContextCompat.getColorStateList(getContext(), R.color.glia_base_light_color);

        ColorStateList textColor =
                isSelected() ?
                        ContextCompat.getColorStateList(getContext(), R.color.glia_base_light_color) :
                        ContextCompat.getColorStateList(getContext(), R.color.glia_base_dark_color);

        setStrokeColor(strokeColor);
        setBackgroundTintList(backgroundColor);
        setTextColor(textColor);
    }
}
