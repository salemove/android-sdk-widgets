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
     * Used by the sdk to differentiate between CHAT and CALL and URL_WEB_VIEW_ACTIVITY activities.
     */
    public static final String URL_WEB_VIEW_ACTIVITY = "url_web_view";
    /**
     * Global media timer delay value
     */
    public static final int CALL_TIMER_DELAY = 0;
    /**
     * Global media timer interval value
     */
    public static final int CALL_TIMER_INTERVAL_VALUE = 1000;

    public static final String PHONE_NUMBER_REGEX = "^[+]\\s*?[0-9]{10,13}$";
}
