package com.glia.widgets.view.textview;

import static com.glia.widgets.helper.Utils.pxToSp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.configuration.TextConfiguration;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

public abstract class BaseConfigurableTextView extends MaterialTextView {
    private TextConfiguration textConfiguration;

    public BaseConfigurableTextView(@NonNull Context context) {
        this(context, null);
    }

    public BaseConfigurableTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.textAppearanceBody1);
    }

    public BaseConfigurableTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Body);
    }

    public BaseConfigurableTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        createBuildTimeConfiguration();
        updateView();
    }

    public abstract TextConfiguration getTextConfigurationFromTheme(UiTheme theme);

    private void createBuildTimeConfiguration() {
        textConfiguration = TextConfiguration
                .builder()
                .textConfiguration(TextConfiguration.getDefaultTextConfiguration())
                .textColor(getTextColors())
                .textColorHighlight(getHighlightColor())
                .hintColor(getHintTextColors())
                .textSize(pxToSp(getContext(), getTextSize()))
                .build();
    }

    public void setTheme(UiTheme theme) {
        if (theme == null) return;
        TextConfiguration runTimeConfiguration = getTextConfigurationFromTheme(theme);
        if (runTimeConfiguration == null) return;
        TextConfiguration.Builder builder = new TextConfiguration.Builder(textConfiguration);

        if (runTimeConfiguration.getTextColor() != null)
            builder.textColor(runTimeConfiguration.getTextColor());
        if (!Objects.equals(runTimeConfiguration.getTextColorHighlight(), textConfiguration.getTextColorHighlight()))
            builder.textColorHighlight(runTimeConfiguration.getTextColorHighlight());
        if (runTimeConfiguration.getHintColor() != null)
            builder.hintColor(runTimeConfiguration.getHintColor());
        if (!Objects.equals(runTimeConfiguration.getTextSize(), textConfiguration.getTextSize()))
            builder.textSize(runTimeConfiguration.getTextSize());
        if (!Objects.equals(runTimeConfiguration.getFontFamily(), textConfiguration.getFontFamily()))
            builder.fontFamily(runTimeConfiguration.getFontFamily());

        textConfiguration = builder.build();
        updateView();
    }

    private void updateView() {
        if (textConfiguration.getTextSize() != null) {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textConfiguration.getTextSize());
        }
        setTextColor(textConfiguration.getTextColor());
        setHintTextColor(textConfiguration.getHintColor());
        setLinkTextColor(textConfiguration.getTextColorLink());
        if (textConfiguration.getTextColorHighlight() != null) {
            setHighlightColor(textConfiguration.getTextColorHighlight());
        }
        if (textConfiguration.getFontFamily() != null) {
            setTypeface(ResourcesCompat.getFont(getContext(), textConfiguration.getFontFamily()));
        }
    }
}
