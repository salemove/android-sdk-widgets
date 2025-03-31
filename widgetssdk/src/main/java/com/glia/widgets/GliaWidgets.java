package com.glia.widgets;

import static com.glia.widgets.helper.Logger.SITE_ID_KEY;
import static java.util.Collections.singletonMap;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.visitor.Authentication;
import com.glia.widgets.chat.adapter.CustomCardAdapter;
import com.glia.widgets.chat.adapter.WebViewCardAdapter;
import com.glia.widgets.core.authentication.AuthenticationManager;
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.engagement.EndedBy;
import com.glia.widgets.entrywidget.EntryWidget;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.launcher.EngagementLauncher;

import java.io.IOException;
import java.util.List;

import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;


/**
 * This class is a starting point for integration with Glia Widgets SDK
 */
public class GliaWidgets {

    private final static String TAG = "GliaWidgets";

    @Nullable
    private static CustomCardAdapter customCardAdapter = new WebViewCardAdapter();

    /**
     * Should be called when the application is starting in {@link Application}.onCreate()
     *
     * @param application the application where it is initialized
     * @throws GliaWidgetsException with {@link GliaWidgetsException.Cause}
     */
    public synchronized static void onAppCreate(Application application) {
        try {
            Dependencies.onAppCreate(application);
        } catch (GliaException gliaException) {
            throw mapCoreExceptionToWidgets(gliaException);
        }
        setupRxErrorHandler();
        Logger.d(TAG, "onAppCreate");
    }

    /**
     * Initializes the Glia core SDK using {@link GliaWidgetsConfig}.
     *
     * @param gliaWidgetsConfig Glia configuration
     * @throws GliaWidgetsException with {@link GliaWidgetsException.Cause}
     */
    public synchronized static void init(GliaWidgetsConfig gliaWidgetsConfig) {
        Logger.i(TAG, "Initialize Glia Widgets SDK");
        try {
            Dependencies.onSdkInit(gliaWidgetsConfig);
            setupLoggingMetadata(gliaWidgetsConfig);
            Dependencies.getGliaThemeManager().applyJsonConfig(gliaWidgetsConfig.uiJsonRemoteConfig);
        } catch (GliaException gliaException) {
            throw mapCoreExceptionToWidgets(gliaException);
        }

    }

    /**
     * Retrieves an instance of {@link EngagementLauncher}.
     *
     * @param queueIds A list of queue IDs to be used for the engagement launcher.
     *                 When empty or invalid, the default queues will be used.
     * @return An instance of {@link EngagementLauncher}.
     * @throws GliaWidgetsException with the {@link GliaWidgetsException.Cause#INVALID_INPUT} if the SDK is not initialized.
     */
    @NonNull
    public synchronized static EngagementLauncher getEngagementLauncher(@NonNull List<String> queueIds) {
        Logger.i(TAG, "Returning an Engagement Launcher");
        try {
            setupQueueIds(queueIds);

            return Dependencies.getEngagementLauncher();
        } catch (GliaException gliaException) {
            throw mapCoreExceptionToWidgets(gliaException);
        }
    }

    /**
     * Retrieves an instance of {@link EntryWidget}.
     *
     * @param queueIds A list of queue IDs to be used for the entry widget.
     *                 When empty or invalid, the default queues will be used.
     * @return An instance of {@link EntryWidget}.
     * @throws GliaWidgetsException with the {@link GliaWidgetsException.Cause#INVALID_INPUT} if the SDK is not initialized.
     */
    @NonNull
    public synchronized static EntryWidget getEntryWidget(@NonNull List<String> queueIds) {
        if (!Dependencies.glia().isInitialized()) {
            Logger.e(TAG, "Attempt to get EntryWidget before SDK initialization");
        }

        try {
            Dependencies.getConfigurationManager().setQueueIds(queueIds);
            return Dependencies.getEntryWidget();
        } catch (GliaException gliaException) {
            throw mapCoreExceptionToWidgets(gliaException);
        }
    }

    private static void setupQueueIds(@NonNull List<String> queueIds) {
        Dependencies.glia().ensureInitialized();

        Dependencies.getConfigurationManager().setQueueIds(queueIds);
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
     *
     * @deprecated This method is no longer required, as all the required permissions are now managed internally.
     */
    @Deprecated(forRemoval = true)
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Logger.d(TAG, "onRequestPermissionsResult");
        try {
            Dependencies.glia().onRequestPermissionsResult(requestCode, permissions, grantResults);
        } catch (GliaException gliaException) {
            throw mapCoreExceptionToWidgets(gliaException);
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
     *
     * @deprecated This method is no longer required, as required activity results are now managed internally.
     */
    @Deprecated(forRemoval = true)
    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d(TAG, "onActivityResult");
        try {
            Dependencies.getRepositoryFactory().getEngagementRepository().onActivityResult(requestCode, resultCode, data);
        } catch (GliaException gliaException) {
            throw mapCoreExceptionToWidgets(gliaException);
        }
    }

    /**
     * Clears visitor session
     * @throws GliaWidgetsException with {@link GliaWidgetsException.Cause}
     */
    public static void clearVisitorSession() {
        Logger.i(TAG, "Clear visitor session");
        try {
            Dependencies.destroyControllersAndResetEngagementData();

            //Here we reset the secure conversations repository to clear the data, because the visitor session is cleared(de-authenticated)
            //and we don't need secure conversations data for un-authenticated visitors.
            Dependencies.getRepositoryFactory().getSecureConversationsRepository().unsubscribeAndResetData();

            Dependencies.glia().clearVisitorSession();
        } catch (GliaException gliaException) {
            throw mapCoreExceptionToWidgets(gliaException);
        }
    }

    /**
     * Ends active engagement
     * <p>
     * Ends active engagement if existing and closes Widgets SDK UI (includes bubble).
     * @throws GliaWidgetsException with {@link GliaWidgetsException.Cause}
     */
    public static void endEngagement() {
        Logger.i(TAG, "End engagement by integrator");
        try {
            Dependencies.getUseCaseFactory().getEndEngagementUseCase().invoke(EndedBy.CLEAR_STATE);
        } catch (GliaException gliaException) {
            throw mapCoreExceptionToWidgets(gliaException);
        }
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
     * @return {@code Authentication} object or throws {@link GliaWidgetsException} if error happened.
     * Exception may have the following cause:
     * {@link GliaWidgetsException.Cause#INVALID_INPUT} - when SDK is not initialized
     */
    public static Authentication getAuthentication(@NonNull Authentication.Behavior behavior) {
        try {
            AuthenticationManager authentication = Dependencies.glia().getAuthenticationManager(behavior);
            Dependencies.setAuthenticationManager(authentication);
            return authentication;
        } catch (GliaException gliaException) {
            throw mapCoreExceptionToWidgets(gliaException);
        }
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
        Logger.addGlobalMetadata(singletonMap(SITE_ID_KEY, gliaWidgetsConfig.siteId));
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

    private static RuntimeException mapCoreExceptionToWidgets(GliaException gliaException) {
        GliaWidgetsException widgetsException = GliaWidgetsException.from(gliaException);
        return widgetsException != null ? widgetsException : gliaException;
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
