package com.glia.widgets;

import android.app.Application;
import android.content.Intent;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaConfig;
import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest;
import com.glia.widgets.core.visitor.GliaWidgetException;
import com.glia.widgets.core.visitor.GliaVisitorInfo;
import com.glia.widgets.core.visitor.VisitorInfoUpdate;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.view.head.ChatHeadsController;

import java.util.function.Consumer;

/**
 * This class is a starting point for integration with Glia Widgets SDK
 */
public class GliaWidgets {

    private final static String TAG = "GliaWidgets";
    /**
     * Use with {@link android.os.Bundle} to pass in a {@link UiTheme} as a navigation argument when
     * navigating to {@link com.glia.widgets.chat.ChatActivity}
     */
    public static final String UI_THEME = "ui_theme";
    /**
     * Use with {@link android.os.Bundle} to pass in the name of your company as a navigation
     * argument when navigating to {@link com.glia.widgets.chat.ChatActivity}
     */
    public static final String COMPANY_NAME = "company_name";
    /**
     * Use with {@link android.os.Bundle} to pass in the id of the queue you wish to enroll in
     * as a navigation argument when navigating to {@link com.glia.widgets.chat.ChatActivity}
     */
    public static final String QUEUE_ID = "queue_id";
    /**
     * Use with {@link android.os.Bundle} to pass in a context url as a navigation
     * argument when navigating to {@link com.glia.widgets.chat.ChatActivity}
     */
    public static final String CONTEXT_URL = "context_url";
    /**
     * Use with {@link android.os.Bundle} to pass in a boolean which represents if you would like to
     * use the chat head bubble as an overlay as a navigation argument when
     * navigating to {@link com.glia.widgets.chat.ChatActivity}
     * If set to true then the chat head will appear in the overlay and the sdk will ask for
     * overlay permissions. If false, then the {@link ChatHeadsController} will notify any
     * listening {@link com.glia.widgets.view.head.ChatHeadLayout} of any visibility changes.
     * It is up to the integrator to integrate {@link com.glia.widgets.view.head.ChatHeadLayout} in their
     * application.
     * When this value is not passed then by default this value is true.
     */
    public static final String USE_OVERLAY = "use_overlay";

    /**
     * Use with {@link android.os.Bundle} to pass an input parameter to the call activity to
     * tell it which type of engagement you would like to start. Can be one of:
     * {@link MEDIA_TYPE_AUDIO} or {@link MEDIA_TYPE_VIDEO}.
     * If no parameter is passed then will default to {@link MEDIA_TYPE_AUDIO}
     */

    public static final String MEDIA_TYPE = "media_type";
    /**
     * Pass this parameter as an input parameter with {@link MEDIA_TYPE} as its key to
     * {@link com.glia.widgets.call.CallActivity} to start an audio call media engagement.
     */
    public static final String MEDIA_TYPE_AUDIO = "media_type_audio";
    /**
     * Pass this parameter as an input parameter with {@link MEDIA_TYPE} as its key to
     * {@link com.glia.widgets.call.CallActivity} to start a video call media engagement.
     */
    public static final String MEDIA_TYPE_VIDEO = "media_type_video";

    /**
     * Should be called when the application is starting in {@link Application}.onCreate()
     *
     * @param application the application where it is initialized
     */
    public synchronized static void onAppCreate(Application application) {
        Glia.onAppCreate(application);
        Dependencies.onAppCreate(application);
        Logger.d(TAG, "onAppCreate");
    }

    /**
     * Initializes the Glia core SDK using {@link GliaWidgetsConfig}.
     *
     * @param gliaWidgetsConfig Glia configuration
     */
    public synchronized static void init(GliaWidgetsConfig gliaWidgetsConfig) {
        GliaConfig gliaConfig = new GliaConfig.Builder()
                .setAppToken(gliaWidgetsConfig.getAppToken())
                .setSiteId(gliaWidgetsConfig.getSiteId())
                .setRegion(gliaWidgetsConfig.getRegion())
                .setContext(gliaWidgetsConfig.getContext())
                .build();

        Glia.init(gliaConfig);
        Dependencies.init();
        Logger.d(TAG, "init");
    }

    /**
     * Accepts permissions request results.
     *
     * <p>Some functionalities, for example Video or Audio calls, require to request runtime permissions via
     * {@link <a href="https://developer.android.com/reference/android/app/Activity.html#requestPermissions(java.lang.String[],%20int)">Activity#requestPermissions(String[], int)</a>}.
     * The results of such request is passed to your activity's
     * {@link
     * <a href="https://developer.android.com/reference/android/app/Activity.html#onRequestPermissionsResult(int,%2520java.lang.String%5B%5D,%2520int%5B%5D)">Activity#onRequestPermissionsResult(int, String[], int[])</a>}
     * <p>
     * Your activity in turn must call this method to pass the results of the request to Glia SDK.</p>
     *
     * <p>This method is no-op for other non-Glia triggered results.</p>
     */
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Logger.d(TAG, "onRequestPermissionsResult");
        Glia.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Accepts permissions request results.
     *
     * <p>Some functionalities, for example Video or Audio calls, require to request runtime permissions via
     * {@link <a href="https://developer.android.com/reference/android/app/Activity.html#requestPermissions(java.lang.String[],%20int)">Activity#requestPermissions(String[], int)</a>}.
     * The results of such request is passed to your activity's
     * {@link
     * <a href="https://developer.android.com/reference/android/app/Activity.html#onRequestPermissionsResult(int,%2520java.lang.String%5B%5D,%2520int%5B%5D)">Activity#onRequestPermissionsResult(int, String[], int[])</a>}
     * <p>
     * Your activity in turn must call this method to pass the results of the request to Glia SDK.</p>
     *
     * <p>This method is no-op for other non-Glia triggered results.</p>
     */
    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d(TAG, "onActivityResult");
        Glia.getCurrentEngagement().ifPresent(engagement -> engagement.onActivityResult(requestCode, resultCode, data));
    }

    public static void clearVisitorSession() {
        Logger.d(TAG, "clearVisitorSession");
        Dependencies.getControllerFactory().destroyControllers();
        Glia.clearVisitorSession();
    }

    /**
     * Updates the visitor's information
     * <p>
     * Updates the visitor's information stored on the server. This information will also be displayed to the operator.
     */
    public static void updateVisitorInfo(VisitorInfoUpdate visitorInfoUpdate, Consumer<GliaWidgetException> exceptionConsumer) {
        Glia.updateVisitorInfo(new VisitorInfoUpdateRequest.Builder()
                .setName(visitorInfoUpdate.getName())
                .setEmail(visitorInfoUpdate.getEmail())
                .setPhone(visitorInfoUpdate.getPhone())
                .setNote(visitorInfoUpdate.getNote())
                .setCustomAttributes(visitorInfoUpdate.getCustomAttributes())
                .setCustomAttrsUpdateMethod(visitorInfoUpdate.getCustomAttrsUpdateMethod())
                .setNoteUpdateMethod(VisitorInfoUpdateRequest.NoteUpdateMethod.REPLACE)
                .build(), e -> {
            if(e != null){
                exceptionConsumer.accept(new GliaWidgetException(e.debugMessage, e.cause));
            }else {
                exceptionConsumer.accept(null);
            }
        });
    }

    /**
     * Fetches the visitor's information
     * <p>
     * If visitor is authenticated, the response will include the attributes and tokens fetched from the authentication provider.
     */
    public static void getVisitorInfo(Consumer<GliaVisitorInfo> visitorCallback, Consumer<GliaWidgetException> exceptionConsumer) {
        Glia.getVisitorInfo((visitorInfo, e) -> {
            if(visitorInfo != null){
                visitorCallback.accept(new GliaVisitorInfo(visitorInfo));
            }
            if(e != null){
                exceptionConsumer.accept(new GliaWidgetException(e.debugMessage, e.cause));
            }
        });
    }
}