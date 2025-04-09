package com.glia.widgets

import android.app.Application
import android.content.Intent
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.visitor.Authentication
import com.glia.widgets.GliaWidgetsException.Companion.from
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.callbacks.OnSuccess
import com.glia.widgets.chat.adapter.CustomCardAdapter
import com.glia.widgets.chat.adapter.WebViewCardAdapter
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer
import com.glia.widgets.core.visitor.VisitorInfo
import com.glia.widgets.core.visitor.VisitorInfoUpdateRequest
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.Dependencies.callVisualizerManager
import com.glia.widgets.di.Dependencies.configurationManager
import com.glia.widgets.di.Dependencies.destroyControllersAndResetEngagementData
import com.glia.widgets.di.Dependencies.engagementLauncher
import com.glia.widgets.di.Dependencies.entryWidget
import com.glia.widgets.di.Dependencies.glia
import com.glia.widgets.di.Dependencies.gliaThemeManager
import com.glia.widgets.di.Dependencies.onSdkInit
import com.glia.widgets.di.Dependencies.repositoryFactory
import com.glia.widgets.di.Dependencies.setAuthenticationManager
import com.glia.widgets.di.Dependencies.useCaseFactory
import com.glia.widgets.engagement.EndedBy
import com.glia.widgets.entrywidget.EntryWidget
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.Logger.SITE_ID_KEY
import com.glia.widgets.helper.Logger.addGlobalMetadata
import com.glia.widgets.launcher.EngagementLauncher
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.io.IOException
import java.util.Collections
import java.util.function.Consumer

/**
 * This class is a starting point for integration with Glia Widgets SDK
 */
object GliaWidgets {
    private const val TAG = "GliaWidgets"

    /**
     * Build-time version of Glia Widgets SDK
     */
    @JvmStatic
    val widgetsSdkVersion: String
        get() = BuildConfig.GLIA_WIDGETS_SDK_VERSION

    /**
     * Build-time version of Glia Core SDK that is used by Glia Widgets SDK
     */
    @JvmStatic
    val widgetsCoreSdkVersion: String
        get() = BuildConfig.GLIA_CORE_SDK_VERSION

    private var _customCardAdapter: CustomCardAdapter? = WebViewCardAdapter()

    /**
     * CallVisualizer manager that provides controls related to the [CallVisualizer] module.
     */
    @JvmStatic
    fun getCallVisualizer(): CallVisualizer {
        return callVisualizerManager
    }

    /**
     * Current instance of [CustomCardAdapter].
     *
     * By default Glia SDK uses [WebViewCardAdapter].
     *
     * @see CustomCardAdapter
     * @see WebViewCardAdapter
     */
    @JvmStatic
    fun setCustomCardAdapter(adapter: CustomCardAdapter?) {
        _customCardAdapter = adapter
    }

    /**
     * Allows configuring custom response cards rendering based on metadata by setting a
     * custom implementation of [CustomCardAdapter].
     *
     * By default Glia SDK uses [WebViewCardAdapter].
     *
     * Set this to an instance of [CustomCardAdapter] for a custom implementation,
     * or `null` to use the default Glia message implementation without custom cards.
     *
     * @see CustomCardAdapter
     * @see WebViewCardAdapter
     */
    @JvmStatic
    fun getCustomCardAdapter(): CustomCardAdapter? {
        return _customCardAdapter
    }

    /**
     * Should be called when the application is starting in [Application].onCreate()
     *
     * @param application the application where it is initialized
     * @throws GliaWidgetsException with [GliaWidgetsException.Cause]
     */
    @Synchronized
    fun onAppCreate(application: Application) {
        try {
            Dependencies.onAppCreate(application)
        } catch (gliaException: GliaException) {
            throw mapCoreExceptionToWidgets(gliaException)
        }
        setupRxErrorHandler()
        Logger.d(TAG, "onAppCreate")
    }

    /**
     * Initializes the Glia Core SDK using [GliaWidgetsConfig].
     *
     * @param gliaWidgetsConfig Glia configuration
     * @throws GliaWidgetsException with [GliaWidgetsException.Cause]
     */
    @JvmStatic
    @Synchronized
    fun init(gliaWidgetsConfig: GliaWidgetsConfig) {
        Logger.i(TAG, "Initialize Glia Widgets SDK")
        try {
            onSdkInit(gliaWidgetsConfig)
            setupLoggingMetadata(gliaWidgetsConfig)
            gliaThemeManager.applyJsonConfig(gliaWidgetsConfig.uiJsonRemoteConfig)
        } catch (gliaException: GliaException) {
            throw mapCoreExceptionToWidgets(gliaException)
        }
    }

    /**
     * Retrieves an instance of [EngagementLauncher].
     *
     * @param queueIds A list of queue IDs to be used for the engagement launcher.
     * When empty or invalid, the default queues will be used.
     * @return An instance of [EngagementLauncher].
     * @throws GliaWidgetsException with the [GliaWidgetsException.Cause.INVALID_INPUT] if the SDK is not initialized.
     */
    @Synchronized
    fun getEngagementLauncher(queueIds: List<String>): EngagementLauncher {
        Logger.i(TAG, "Returning an Engagement Launcher")
        try {
            setupQueueIds(queueIds)
            return engagementLauncher
        } catch (gliaException: GliaException) {
            throw mapCoreExceptionToWidgets(gliaException)
        }
    }

    /**
     * Retrieves an instance of [EntryWidget].
     *
     * @param queueIds A list of queue IDs to be used for the entry widget.
     * When empty or invalid, the default queues will be used.
     * @return An instance of [EntryWidget].
     * @throws GliaWidgetsException with the [GliaWidgetsException.Cause.INVALID_INPUT] if the SDK is not initialized.
     */
    @Synchronized
    fun getEntryWidget(queueIds: List<String>): EntryWidget {
        if (!glia().isInitialized) {
            Logger.e(TAG, "Attempt to get EntryWidget before SDK initialization")
        }

        try {
            configurationManager.setQueueIds(queueIds)
            return entryWidget
        } catch (gliaException: GliaException) {
            throw mapCoreExceptionToWidgets(gliaException)
        }
    }

    /**
     * Accepts permissions request results.
     *
     *
     * Some functionalities, for example Video or Audio calls, require to request runtime permissions via
     * [][<a href=]//developer.android.com/reference/android/app/Activity.html.requestPermissions">&lt;a href=&quot;https://developer.android.com/reference/android/app/Activity.html#requestPermissions(java.lang.String[],%20int)&quot;&gt;Activity#requestPermissions(String[], int)&lt;/a&gt;.
     * The results of such request is passed to your activity's
     * [][ <a href=]//developer.android.com/reference/android/app/Activity.html.onRequestPermissionsResult"> &lt;a href=&quot;https://developer.android.com/reference/android/app/Activity.html#onRequestPermissionsResult(int,%2520java.lang.String%5B%5D,%2520int%5B%5D)&quot;&gt;Activity#onRequestPermissionsResult(int, String[], int[])&lt;/a&gt;
     *
     *
     * Your activity in turn must call this method to pass the results of the request to Glia SDK.
     *
     *
     * This method is no-op for other non-Glia triggered results.
     *
     */
    @Deprecated("This method is no longer required, as all the required permissions are now managed internally.")
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Logger.d(TAG, "onRequestPermissionsResult")
        try {
            glia().onRequestPermissionsResult(requestCode, permissions, grantResults)
        } catch (gliaException: GliaException) {
            throw mapCoreExceptionToWidgets(gliaException)
        }
    }

    /**
     * Accepts permissions request results.
     *
     *
     * Some functionalities, for example Video or Audio calls, require to request runtime permissions via
     * [][<a href=]//developer.android.com/reference/android/app/Activity.html.requestPermissions">&lt;a href=&quot;https://developer.android.com/reference/android/app/Activity.html#requestPermissions(java.lang.String[],%20int)&quot;&gt;Activity#requestPermissions(String[], int)&lt;/a&gt;.
     * The results of such request is passed to your activity's
     * [][ <a href=]//developer.android.com/reference/android/app/Activity.html.onRequestPermissionsResult"> &lt;a href=&quot;https://developer.android.com/reference/android/app/Activity.html#onRequestPermissionsResult(int,%2520java.lang.String%5B%5D,%2520int%5B%5D)&quot;&gt;Activity#onRequestPermissionsResult(int, String[], int[])&lt;/a&gt;
     *
     *
     * Your activity in turn must call this method to pass the results of the request to Glia SDK.
     *
     *
     * This method is no-op for other non-Glia triggered results.
     *
     */
    @Deprecated("This method is no longer required, as required activity results are now managed internally.")
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Logger.d(TAG, "onActivityResult")
        try {
            repositoryFactory.engagementRepository.onActivityResult(requestCode, resultCode, data)
        } catch (gliaException: GliaException) {
            throw mapCoreExceptionToWidgets(gliaException)
        }
    }

    /**
     * Fetches the visitor's information
     *
     * @param onSuccess Callback invoked when the visitor information is successfully retrieved.
     *                  Provides the retrieved [VisitorInfo] or `null` if no information is available.
     * @param onError Callback invoked when an error occurs during the retrieval process.
     *                Provides a [GliaWidgetsException] describing the error.
     */
    fun getVisitorInfo(
        onSuccess: OnSuccess<VisitorInfo?>,
        onError: OnError
    ) {
        val callback =
            RequestCallback { visitorInfo: com.glia.androidsdk.visitor.VisitorInfo?,
                              error: GliaException? ->
                if (error != null || visitorInfo == null) {
                    onError.onError(from(error))
                } else {
                    onSuccess.onSuccess(VisitorInfo(visitorInfo))
                }
            }
        glia().getVisitorInfo(callback)
    }

    /**
     * Updates the visitor's information
     *
     * @param visitorInfoUpdateRequest The request containing the updated visitor information.
     * @param onComplete Callback invoked when the update operation is successfully completed.
     * @param onError Callback invoked when an error occurs during the update operation.
     *                Provides a [GliaWidgetsException] describing the error.
     */
    fun updateVisitorInfo(
        visitorInfoUpdateRequest: VisitorInfoUpdateRequest,
        onComplete: OnComplete,
        onError: OnError
    ) {
        val updateCallback =
            Consumer { error: GliaException? ->
                if (error == null) {
                    onComplete.onComplete()
                } else {
                    onError.onError(from(error))
                }
            }
        glia().updateVisitorInfo(visitorInfoUpdateRequest.toCoreType(), updateCallback)
    }

    /**
     * Clears visitor session
     * @throws GliaWidgetsException with [GliaWidgetsException.Cause]
     */
    fun clearVisitorSession() {
        Logger.i(TAG, "Clear visitor session")
        try {
            destroyControllersAndResetEngagementData()

            //Here we reset the secure conversations repository to clear the data,
            // because the visitor session is cleared(de-authenticated)
            //and we don't need secure conversations data for un-authenticated visitors.
            repositoryFactory.secureConversationsRepository.unsubscribeAndResetData()

            glia().clearVisitorSession()
        } catch (gliaException: GliaException) {
            throw mapCoreExceptionToWidgets(gliaException)
        }
    }

    /**
     * Ends active engagement
     *
     *
     * Ends active engagement if existing and closes Widgets SDK UI (includes bubble).
     * @throws GliaWidgetsException with [GliaWidgetsException.Cause]
     */
    fun endEngagement() {
        Logger.i(TAG, "End engagement by integrator")
        try {
            useCaseFactory.endEngagementUseCase.invoke(EndedBy.CLEAR_STATE)
        } catch (gliaException: GliaException) {
            throw mapCoreExceptionToWidgets(gliaException)
        }
    }

    /**
     * Creates `Authentication` instance for a given JWT token.
     *
     * @param behavior authentication behavior
     * @return `Authentication` object or throws [GliaWidgetsException] if error happened.
     * Exception may have the following cause:
     * [GliaWidgetsException.Cause.INVALID_INPUT] - when SDK is not initialized
     */
    fun getAuthentication(behavior: Authentication.Behavior): Authentication {
        try {
            val authentication = glia().getAuthenticationManager(behavior)
            setAuthenticationManager(authentication)
            return authentication
        } catch (gliaException: GliaException) {
            throw mapCoreExceptionToWidgets(gliaException)
        }
    }

    private fun setupQueueIds(queueIds: List<String>) {
        glia().ensureInitialized()

        configurationManager.setQueueIds(queueIds)
    }

    private fun setupLoggingMetadata(gliaWidgetsConfig: GliaWidgetsConfig) {
        addGlobalMetadata(
            Collections.singletonMap<String, String?>(
                SITE_ID_KEY,
                gliaWidgetsConfig.siteId
            )
        )
    }

    // More info about global Rx error handler:
    // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
    private fun setupRxErrorHandler() {
        RxJavaPlugins.setErrorHandler { throwable: Throwable ->
            val error = (throwable as? UndeliverableException)?.cause ?: throwable
            when (error) {
                is IOException, is InterruptedException -> {
                    // Ignored: network issues or interruptions
                }

                is NullPointerException, is IllegalArgumentException -> {
                    // Application bug
                    throwUncaughtException(error)
                }

                is IllegalStateException -> {
                    // RxJava or custom operator bug
                    throwUncaughtException(error)
                }

                else -> {
                    // Log other undeliverable exceptions
                    logUndeliverableException(error)
                }
            }
        }
    }

    private fun throwUncaughtException(e: Throwable) {
        val handler = Thread.currentThread().uncaughtExceptionHandler
        handler?.uncaughtException(Thread.currentThread(), e)
    }

    private fun logUndeliverableException(e: Throwable?) {
        var message: String? = "Exception message: "
        if (e != null) message += e.message
        Logger.e(
            "RxErrorHandler",
            "Undeliverable exception received, not sure what to do. $message"
        )
    }

    private fun mapCoreExceptionToWidgets(gliaException: GliaException): RuntimeException {
        val widgetsException = from(gliaException)
        return widgetsException ?: gliaException
    }
}
