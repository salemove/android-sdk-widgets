package com.glia.widgets.view.header.button;

import android.content.Context;
import android.util.AttributeSet;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.button.BaseConfigurableButton;
import com.glia.widgets.view.configuration.ButtonConfiguration;

public class GliaEndButton extends BaseConfigurableButton {
    @Override
    public ButtonConfiguration getButtonConfigurationFromTheme(UiTheme theme) {
        return theme.getGliaEndButtonConfiguration();
    }

    public GliaEndButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.gliaHeaderEndButtonStyle);
    }
}
