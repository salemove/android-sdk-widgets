package com.glia.widgets;

import android.content.res.ColorStateList;

public class UiTheme {

    private final ColorStateList colorPrimary;
    private final ColorStateList backgroundTint;
    private final ColorStateList senderBackgroundTint;

    public UiTheme(ColorStateList colorPrimary, ColorStateList backgroundTint, ColorStateList senderBackgroundTint) {
        this.colorPrimary = colorPrimary;
        this.backgroundTint = backgroundTint;
        this.senderBackgroundTint = senderBackgroundTint;
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
}
