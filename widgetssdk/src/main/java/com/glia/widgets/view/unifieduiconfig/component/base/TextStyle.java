package com.glia.widgets.view.unifieduiconfig.component.base;

import android.graphics.Typeface;

import androidx.annotation.NonNull;

/**
 * Represents text style from remote config
 */
public class TextStyle {

    private final int style;

    public TextStyle(int style) {
        this.style = style;
    }

    @NonNull
    public Typeface applyTo(Typeface typeface) {
        return Typeface.create(typeface, style);
    }

    public int getStyle() {
        return style;
    }
}
