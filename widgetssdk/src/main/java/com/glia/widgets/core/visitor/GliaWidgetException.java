package com.glia.widgets.core.visitor;

import com.glia.androidsdk.GliaException;

/**
 * Glia Exception that has been thrown by Widgets SDK.
 */
public class GliaWidgetException extends RuntimeException {
    private String debugMessage;
    private GliaException.Cause cause;

    public GliaWidgetException(String s, GliaException.Cause cause) {
        super(s);
        this.cause = cause;
        this.debugMessage = s;
    }

    public String getDebugMessage() {
        return debugMessage;
    }

    public String getGliaCause() {
        return cause.toString();
    }

    public String toString() {
        return "GliaException:" + this.debugMessage + ", cause: " + this.cause.toString();
    }
}
