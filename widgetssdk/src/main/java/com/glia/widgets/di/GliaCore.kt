package com.glia.widgets.di

import com.glia.androidsdk.CoreConfiguration
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Glia.OmnicoreEvent
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
import com.glia.androidsdk.visitor.Visitor
import com.glia.androidsdk.visitor.VisitorInfo
import com.glia.androidsdk.visitor.VisitorInfoUpdateRequest
import com.glia.widgets.engagement.MediaType
import java.io.InputStream
import java.util.Optional
import java.util.function.Consumer

internal interface GliaCore {
    val isInitialized: Boolean
    val pushNotifications: PushNotifications
    val currentEngagement: Optional<Engagement>
    val callVisualizer: Omnibrowse
    val secureConversations: SecureConversations
    val liveObservation: LiveObservation

    @Throws(GliaException::class)
    fun init(config: CoreConfiguration)
    fun init(config: CoreConfiguration, callback: RequestCallback<Boolean?>)
    fun getVisitorInfo(visitorCallback: RequestCallback<VisitorInfo?>)
    fun updateVisitorInfo(visitorInfoUpdateRequest: VisitorInfoUpdateRequest, visitorCallback: Consumer<GliaException?>)
    fun <T> on(event: OmnicoreEvent<T>, listener: Consumer<T>)
    fun <T> off(event: OmnicoreEvent<T>, listener: Consumer<T>)
    fun <T> off(event: OmnicoreEvent<T>)
    fun fetchFile(attachmentFile: AttachmentFile, callback: RequestCallback<InputStream?>)
    fun getChatHistory(callback: RequestCallback<List<ChatMessage>?>)
    fun getQueues(onResult: (Array<Queue>) -> Unit, onError: (GliaException?) -> Unit)

    fun queueForEngagement(
        queueIds: List<String>,
        mediaType: MediaType,
        visitorContextAssetId: String?,
        engagementOptions: EngagementOptions?,
        replaceExisting: Boolean,
        callback: Consumer<GliaException?>
    )

    fun cancelQueueTicket(queueTicketId: String, callback: Consumer<GliaException?>)

    fun subscribeToQueueTicketUpdates(ticketId: String, callback: RequestCallback<QueueTicket?>)

    fun submitSurveyAnswers(answers: List<Survey.Answer>, surveyId: String, engagementId: String, callback: Consumer<GliaException?>)
    fun clearVisitorSession()
    fun getSiteInfo(callback: RequestCallback<SiteInfo?>)
    fun getOperator(operatorId: String, callback: RequestCallback<Operator?>)
    fun getAuthentication(behavior: Authentication.Behavior): Authentication

    fun ensureInitialized() {
        if (!isInitialized) {
            throw GliaException("Glia SDK is not initialized", GliaException.Cause.INVALID_INPUT)
        }
    }

    fun subscribeToQueueStateUpdates(queueIds: List<String>, onError: Consumer<GliaException>, callback: Consumer<Queue>)
    fun unsubscribeFromQueueUpdates(onError: Consumer<GliaException>?, callback: Consumer<Queue>)
    fun getCurrentVisitor(onSuccess: (Visitor) -> Unit)
}
