package com.glia.widgets;

public class Constants {
    /**
     * Used by the sdk to differentiate between CHAT and CALL and CALL and IMAGE_PREVIEW activities.
     */
    public static final String CHAT_ACTIVITY = "chat_activity";
    /**
     * Used by the sdk to differentiate between CHAT and CALL and CALL and IMAGE_PREVIEW activities.
     */
    public static final String CALL_ACTIVITY = "call_activity";
    /**
     * Used by the sdk to differentiate between CHAT and CALL and IMAGE_PREVIEW activities.
     */
    public static final String IMAGE_PREVIEW_ACTIVITY = "image_preview_activity";
    /**
     * Global media timer delay value
     */
    public static final int CALL_TIMER_DELAY = 0;
    /**
     * Global media timer interval value
     */
    public static final int CALL_TIMER_INTERVAL_VALUE = 1000;

    /**
     * Needed to overlap existing app bar in existing view with this view's app bar.
     */
    public static final float WIDGETS_SDK_LAYER_ELEVATION = 100f;
}
