package com.glia.widgets.view.unifieduiconfig.component.base;

import com.glia.widgets.view.unifieduiconfig.deserializer.ColorDeserializer;

/**
 * Represents ARGB color fields from remote config e.g. #FF44DD55
 * with the help of {@link ColorDeserializer} it guarantees
 * that {@link #getColor()} will return parsed valid color value
 */
public class Color {
    private final int color;

    public Color(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
