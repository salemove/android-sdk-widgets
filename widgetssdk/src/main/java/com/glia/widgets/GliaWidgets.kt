package com.glia.widgets

import android.app.Application
import android.content.Intent
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.telemetry_lib.SdkType
import com.glia.widgets.authentication.Authentication
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.callbacks.OnResult
import com.glia.widgets.chat.adapter.CustomCardAdapter
import com.glia.widgets.chat.adapter.WebViewCardAdapter
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.Dependencies.callVisualizerManager
import com.glia.widgets.di.Dependencies.configurationManager
import com.glia.widgets.di.Dependencies.destroyControllersAndResetEngagementData
import com.glia.widgets.di.Dependencies.engagementLauncher
import com.glia.widgets.di.Dependencies.entryWidget
import com.glia.widgets.di.Dependencies.getAuthenticationManager
import com.glia.widgets.di.Dependencies.gliaCore
import com.glia.widgets.di.Dependencies.gliaThemeManager
import com.glia.widgets.di.Dependencies.liveObservation
import com.glia.widgets.di.Dependencies.pushNotifications
import com.glia.widgets.di.Dependencies.repositoryFactory
import com.glia.widgets.di.Dependencies.secureConversations
import com.glia.widgets.di.Dependencies.useCaseFactory
import com.glia.widgets.entrywidget.EntryWidget
import com.glia.widgets.fcm.PushNotifications
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.Logger.SITE_ID_KEY
import com.glia.widgets.helper.Logger.addGlobalMetadata
import com.glia.widgets.helper.orNotApplicable
import com.glia.widgets.internal.authentication.toCoreType
import com.glia.widgets.internal.authentication.toWidgetsType
import com.glia.widgets.launcher.EngagementLauncher
import com.glia.widgets.liveobservation.LiveObservation
import com.glia.widgets.queue.Queue
import com.glia.widgets.queue.toWidgetsType
import com.glia.widgets.secureconversations.SecureConversations
import com.glia.widgets.visitor.VisitorInfo
import com.glia.widgets.visitor.VisitorInfoUpdateRequest
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.io.IOException
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
        get() {
            GliaLogger.logMethodUse(GliaWidgets::class, "widgetsSdkVersion")
            return BuildConfig.GLIA_WIDGETS_SDK_VERSION
        }

    /**
     * Build-time version of Glia Core SDK that is used by Glia Widgets SDK
     */
    @JvmStatic
    val widgetsCoreSdkVersion: String
        get() {
            GliaLogger.logMethodUse(GliaWidgets::class, "widgetsCoreSdkVersion")
            return BuildConfig.GLIA_CORE_SDK_VERSION
        }

    private var isInitialized = false

    private var _customCardAdapter: CustomCardAdapter? = WebViewCardAdapter()

    /**
     * CallVisualizer manager that provides controls related to the [CallVisualizer] module.
     */
    @JvmStatic
    fun getCallVisualizer(): CallVisualizer {
        GliaLogger.logMethodUse(GliaWidgets::class, "getCallVisualizer")
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
        GliaLogger.logMethodUse(GliaWidgets::class, "setCustomCardAdapter")
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
        GliaLogger.logMethodUse(GliaWidgets::class, "getCustomCardAdapter")
        return _customCardAdapter
    }

    /**
     * This method is obsolete and no longer required.
     * It is safe to remove calls to this method.
     *
     * @param application the application where it is initialized
     * @throws GliaWidgetsException with [GliaWidgetsException.Cause]
     */
    @Deprecated("No longer required to call this method")
    @JvmStatic
    @Synchronized
    fun onAppCreate(application: Application) {
        GliaLogger.logDeprecatedApiUse(SdkType.WIDGETS_SDK, GliaWidgets::class, "onAppCreate")
        try {
            Dependencies.onAppCreate(application)
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
        }
        setupRxErrorHandler()
        Logger.d(TAG, "onAppCreate")
    }

    /**
     * Initializes the Glia Core SDK using [GliaWidgetsConfig].
     * [GliaWidgets.isInitialized] will return `true` regardless of the initialization result.
     *
     * @param gliaWidgetsConfig Glia configuration
     * @throws GliaWidgetsException with [GliaWidgetsException.Cause]
     */
    @JvmStatic
    @Synchronized
    fun init(gliaWidgetsConfig: GliaWidgetsConfig) {
        GliaLogger.logMethodUse(GliaWidgets::class, "init")
        Logger.i(TAG, "Initialize Glia Widgets SDK")
        try {
            Dependencies.onSdkInit(gliaWidgetsConfig)
            setupLoggingMetadata(gliaWidgetsConfig)
            gliaThemeManager.applyJsonConfig(gliaWidgetsConfig.uiJsonRemoteConfig)
            isInitialized = true
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
        }
    }

    /**
     * Initializes the Glia Core SDK using [GliaWidgetsConfig].
     * [GliaWidgets.isInitialized] will return `true` after initialization succeeds.
     *
     * @param gliaWidgetsConfig Glia configuration
     * @throws GliaWidgetsException with [GliaWidgetsException.Cause]
     */
    @JvmStatic
    @Synchronized
    fun init(gliaWidgetsConfig: GliaWidgetsConfig, onComplete: OnComplete, onError: OnError) {
        GliaLogger.logMethodUse(GliaWidgets::class, "init", "onComplete", "onError")
        Logger.i(TAG, "Initialize Glia Widgets SDK")
        try {
            val callback: RequestCallback<Boolean?> = RequestCallback { _, exception ->
                if (exception == null) {
                    isInitialized = true
                    onComplete.onComplete()
                } else {
                    val invalidInputError = when (exception.cause) {
                        GliaException.Cause.NETWORK_TIMEOUT -> {
                            GliaWidgetsException(
                                "Network timeout. Please check the Internet connection.",
                                GliaWidgetsException.Cause.NETWORK_TIMEOUT
                            )
                        }

                        GliaException.Cause.INVALID_INPUT -> {
                            GliaWidgetsException(
                                "Failed to initialise Glia Widgets SDK. Invalid input. Please check credentials.",
                                GliaWidgetsException.Cause.INVALID_INPUT
                            )
                        }

                        GliaException.Cause.FORBIDDEN -> {
                            GliaWidgetsException(
                                "Failed to initialise Glia Widgets SDK. Forbidden. Please check credentials.",
                                GliaWidgetsException.Cause.INVALID_INPUT
                            )
                        }

                        else -> {
                            GliaWidgetsException(
                                "Failed to initialise Glia Widgets SDK. Please check logs.",
                                GliaWidgetsException.Cause.INVALID_INPUT
                            )
                        }
                    }
                    onError.onError(invalidInputError)

                    Logger.e(TAG, "Glia Widgets SDK initialization failed", invalidInputError)
                    GliaLogger.e(LogEvents.WIDGETS_SDK_UNCATEGORIZED, "Glia Widgets SDK initialization failed", invalidInputError)
                }
            }

            Dependencies.onSdkInit(gliaWidgetsConfig, callback)
            setupLoggingMetadata(gliaWidgetsConfig)
            gliaThemeManager.applyJsonConfig(gliaWidgetsConfig.uiJsonRemoteConfig)
        } catch (gliaWidgetsException: GliaWidgetsException) {
            onError.onError(gliaWidgetsException)

            Logger.e(TAG, "Glia Widgets SDK initialization failed", gliaWidgetsException)
            GliaLogger.e(LogEvents.WIDGETS_SDK_UNCATEGORIZED, "Glia Widgets SDK initialization failed", gliaWidgetsException)
        } catch (gliaException: GliaException) {
            onError.onError(gliaException.toWidgetsType())

            Logger.e(TAG, "Glia Widgets SDK initialization failed", gliaException)
            GliaLogger.e(LogEvents.WIDGETS_SDK_UNCATEGORIZED, "Glia Widgets SDK initialization failed", gliaException)
        } catch (ex: Exception) {
            val internalError = GliaWidgetsException("Internal SDK error", GliaWidgetsException.Cause.INTERNAL_ERROR)
            onError.onError(internalError)

            Logger.e(TAG, "Glia Widgets SDK initialization failed", ex)
            GliaLogger.e(LogEvents.WIDGETS_SDK_UNCATEGORIZED, "Glia Widgets SDK initialization failed", ex)
        }
    }

    /**
     * Checks result of Glia Widgets SDK initialization
     *
     * @return `true` if [GliaWidgets.init] is called without exceptions.
     *
     *  Please note, that `true` doesn't mean that credentials are valid/correct.
     * @see [GliaWidgets.init]
     */
    @JvmStatic
    fun isInitialized(): Boolean {
        GliaLogger.logMethodUse(GliaWidgets::class, "isInitialized")
        return isInitialized
    }

    /**
     * Fetches all queues and their information for the current site.
     *
     * @param onResult Callback invoked when the queues are successfully retrieved.
     *                  Provides a collection of [Queue] objects or `null` if no queues are available.
     * @param onError Callback invoked when an error occurs during the retrieval process.
     *                Provides a [GliaWidgetsException] describing the error.
     */
    @JvmStatic
    fun getQueues(
        onResult: OnResult<Collection<Queue>>,
        onError: OnError? = null
    ) {
        GliaLogger.logMethodUse(GliaWidgets::class, "getQueues")
        gliaCore().getQueues(
            onResult = { queues ->
                onResult.onResult(queues.toWidgetsType())
            },
            onError = {
                onError?.onError(it.toWidgetsType("Failed to get queues"))
            }
        )
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
    @JvmStatic
    fun getEngagementLauncher(queueIds: List<String>): EngagementLauncher {
        GliaLogger.logMethodUse(GliaWidgets::class, "getEngagementLauncher")
        Logger.i(TAG, "Returning an Engagement Launcher")
        try {
            setupQueueIds(queueIds)
            return engagementLauncher
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
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
    @JvmStatic
    fun getEntryWidget(queueIds: List<String>): EntryWidget {
        GliaLogger.logMethodUse(GliaWidgets::class, "getEntryWidget")
        if (!gliaCore().isInitialized) {
            Logger.e(TAG, "Attempt to get EntryWidget before SDK initialization")
        }

        try {
            configurationManager.setQueueIds(queueIds)
            return entryWidget
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
        }
    }

    /**
     * Accepts permissions request results.
     *
     *
     * Some functionalities, for example Video or Audio calls, require to request [runtime permissions](https://developer.android.com/reference/androidx/core/app/ActivityCompat?hl=en#requestPermissions(android.app.Activity,java.lang.String[],int)).
     * The results of such request is passed to your activity's [onRequestPermissionsResult()](https://developer.android.com/reference/androidx/core/app/ActivityCompat.OnRequestPermissionsResultCallback?hl=en#onRequestPermissionsResult(int,java.lang.String[],int[])) function.
     *
     * Your activity in turn must call this method to pass the results of the request to Glia SDK.
     *
     *
     * This method is no-op for other non-Glia triggered results.
     *
     */
    @Deprecated("This method is no longer required, as all the required permissions are now managed internally.")
    @JvmStatic
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // no op
    }

    /**
     * Accepts permissions request results.
     *
     * Some functionalities, for example Video or Audio calls, require to request [runtime permissions](https://developer.android.com/reference/androidx/core/app/ActivityCompat?hl=en#requestPermissions(android.app.Activity,java.lang.String[],int)).
     * The results of such request is passed to your activity's [onRequestPermissionsResult()](https://developer.android.com/reference/androidx/core/app/ActivityCompat.OnRequestPermissionsResultCallback?hl=en#onRequestPermissionsResult(int,java.lang.String[],int[])) function.
     *
     * Your activity in turn must call this method to pass the results of the request to Glia SDK.
     *
     * This method is no-op for other non-Glia triggered results.
     *
     */
    @Deprecated("This method is no longer required, as required activity results are now managed internally.")
    @JvmStatic
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        GliaLogger.logDeprecatedApiUse(SdkType.WIDGETS_SDK, GliaWidgets::class, "onActivityResult")
        Logger.d(TAG, "onActivityResult")
        try {
            repositoryFactory.engagementRepository.onActivityResult(requestCode, resultCode, data)
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
        }
    }

    /**
     * Fetches the visitor's information
     *
     * @param onResult Callback invoked when the visitor information is successfully retrieved.
     *                  Provides the retrieved [VisitorInfo] or `null` if no information is available.
     * @param onError Callback invoked when an error occurs during the retrieval process.
     *                Provides a [GliaWidgetsException] describing the error.
     */
    @JvmStatic
    fun getVisitorInfo(onResult: OnResult<VisitorInfo>, onError: OnError? = null) {
        GliaLogger.logMethodUse(GliaWidgets::class, "getVisitorInfo")
        val callback =
            RequestCallback { visitorInfo: com.glia.androidsdk.visitor.VisitorInfo?,
                              error: GliaException? ->
                if (error != null || visitorInfo == null) {
                    onError?.onError(error.toWidgetsType("Failed to get visitor info"))
                } else {
                    onResult.onResult(VisitorInfo(visitorInfo))
                }
            }

        try {
            gliaCore().getVisitorInfo(callback)
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
        }
    }

    /**
     * Updates the visitor's information
     *
     * @param visitorInfoUpdateRequest The request containing the updated visitor information.
     * @param onComplete Callback invoked when the update operation is successfully completed.
     * @param onError Callback invoked when an error occurs during the update operation.
     *                Provides a [GliaWidgetsException] describing the error.
     */
    @JvmStatic
    fun updateVisitorInfo(visitorInfoUpdateRequest: VisitorInfoUpdateRequest, onComplete: OnComplete, onError: OnError) {
        GliaLogger.logMethodUse(GliaWidgets::class, "updateVisitorInfo")
        val updateCallback =
            Consumer { error: GliaException? ->
                if (error == null) {
                    onComplete.onComplete()
                } else {
                    onError.onError(error.toWidgetsType())
                }
            }
        gliaCore().updateVisitorInfo(visitorInfoUpdateRequest.toCoreType(), updateCallback)
    }

    /**
     * Clears visitor session
     * @throws GliaWidgetsException with [GliaWidgetsException.Cause]
     */
    @JvmStatic
    fun clearVisitorSession() {
        GliaLogger.logMethodUse(GliaWidgets::class, "clearVisitorSession")
        Logger.i(TAG, "Clear visitor session")
        try {
            destroyControllersAndResetEngagementData()

            //Here we reset the secure conversations repository to clear the data,
            // because the visitor session is cleared(de-authenticated)
            //and we don't need secure conversations data for un-authenticated visitors.
            repositoryFactory.secureConversationsRepository.unsubscribeAndResetData()

            gliaCore().clearVisitorSession()
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
        }
    }

    /**
     * Ends active engagement
     *
     *
     * Ends active engagement if existing and closes Widgets SDK UI (includes bubble).
     * @throws GliaWidgetsException with [GliaWidgetsException.Cause]
     */
    @JvmStatic
    fun endEngagement() {
        GliaLogger.logMethodUse(GliaWidgets::class, "endEngagement")
        Logger.i(TAG, "End engagement by integrator")
        try {
            useCaseFactory.endEngagementUseCase.silently()
            // Here we destroy controllers to not keep queueing state for authenticated chat when it is minimized
            Dependencies.destroyControllers()
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
        }
    }

    /**
     * Creates `Authentication` instance for a given JWT token.
     *
     * @param behavior authentication behavior
     * @return `com.glia.androidsdk.visitor.Authentication` object or throws [GliaWidgetsException] if error happened.
     * Exception may have the following cause:
     * [GliaWidgetsException.Cause.INVALID_INPUT] - when SDK is not initialized
     */
    @Deprecated("Please use getAuthentication(behavior: com.glia.widgets.authentication.Authentication.Behavior)")
    @JvmStatic
    fun getAuthentication(behavior: com.glia.androidsdk.visitor.Authentication.Behavior): com.glia.androidsdk.visitor.Authentication {
        GliaLogger.logDeprecatedApiUse(SdkType.WIDGETS_SDK, GliaWidgets::class, "getAuthentication")
        try {
            return getAuthenticationManager(behavior.toWidgetsType()).toCoreType()
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
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
    @JvmStatic
    fun getAuthentication(behavior: Authentication.Behavior): Authentication {
        GliaLogger.logMethodUse(GliaWidgets::class, "getAuthentication")
        try {
            return getAuthenticationManager(behavior)
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
        }
    }

    /**
     * Creates `SecureConversations` instance for secure conversations.
     *
     * @return {@code SecureConversations} object or throws [GliaWidgetsException] if error happened.
     * Exception may have the following cause:
     * [GliaWidgetsException.Cause.INVALID_INPUT] - when SDK is not initialized
     */
    @Suppress("unused")
    @JvmStatic
    fun getSecureConversations(): SecureConversations {
        GliaLogger.logMethodUse(GliaWidgets::class, "getSecureConversations")
        try {
            return secureConversations
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
        }
    }

    /**
     * Handles Live Observation
     *
     * @return {@code LiveObservation} object or throws [GliaWidgetsException] if error happened.
     * Exception may have the following cause:
     * [GliaWidgetsException.Cause.INVALID_INPUT] - when SDK is not initialized or initialization failed.
     */
    @Suppress("unused")
    @JvmStatic
    fun getLiveObservation(): LiveObservation {
        GliaLogger.logMethodUse(GliaWidgets::class, "getLiveObservation")
        try {
            return liveObservation
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
        }
    }

    /**
     * Handles FCM tokens and push messages.
     *
     * @return [PushNotifications]
     */
    @JvmStatic
    fun getPushNotifications(): PushNotifications {
        GliaLogger.logMethodUse(GliaWidgets::class, "getPushNotifications")
        return pushNotifications
    }

    private fun setupQueueIds(queueIds: List<String>) {
        gliaCore().ensureInitialized()

        configurationManager.setQueueIds(queueIds)
    }

    private fun setupLoggingMetadata(gliaWidgetsConfig: GliaWidgetsConfig) {
        addGlobalMetadata(mapOf(Pair(SITE_ID_KEY, gliaWidgetsConfig.siteId.orNotApplicable)))
    }

    // More info about global Rx error handler:
    // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
    internal fun setupRxErrorHandler() {
        // Check if RxJava error handler is already set
        if (RxJavaPlugins.getErrorHandler() != null) {
            // RxJava error handler is already set by the application
            return
        }

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
}
