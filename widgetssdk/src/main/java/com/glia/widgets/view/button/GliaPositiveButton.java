package com.glia.widgets.view.button;

import android.content.Context;
import android.util.AttributeSet;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.configuration.ButtonConfiguration;

/**
 * @hide
 */
public class GliaPositiveButton extends BaseConfigurableButton {
    @Override
    public ButtonConfiguration getButtonConfigurationFromTheme(UiTheme theme) {
        return theme.getGliaPositiveButtonConfiguration();
    }

    public GliaPositiveButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.buttonBarPositiveButtonStyle);
    }
}
