package com.glia.widgets.di

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Glia
import com.glia.androidsdk.Glia.OmnicoreEvent
import com.glia.androidsdk.GliaConfig
import com.glia.androidsdk.GliaException
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
import com.glia.androidsdk.visitor.VisitorInfo
import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest
import java.io.InputStream
import java.util.Optional
import java.util.function.Consumer

internal class GliaCoreImpl : GliaCore {
    override var isInitialized: Boolean = false

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
    @Throws(GliaException::class)
    override fun init(config: GliaConfig) {
        Glia.init(config)
    }

    @Synchronized
    @Throws(GliaException::class)
    override fun init(config: GliaConfig, callback: RequestCallback<Boolean?>) {
        Glia.init(config, callback)
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

    override fun getQueues(onSuccess: (Array<Queue>) -> Unit, onError: (GliaException?) -> Unit) {
        Glia.getQueues { queues, gliaException ->
            queues?.also { onSuccess(it) } ?: onError(gliaException)
        }
    }

    override fun queueForEngagement(
        queueIds: List<String>,
        mediaType: Engagement.MediaType,
        visitorContextAssetId: String?,
        engagementOptions: EngagementOptions?,
        mediaPermissionRequestCode: Int,
        replaceExisting: Boolean,
        callback: Consumer<GliaException?>
    ) {
        Glia.queueForEngagement(
            queueIds.toTypedArray(),
            mediaType,
            visitorContextAssetId,
            engagementOptions,
            mediaPermissionRequestCode,
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Glia.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
}
