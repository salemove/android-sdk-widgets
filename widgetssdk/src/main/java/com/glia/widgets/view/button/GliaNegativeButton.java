package com.glia.widgets.view.button;

import android.content.Context;
import android.util.AttributeSet;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.configuration.ButtonConfiguration;

public class GliaNegativeButton extends BaseConfigurableButton {
    @Override
    public ButtonConfiguration getButtonConfigurationFromTheme(UiTheme theme) {
        return theme.getGliaNegativeButtonConfiguration();
    }

    public GliaNegativeButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.buttonBarNegativeButtonStyle);
    }
}
