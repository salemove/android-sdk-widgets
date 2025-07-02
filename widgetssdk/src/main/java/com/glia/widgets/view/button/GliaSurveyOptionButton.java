package com.glia.widgets.view.button;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.view.configuration.ButtonConfiguration;
import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.OptionButtonConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;

/**
 * @hide
 */
public class GliaSurveyOptionButton extends BaseConfigurableButton {
    private boolean isError = false;
    private OptionButtonConfiguration buttonConfiguration;

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

    public boolean isError() {
        return isError;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        applyView();
    }

    public void setStyle(OptionButtonConfiguration buttonConfiguration) {
        this.buttonConfiguration = buttonConfiguration;
    }

    private void applyView() {
        LayerConfiguration normalLayer = buttonConfiguration.getNormalLayer();
        LayerConfiguration selectedLayer = buttonConfiguration.getSelectedLayer();
        LayerConfiguration highlightedLayer = buttonConfiguration.getHighlightedLayer();

        ColorStateList strokeColor =
            isError ? ColorStateList.valueOf(Color.parseColor(highlightedLayer.getBorderColor())) :
                isSelected() ?
                    ColorStateList.valueOf(Color.parseColor(selectedLayer.getBorderColor())) :
                    ColorStateList.valueOf(Color.parseColor(normalLayer.getBorderColor()));

        ColorStateList backgroundColor = isSelected() ?
            ColorStateList.valueOf(Color.parseColor(selectedLayer.getBackgroundColor())) :
            ColorStateList.valueOf(Color.parseColor(normalLayer.getBackgroundColor()));

        ColorStateList textColor = isSelected() ?
            buttonConfiguration.getSelectedText().getTextColor() :
            buttonConfiguration.getNormalText().getTextColor();

        setStrokeColor(strokeColor);
        setStrokeWidth(normalLayer.getBorderWidth());
        setBackgroundTintList(backgroundColor);
        setTextColor(textColor);
        float textSize = buttonConfiguration.getNormalText().getTextSize();
        setTextSize(textSize);
        setupTypeFace();
        int radiusDp = normalLayer.getCornerRadius();
        int radiusPx = Dependencies.getResourceProvider().convertDpToIntPixel(radiusDp);
        setCornerRadius(radiusPx);
    }

    private void setupTypeFace() {
        TextConfiguration textConfiguration;
        if (isSelected()) {
            textConfiguration = buttonConfiguration.getSelectedText();
        } else {
            textConfiguration = buttonConfiguration.getNormalText();
        }
        if (textConfiguration.isBold()) setTypeface(Typeface.DEFAULT_BOLD);
    }
}
