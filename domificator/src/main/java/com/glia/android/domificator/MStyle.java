package com.glia.android.domificator;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MStyle {
    private Map<String, String> styleMap = new HashMap<>();

    public MStyle add(String attributeName, String attributeValue) {
        styleMap.put(attributeName, attributeValue);
        return this;
    }

    public MStyle addColor(int color) {
        return add("color", colorIntToHex(color));
    }

    public MStyle addBackgroundColor(int color) {
        return add("background-color", colorIntToHex(color));
    }

    private String colorIntToHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    /**
     * N.B: Should use {@link Gravity#getAbsoluteGravity(int, int)} value for this method to work properly
     * e.g: Gravity.getAbsoluteGravity(((TextView) view).getGravity(), view.getLayoutDirection())
     */
    public MStyle addTextAlign(int gravity) {
        switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL: //center
                return add("text-align", "center");
            case Gravity.LEFT: //viewStart
                return add("text-align", "left");
            case Gravity.RIGHT: //viewEnd
                return add("text-align", "right");
            default:
                // no horizontal gravity applied to the receive value
                // TODO: apply some default behavior?
                return this;
        }
    }

    public MStyle addTextSize(float textSize) {
        return add("font-size", textSize + "px");
    }

    public MStyle remove(String attributeName) {
        styleMap.remove(attributeName);
        return this;
    }

    public MStyle addViewMetrics(View view) {
        int[] coordinates = new int[2];
        view.getLocationInWindow(coordinates);
        add("height", view.getHeight() + "px");
        add("width", view.getWidth() + "px");
        add("left", coordinates[0] + "px");
        add("top", coordinates[1] + "px");
        add("position", "fixed");
        add("padding-top", view.getPaddingTop() + "px");
        add("padding-right", view.getPaddingRight() + "px");
        add("padding-bottom", view.getPaddingBottom() + "px");
        add("padding-left", view.getPaddingLeft() + "px");
        Optional.ofNullable(getBackgroundColor(view)).ifPresent(this::addBackgroundColor);
        // TODO: margins?
        // TODO: opacity?
        // TODO: background images?
        return this;
    }

    private Integer getBackgroundColor(View view) {
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            return ((ColorDrawable) background).getColor();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        if (!styleMap.isEmpty()) {
            boolean isFirst = true;
            for (Map.Entry<String, String> styleNode: styleMap.entrySet()) {
                if (!isFirst) {
                    output.append(" ");
                } else {
                    isFirst = false;
                }
                output.append(styleNode.getKey());
                output.append(":");
                output.append(styleNode.getValue());
                output.append(";");
            }
        }
        return output.toString();
    }
}
