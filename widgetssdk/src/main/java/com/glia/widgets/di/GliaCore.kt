package com.glia.widgets.di

import android.app.Application
import com.glia.androidsdk.Engagement
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

interface GliaCore {
    val isInitialized: Boolean
    val pushNotifications: PushNotifications
    val currentEngagement: Optional<Engagement>
    val callVisualizer: Omnibrowse
    val secureConversations: SecureConversations
    @Throws(GliaException::class)
    fun init(config: GliaConfig)
    @Throws(GliaException::class)
    fun onAppCreate(application: Application)
    fun getVisitorInfo(visitorCallback: RequestCallback<VisitorInfo?>)
    fun updateVisitorInfo(visitorInfoUpdateRequest: VisitorInfoUpdateRequest, visitorCallback: Consumer<GliaException?>)
    fun <T> on(event: OmnicoreEvent<T>, listener: Consumer<T>)
    fun <T> off(event: OmnicoreEvent<T>, listener: Consumer<T>)
    fun <T> off(event: OmnicoreEvent<T>)
    fun fetchFile(attachmentFile: AttachmentFile, callback: RequestCallback<InputStream?>)
    fun getChatHistory(callback: RequestCallback<Array<ChatMessage>?>)
    fun getQueues(requestCallback: RequestCallback<Array<Queue>?>)
    fun queueForEngagement(queueId: String, visitorContextAssetId: String?, callback: Consumer<GliaException?>)

    fun queueForEngagement(
        queueId: String,
        mediaType: Engagement.MediaType,
        visitorContextAssetId: String?,
        engagementOptions: EngagementOptions?,
        mediaPermissionRequestCode: Int,
        callback: Consumer<GliaException?>)

    fun cancelQueueTicket(queueTicketId: String, callback: Consumer<GliaException?>)

    fun subscribeToQueueTicketUpdates(ticketId: String, callback: RequestCallback<QueueTicket?>)
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)

    fun submitSurveyAnswers(answers: List<Survey.Answer>, surveyId: String, engagementId: String, callback: Consumer<GliaException?>)
    fun clearVisitorSession()
    fun getSiteInfo(callback: RequestCallback<SiteInfo?>)
    fun getOperator(operatorId: String, callback: RequestCallback<Operator?>)
    fun getAuthentication(behavior: Authentication.Behavior): Authentication
}
