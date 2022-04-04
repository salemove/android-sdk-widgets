package com.glia.widgets.view.button;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.configuration.ButtonConfiguration;

public class GliaSurveyOptionButton extends BaseConfigurableButton {
    @Override
    public ButtonConfiguration getButtonConfigurationFromTheme(UiTheme theme) {
        return theme.getGliaNeutralButtonConfiguration();
    }

    public GliaSurveyOptionButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.buttonSurveyOptionButtonStyle);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        // TODO: get colors from theme
        ColorStateList actionButtonBackgroundColor =
                selected ?
                        ContextCompat.getColorStateList(getContext(), R.color.glia_brand_primary_color) :
                        ContextCompat.getColorStateList(getContext(), R.color.glia_system_agent_bubble_color);

        ColorStateList actionButtonTextColor =
                selected ?
                        ContextCompat.getColorStateList(getContext(), R.color.glia_base_light_color) :
                        ContextCompat.getColorStateList(getContext(), R.color.glia_base_dark_color);

        setBackgroundTintList(actionButtonBackgroundColor);
        setTextColor(actionButtonTextColor);
    }
}
