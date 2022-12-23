package com.glia.widgets.core.dialog;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class Dialog {

    public static final int MODE_NONE = 0;
    public static final int MODE_OVERLAY_PERMISSION = 1;
    public static final int MODE_UNEXPECTED_ERROR = 2;
    public static final int MODE_NO_MORE_OPERATORS = 3;
    public static final int MODE_ENGAGEMENT_ENDED = 4;
    public static final int MODE_EXIT_QUEUE = 5;
    public static final int MODE_START_SCREEN_SHARING = 6;
    public static final int MODE_END_ENGAGEMENT = 7;
    public static final int MODE_ENABLE_NOTIFICATION_CHANNEL = 8;
    public static final int MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING = 9;
    public static final int MODE_MEDIA_UPGRADE = 10;
    public static final int MODE_VISITOR_CODE = 11;
    public static final int MODE_MESSAGE_CENTER_UNAVAILABLE = 12;
    public static final int MODE_UNAUTHENTICATED = 13;

    @IntDef({MODE_NONE, MODE_OVERLAY_PERMISSION, MODE_UNEXPECTED_ERROR, MODE_NO_MORE_OPERATORS, MODE_ENGAGEMENT_ENDED,
            MODE_EXIT_QUEUE, MODE_START_SCREEN_SHARING, MODE_END_ENGAGEMENT, MODE_ENABLE_NOTIFICATION_CHANNEL,
            MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING, MODE_MEDIA_UPGRADE, MODE_VISITOR_CODE,
            MODE_MESSAGE_CENTER_UNAVAILABLE, MODE_UNAUTHENTICATED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

}
