package com.glia.widgets.view.textview;


import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.ContextExtensions;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.configuration.TextConfiguration;
import com.google.android.material.textview.MaterialTextView;

/**
 * @hide
 */
public abstract class BaseConfigurableTextView extends MaterialTextView {
    private TextConfiguration textConfiguration;
    private final ResourceProvider resourceProvider;

    public BaseConfigurableTextView(@NonNull Context context) {
        this(context, null);
    }

    public BaseConfigurableTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.textAppearanceBody1);
    }

    public BaseConfigurableTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Body);
    }

    public BaseConfigurableTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (isInEditMode()) {
            resourceProvider = new ResourceProvider(getContext());
        } else {
            resourceProvider = Dependencies.getResourceProvider();
        }

        createBuildTimeConfiguration();
        updateView();
    }

    public abstract TextConfiguration getTextConfigurationFromTheme(UiTheme theme);

    private void createBuildTimeConfiguration() {
        textConfiguration = TextConfiguration
            .builder()
            .textColor(getTextColors())
            .textColorHighlight(getHighlightColor())
            .hintColor(getHintTextColors())
            .textSize(ContextExtensions.pxToSp(getContext(), getTextSize()))
            .build(resourceProvider);
    }

    public void setTheme(UiTheme theme) {
        if (theme == null) return;
        TextConfiguration runTimeConfiguration = getTextConfigurationFromTheme(theme);
        if (runTimeConfiguration == null) return;
        TextConfiguration.Builder builder = new TextConfiguration.Builder(textConfiguration);

        if (runTimeConfiguration.getTextColor() != null)
            builder.textColor(runTimeConfiguration.getTextColor());
        if (runTimeConfiguration.getTextColorHighlight() != textConfiguration.getTextColorHighlight())
            builder.textColorHighlight(runTimeConfiguration.getTextColorHighlight());
        if (runTimeConfiguration.getHintColor() != null)
            builder.hintColor(runTimeConfiguration.getHintColor());
        if (runTimeConfiguration.getTextSize() != textConfiguration.getTextSize())
            builder.textSize(runTimeConfiguration.getTextSize());
        if (runTimeConfiguration.getFontFamily() != textConfiguration.getFontFamily())
            builder.fontFamily(runTimeConfiguration.getFontFamily());

        textConfiguration = builder.build(resourceProvider);
        updateView();
    }

    private void updateView() {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, textConfiguration.getTextSize());
        setTextColor(textConfiguration.getTextColor());
        setHintTextColor(textConfiguration.getHintColor());
        setLinkTextColor(textConfiguration.getTextColorLink());
        setHighlightColor(textConfiguration.getTextColorHighlight());
        if (textConfiguration.getFontFamily() != 0)
            setTypeface(ResourcesCompat.getFont(getContext(), textConfiguration.getFontFamily()));
    }
}
