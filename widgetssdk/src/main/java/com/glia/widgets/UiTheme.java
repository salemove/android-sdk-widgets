package com.glia.widgets;

import android.content.res.ColorStateList;
import android.graphics.Typeface;

public class UiTheme {

    private final ColorStateList colorPrimary;
    private final ColorStateList backgroundTint;
    private final ColorStateList senderBackgroundTint;
    private final Typeface fontFamily;
    private final ColorStateList primaryTextColor;

    public UiTheme(ColorStateList colorPrimary, ColorStateList backgroundTint, ColorStateList senderBackgroundTint, Typeface fontFamily, ColorStateList primaryTextColor) {
        this.colorPrimary = colorPrimary;
        this.backgroundTint = backgroundTint;
        this.senderBackgroundTint = senderBackgroundTint;
        this.fontFamily = fontFamily;
        this.primaryTextColor = primaryTextColor;
    }

    public ColorStateList getColorPrimary() {
        return colorPrimary;
    }

    public ColorStateList getBackgroundTint() {
        return backgroundTint;
    }

    public ColorStateList getSenderBackgroundTint() {
        return senderBackgroundTint;
    }

    public Typeface getFontFamily() {
        return fontFamily;
    }

    public ColorStateList getPrimaryTextColor() {
        return this.primaryTextColor;
    }
}
