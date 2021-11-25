package com.glia.widgets.view.header.button;

import android.content.Context;
import android.util.AttributeSet;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.configuration.ButtonConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;
import com.google.android.material.button.MaterialButton;

public class GliaEndButton extends MaterialButton {
    private ButtonConfiguration buttonConfiguration;

    public GliaEndButton(Context context) {
        this(context, null);
    }

    public GliaEndButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.gliaHeaderEndButtonStyle);
    }

    public GliaEndButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createBuildTimeConfiguration();
        updateView();
    }

    private void updateView() {
        updateBackgroundColor();
        updateStrokeColor();
        updateTextView();
    }

    private void updateBackgroundColor() {
        if (hasBackgroundColor())
            setBackgroundTintList(buttonConfiguration.getBackgroundColor());
        else
            setBackgroundResource(0);
    }

    private boolean hasBackgroundColor() {
        return buttonConfiguration.getBackgroundColor() != null && buttonConfiguration.getBackgroundColor().getDefaultColor() != 0;
    }

    private void updateStrokeColor() {
        setStrokeColor(buttonConfiguration.getStrokeColor());
    }

    private void updateTextView() {
        TextConfiguration textConfiguration = buttonConfiguration.getTextConfiguration();
        setTextColor(textConfiguration.getTextColor());
        setHintTextColor(textConfiguration.getTextColorHint());
        setLinkTextColor(textConfiguration.getTextColorLink());
        setHighlightColor(textConfiguration.getTextColorHighlight());
    }

    private void createBuildTimeConfiguration() {
        TextConfiguration textConfiguration = TextConfiguration
                .builder()
                .textColor(getTextColors())
                .textColorHighlight(getHighlightColor())
                .textColorHint(getHintTextColors())
                .textSize(getTextSize())
                .build();

        buttonConfiguration = ButtonConfiguration
                .builder()
                .textConfiguration(textConfiguration)
                .backgroundColor(getBackgroundTintList())
                .strokeColor(getStrokeColor())
                .strokeWidth(getStrokeWidth())
                .build();
    }

    public void setTheme(UiTheme theme) {
        if (theme == null || theme.getGliaEndButtonConfiguration() == null) return;
        ButtonConfiguration runTimeConfiguration = theme.getGliaEndButtonConfiguration();
        ButtonConfiguration.Builder builder = ButtonConfiguration.builder(buttonConfiguration);
        if (runTimeConfiguration.getTextConfiguration() != null)
            builder.textConfiguration(runTimeConfiguration.getTextConfiguration());
        if (runTimeConfiguration.getBackgroundColor() != null)
            builder.backgroundColor(runTimeConfiguration.getBackgroundColor());
        if (runTimeConfiguration.getStrokeColor() != null)
            builder.strokeColor(runTimeConfiguration.getStrokeColor());
        if (runTimeConfiguration.getStrokeWidth() != null)
            builder.strokeWidth(runTimeConfiguration.getStrokeWidth());
        buttonConfiguration = builder.build();
        updateView();
    }
}
