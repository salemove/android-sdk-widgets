package com.glia.widgets;

import static com.glia.widgets.helper.Logger.SITE_ID_KEY;
import static java.util.Collections.singletonMap;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.GliaConfig;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.visitor.Authentication;
import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest;
import com.glia.widgets.chat.adapter.CustomCardAdapter;
import com.glia.widgets.chat.adapter.WebViewCardAdapter;
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer;
import com.glia.widgets.core.visitor.GliaVisitorInfo;
import com.glia.widgets.core.visitor.GliaWidgetException;
import com.glia.widgets.core.visitor.VisitorInfoUpdate;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Logger;

import java.io.IOException;
import java.util.function.Consumer;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * This class is a starting point for integration with Glia Widgets SDK
 */
public class GliaWidgets {
    public static final String REMOTE_CONFIGURATION = "remote_configuration";
    /**
     * Use with {@link android.os.Bundle} to pass in a {@link UiTheme} as a navigation argument when
     * navigating to {@link com.glia.widgets.chat.ChatActivity}
     */
    public static final String UI_THEME = "ui_theme";
    /**
     * Use with {@link android.os.Bundle} to pass in the name of your company as a navigation
     * argument when navigating to {@link com.glia.widgets.chat.ChatActivity}
     *
     * @deprecated Use {@link com.glia.widgets.GliaWidgetsConfig.Builder#companyName}
     * or customize the strings from GliaHub
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
     *
     * @deprecated Use {@link com.glia.widgets.GliaWidgets#CONTEXT_ASSET_ID}
     */
    @Deprecated
    public static final String CONTEXT_URL = "context_url";
    /**
     * Use with {@link android.os.Bundle} to pass in a context asset ID as a navigation
     * argument when navigating to {@link com.glia.widgets.chat.ChatActivity}
     */
    public static final String CONTEXT_ASSET_ID = "context_asset_id";
    /**
     * Use with {@link android.os.Bundle} to pass in a boolean which represents if you would like to
     * use the chat head bubble as an overlay as a navigation argument when
     * navigating to {@link com.glia.widgets.chat.ChatActivity}
     * If set to true then the chat head will appear in the overlay and the sdk will ask for
     * overlay permissions. If false, then the {@link com.glia.widgets.view.head.controller.ApplicationChatHeadLayoutController} will notify any
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
     * Pass this parameter to call activity to tell it that upgrade to audio/video call is ongoing
     * If no parameter is passed then will default to false
     */
    public static final String IS_UPGRADE_TO_CALL = "upgrade_to_call";
    public static final String SURVEY = "survey";
    /**
     * Use with {@link android.os.Bundle} to pass in
     * {@link com.glia.androidsdk.screensharing.ScreenSharing.Mode} as a navigation
     * argument when navigating to {@link com.glia.widgets.chat.ChatActivity}
     */
    public static final String SCREEN_SHARING_MODE = "screens_haring_mode";
    public static final String CHAT_TYPE = "chat_type";
    private final static String TAG = "GliaWidgets";
    @Nullable
    private static CustomCardAdapter customCardAdapter = new WebViewCardAdapter();

    /**
     * Should be called when the application is starting in {@link Application}.onCreate()
     *
     * @param application the application where it is initialized
     */
    public synchronized static void onAppCreate(Application application) {
        Dependencies.glia().onAppCreate(application);
        Dependencies.onAppCreate(application);
        setupRxErrorHandler();
        Logger.d(TAG, "onAppCreate");
    }

    /**
     * Initializes the Glia core SDK using {@link GliaWidgetsConfig}.
     *
     * @param gliaWidgetsConfig Glia configuration
     */
    public synchronized static void init(GliaWidgetsConfig gliaWidgetsConfig) {
        GliaConfig gliaConfig = createGliaConfig(gliaWidgetsConfig);
        Dependencies.glia().init(gliaConfig);
        Dependencies.init(gliaWidgetsConfig);
        setupLoggingMetadata(gliaWidgetsConfig);
        Dependencies.getGliaThemeManager().applyJsonConfig(gliaWidgetsConfig.getUiJsonRemoteConfig());
        Logger.d(TAG, "init");
    }

    private static GliaConfig createGliaConfig(GliaWidgetsConfig gliaWidgetsConfig) {
        GliaConfig.Builder builder = new GliaConfig.Builder();
        setAuthorization(gliaWidgetsConfig, builder);
        return builder
            .setSiteId(gliaWidgetsConfig.getSiteId())
            .setRegion(gliaWidgetsConfig.getRegion())
            .setBaseDomain(gliaWidgetsConfig.getBaseDomain())
            .setContext(gliaWidgetsConfig.getContext())
            .setManualLocaleOverride(gliaWidgetsConfig.getManualLocaleOverride())
            .build();
    }

    private static void setAuthorization(
        GliaWidgetsConfig widgetsConfig,
        GliaConfig.Builder builder
    ) {
        if (widgetsConfig.getSiteApiKey() != null) {
            builder.setSiteApiKey(widgetsConfig.getSiteApiKey());
        } else {
            throw new RuntimeException("Site key or app token is missing");
        }
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
        Dependencies.glia().onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement -> engagement.onActivityResult(requestCode, resultCode, data));
    }

    /**
     * Clears visitor session
     */
    public static void clearVisitorSession() {
        Logger.d(TAG, "clearVisitorSession");
        Dependencies.getControllerFactory().destroyControllers();
        Dependencies.getUseCaseFactory().resetState();
        Dependencies.glia().clearVisitorSession();
    }

    /**
     * Ends active engagement
     * <p>
     * Ends active engagement if existing and closes Widgets SDK UI (includes bubble).
     */
    public static void endEngagement() {
        Logger.d(TAG, "endEngagement");
        Dependencies.getControllerFactory().destroyControllers();
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement -> engagement.end(e -> {
            if (e != null) {
                Logger.e(TAG, "Ending engagement error: " + e);
            }
        }));
    }

    /**
     * Updates the visitor's information
     * <p>
     * Updates the visitor's information stored on the server. This information will also be displayed to the operator.
     *
     * @deprecated since 1.9.0 use @see {@link com.glia.androidsdk.Glia#updateVisitorInfo(VisitorInfoUpdateRequest, Consumer)}
     */
    @Deprecated
    public static void updateVisitorInfo(VisitorInfoUpdate visitorInfoUpdate, Consumer<GliaWidgetException> exceptionConsumer) {
        Dependencies.glia().updateVisitorInfo(new VisitorInfoUpdateRequest.Builder()
            .setName(visitorInfoUpdate.getName())
            .setEmail(visitorInfoUpdate.getEmail())
            .setPhone(visitorInfoUpdate.getPhone())
            .setNote(visitorInfoUpdate.getNote())
            .setCustomAttributes(visitorInfoUpdate.getCustomAttributes())
            .setCustomAttrsUpdateMethod(visitorInfoUpdate.getCustomAttrsUpdateMethod())
            .setNoteUpdateMethod(visitorInfoUpdate.getNoteUpdateMethod())
            .build(), e -> {
            if (e != null) {
                exceptionConsumer.accept(new GliaWidgetException(e.debugMessage, e.cause));
            } else {
                exceptionConsumer.accept(null);
            }
        });
    }

    /**
     * Fetches the visitor's information
     * <p>
     * If visitor is authenticated, the response will include the attributes and tokens fetched from the authentication provider.
     *
     * @deprecated since 1.9.0 use @see {@link com.glia.androidsdk.Glia#getVisitorInfo(RequestCallback)}
     */
    @Deprecated
    public static void getVisitorInfo(Consumer<GliaVisitorInfo> visitorCallback, Consumer<GliaWidgetException> exceptionConsumer) {
        Dependencies.glia().getVisitorInfo((visitorInfo, e) -> {
            if (visitorInfo != null) {
                visitorCallback.accept(new GliaVisitorInfo(visitorInfo));
            }
            if (e != null) {
                exceptionConsumer.accept(new GliaWidgetException(e.debugMessage, e.cause));
            }
        });
    }

    /**
     * @return current instance of {@link CustomCardAdapter}
     */
    @Nullable
    public static CustomCardAdapter getCustomCardAdapter() {
        return customCardAdapter;
    }

    /**
     * Allows configuring custom response cards based on metadata.
     * <p>
     * Glia SDK uses {@link WebViewCardAdapter} by default.
     * This method allows setting the custom implementation of {@link CustomCardAdapter}.
     *
     * @param customCardAdapter an instance of {@link CustomCardAdapter}
     *                          or {@code null} for the default, not Custom Card, Glia message implementation.
     * @see CustomCardAdapter
     * @see WebViewCardAdapter
     */
    public static void setCustomCardAdapter(@Nullable CustomCardAdapter customCardAdapter) {
        GliaWidgets.customCardAdapter = customCardAdapter;
    }

    /**
     * Creates `Authentication` instance for a given JWT token.
     *
     * @param behavior authentication behavior
     * @return {@code Authentication} object or throws {@link GliaException} if error happened.
     * Exception may have the following cause:
     * {@link GliaException.Cause#INVALID_INPUT} - when SDK is not initialized
     */
    public static Authentication getAuthentication(@NonNull Authentication.Behavior behavior) {
        return Dependencies.glia().getAuthentication(behavior);
    }

    /**
     * Provides controls related to the {@link CallVisualizer} module.
     */
    public static CallVisualizer getCallVisualizer() {
        return Dependencies.getCallVisualizerManager();
    }

    /**
     * Build-time version of Glia Widgets SDK
     *
     * @return Glia Widgets SDK version name
     */
    public static String getWidgetsSdkVersion() {
        return BuildConfig.GLIA_WIDGETS_SDK_VERSION;
    }

    /**
     * Build-time version of Glia Core SDK that is used by Glia Widgets SDK
     *
     * @return Glia Core SDK version name that is used by Glia Widgets SDK
     */
    public static String getWidgetsCoreSdkVersion() {
        return BuildConfig.GLIA_CORE_SDK_VERSION;
    }

    private static void setupLoggingMetadata(GliaWidgetsConfig gliaWidgetsConfig) {
        Logger.addGlobalMetadata(singletonMap(SITE_ID_KEY, gliaWidgetsConfig.getSiteId()));
    }

    // More info about global Rx error handler:
    //     https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
    private static void setupRxErrorHandler() {
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if (e instanceof IOException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                throwUncaughtException(e);
                return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                throwUncaughtException(e);
                return;
            }

            logUndeliverableException(e);
        });
    }

    private static void throwUncaughtException(Throwable e) {
        Thread.UncaughtExceptionHandler handler =
            Thread.currentThread().getUncaughtExceptionHandler();
        if (handler != null) {
            handler.uncaughtException(Thread.currentThread(), e);
        }
    }

    private static void logUndeliverableException(Throwable e) {
        String message = "Exception message: ";
        if (e != null) message += e.getMessage();
        Logger.e("RxErrorHandler", "Undeliverable exception received, not sure what to do. " + message);
    }
}
