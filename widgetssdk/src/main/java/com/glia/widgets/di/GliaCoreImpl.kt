package com.glia.widgets.di

import android.content.Context
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Glia
import com.glia.androidsdk.Glia.OmnicoreEvent
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.NetworkTracker
import com.glia.androidsdk.Operator
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.comms.EngagementOptions
import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.fcm.PushNotifications
import com.glia.androidsdk.liveobservation.LiveObservation
import com.glia.androidsdk.omnibrowse.Omnibrowse
import com.glia.androidsdk.queuing.Queue
import com.glia.androidsdk.queuing.QueueTicket
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.androidsdk.site.SiteInfo
import com.glia.androidsdk.visitor.Authentication
import com.glia.androidsdk.visitor.Visitor
import com.glia.androidsdk.visitor.VisitorInfo
import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.GliaWidgets.TAG
import com.glia.widgets.GliaWidgetsConfig
import com.glia.widgets.GliaWidgetsException
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.toCoreType
import com.glia.widgets.queue.toCoreType
import com.glia.widgets.toWidgetsType
import java.io.InputStream
import java.util.Optional
import java.util.function.Consumer

internal class GliaCoreImpl : GliaCore {
    // GliaWidgets initialization process: The Core SDK is initialized first, followed by GliaWidgets.
    // The `isInitialized` property represents the initialization state of the Core SDK (via `Glia.isInitialized()`).
    // It is not connected to the Widgets-specific `GliaWidgets.isInitialized()` Boolean.
    override val isInitialized: Boolean
        get() = Glia.isInitialized()

    override val isInitializationInProgress: Boolean
        get() = Glia.isInitInProgress()

    override val pushNotifications: PushNotifications
        get() = Glia.getPushNotifications()

    override val currentEngagement: Optional<Engagement>
        get() = Glia.getCurrentEngagement()

    override val callVisualizer: Omnibrowse
        get() = Glia.omnibrowse

    override val secureConversations: SecureConversations
        get() = Glia.getSecureConversations()

    override val liveObservation: LiveObservation
        get() = Glia.getLiveObservation()

    @Synchronized
    @Throws(GliaWidgetsException::class)
    override fun init(config: GliaWidgetsConfig) {
        try {
            @Suppress("DEPRECATION")
            Glia.init(config.toCoreType())
        } catch (gliaException: GliaException) {
            throw gliaException.toWidgetsType()
        }
    }

    @Synchronized
    override fun init(config: GliaWidgetsConfig, onComplete: OnComplete, onError: OnError) {
        // The initialization result is delivered asynchronously via the callbacks, but config
        // mapping and `Glia.init` can still throw synchronously (e.g. missing required
        // configuration fields). The `try` routes those failures to `onError` as well, so
        // integrators get every failure through a single channel instead of a thrown exception.
        try {
            Glia.init(config.toCoreType(), onComplete::onComplete) {
                reportInitializationFailure(onError, it.toInitializationError(), it)
            }
        } catch (gliaWidgetsException: GliaWidgetsException) {
            reportInitializationFailure(onError, gliaWidgetsException)
        } catch (gliaException: GliaException) {
            reportInitializationFailure(onError, gliaException.toWidgetsType(), gliaException)
        } catch (ex: Exception) {
            reportInitializationFailure(onError, GliaWidgetsException("Internal SDK error", GliaWidgetsException.Cause.INTERNAL_ERROR), ex)
        }
    }

    private fun reportInitializationFailure(onError: OnError, error: GliaWidgetsException, loggedError: Throwable = error) {
        onError.onError(error)

        Logger.e(TAG, "Glia Widgets SDK initialization failed", loggedError)
        GliaLogger.e(LogEvents.WIDGETS_SDK_UNCATEGORIZED, "Glia Widgets SDK initialization failed", loggedError)
    }

    private fun GliaException.toInitializationError(): GliaWidgetsException =
        when (cause) {
            GliaException.Cause.ALREADY_INITIALIZED -> {
                GliaWidgetsException(
                    "Glia Widgets SDK is already initialized or initialization is already in progress.",
                    GliaWidgetsException.Cause.INVALID_INPUT
                )
            }

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

            GliaException.Cause.AUTHENTICATION_ERROR -> {
                GliaWidgetsException(
                    "Failed to initialise Glia Widgets SDK. Authentication error. Please check credentials.",
                    GliaWidgetsException.Cause.AUTHENTICATION_ERROR
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

    override fun getVisitorInfo(visitorCallback: RequestCallback<VisitorInfo?>) {
        Glia.getVisitorInfo(visitorCallback)
    }

    override fun updateVisitorInfo(visitorInfoUpdateRequest: VisitorInfoUpdateRequest, visitorCallback: Consumer<GliaException?>) {
        Glia.updateVisitorInfo(visitorInfoUpdateRequest, visitorCallback)
    }

    override fun <T> on(event: OmnicoreEvent<T>, listener: Consumer<T>) {
        Glia.on(event, listener)
    }

    override fun <T> off(event: OmnicoreEvent<T>, listener: Consumer<T>) {
        Glia.off(event, listener)
    }

    override fun <T> off(event: OmnicoreEvent<T>) {
        Glia.off(event)
    }

    override fun fetchFile(attachmentFile: AttachmentFile, callback: RequestCallback<InputStream?>) {
        Glia.fetchFile(attachmentFile, callback)
    }

    override fun getChatHistory(callback: RequestCallback<List<ChatMessage>?>) {
        Glia.getChatHistory { messages, exception -> callback.onResult(messages?.toList(), exception) }
    }

    override fun getQueues(onResult: (Array<Queue>) -> Unit, onError: (GliaException?) -> Unit) {
        Glia.getQueues { queues, gliaException ->
            queues?.also { onResult(it) } ?: onError(gliaException)
        }
    }

    override fun queueForEngagement(
        queueIds: List<String>,
        mediaType: MediaType,
        visitorContextAssetId: String?,
        engagementOptions: EngagementOptions?,
        replaceExisting: Boolean,
        callback: Consumer<GliaException?>
    ) {
        Glia.queueForEngagement(
            queueIds.toTypedArray(),
            mediaType.toCoreType(),
            visitorContextAssetId,
            engagementOptions,
            replaceExisting,
            callback
        )
    }

    override fun cancelQueueTicket(queueTicketId: String, callback: Consumer<GliaException?>) {
        Glia.cancelQueueTicket(queueTicketId, callback)
    }

    override fun subscribeToQueueTicketUpdates(ticketId: String, callback: RequestCallback<QueueTicket?>) {
        Glia.subscribeToQueueTicketUpdates(ticketId, callback)
    }

    override fun submitSurveyAnswers(answers: List<Survey.Answer>, surveyId: String, engagementId: String, callback: Consumer<GliaException?>) {
        Glia.submitSurveyAnswers(answers, surveyId, engagementId, callback)
    }

    override fun getSiteInfo(callback: RequestCallback<SiteInfo?>) {
        Glia.getSiteInfo(callback)
    }

    override fun clearVisitorSession() {
        Glia.clearVisitorSession()
    }

    override fun getOperator(operatorId: String, callback: RequestCallback<Operator?>) {
        Glia.getOperator(operatorId, callback)
    }

    override fun getAuthentication(behavior: Authentication.Behavior): Authentication = Glia.getAuthentication(behavior)

    override fun subscribeToQueueStateUpdates(queueIds: List<String>, onError: Consumer<GliaException>, callback: Consumer<Queue>) {
        Glia.subscribeToQueueStateUpdates(queueIds.toTypedArray(), onError, callback)
    }

    override fun unsubscribeFromQueueUpdates(onError: Consumer<GliaException>?, callback: Consumer<Queue>) {
        Glia.unsubscribeFromQueueUpdates(onError, callback)
    }

    override fun getCurrentVisitor(onSuccess: (Visitor) -> Unit) = Glia.getCurrentVisitor { visitor, _ ->
        onSuccess(visitor ?: return@getCurrentVisitor)
    }

    override fun getNetworkTracker(context: Context): NetworkTracker = Glia.getNetworkTracker(context)
}
