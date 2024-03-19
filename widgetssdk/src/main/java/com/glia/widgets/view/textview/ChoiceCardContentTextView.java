package com.glia.widgets.view.textview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.configuration.TextConfiguration;

/**
 * @hide
 */
public class ChoiceCardContentTextView extends BaseConfigurableTextView {
    public ChoiceCardContentTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, R.attr.choiceCardContentTextStyle, R.style.Application_Glia_ChoiceCard_ContentText);
    }

    @Override
    public TextConfiguration getTextConfigurationFromTheme(UiTheme theme) {
        return theme.getGliaChoiceCardContentTextConfiguration();
    }
}
