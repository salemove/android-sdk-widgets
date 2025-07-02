package com.glia.widgets.launcher

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.chat.Intention
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.IntentHelper
import com.glia.widgets.helper.safeStartActivity
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.locale.LocaleString


internal interface ActivityLauncher {
    fun launchChat(context: Context, intention: Intention)
    fun launchCall(context: Context, mediaType: MediaType?, upgradeToCall: Boolean)
    fun launchSecureMessagingWelcomeScreen(context: Context)
    fun launchWebBrowser(context: Context, title: LocaleString, url: String)
    fun launchOverlayPermission(context: Context, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {})
    fun launchImagePreview(context: Context, attachment: AttachmentFile, options: Bundle? = null)
    fun launchImagePreview(context: Context, attachment: LocalAttachment, options: Bundle? = null)
    fun launchEmailClient(context: Context, uri: Uri, onFailure: () -> Unit)
    fun launchDialer(context: Context, uri: Uri, onFailure: () -> Unit)
    fun launchUri(context: Context, uri: Uri, onFailure: () -> Unit)
    fun launchFileReader(context: Context, contentUri: Uri, fileContentType: String?, onFailure: () -> Unit)
    fun launchShareImage(activity: Activity, fileName: String)
    fun launchEntryWidget(activity: Activity)
    fun launchSurvey(activity: Activity, survey: Survey)
}

internal class ActivityLauncherImpl(
    private val intentHelper: IntentHelper,
    private val engagementRepository: EngagementRepository
) : ActivityLauncher {

    override fun launchChat(context: Context, intention: Intention) {
        engagementRepository.updateIsSecureMessagingRequested(intention.isSecureConversation)
        context.startActivity(intentHelper.chatIntent(context, intention))
    }

    override fun launchCall(context: Context, mediaType: MediaType?, upgradeToCall: Boolean) {
        engagementRepository.updateIsSecureMessagingRequested(false)
        context.startActivity(intentHelper.callIntent(context, mediaType, upgradeToCall))
    }

    override fun launchSecureMessagingWelcomeScreen(context: Context) {
        engagementRepository.updateIsSecureMessagingRequested(true)
        context.startActivity(intentHelper.secureMessagingWelcomeScreenIntent(context))
    }

    override fun launchWebBrowser(context: Context, title: LocaleString, url: String) =
        context.startActivity(intentHelper.webBrowserIntent(context, title, url))

    override fun launchOverlayPermission(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) =
        context.safeStartActivity(intentHelper.overlayPermissionIntent(context), onFailure, onSuccess)

    override fun launchImagePreview(context: Context, attachment: AttachmentFile, options: Bundle?) =
        context.startActivity(intentHelper.imagePreviewIntent(context, attachment), options)

    override fun launchImagePreview(context: Context, attachment: LocalAttachment, options: Bundle?) =
        context.startActivity(intentHelper.imagePreviewIntent(context, attachment), options)

    override fun launchEmailClient(context: Context, uri: Uri, onFailure: () -> Unit) =
        context.safeStartActivity(intentHelper.openEmailIntent(uri), onFailure)

    override fun launchDialer(context: Context, uri: Uri, onFailure: () -> Unit) =
        context.safeStartActivity(intentHelper.dialerIntent(uri), onFailure)

    override fun launchUri(context: Context, uri: Uri, onFailure: () -> Unit) = context.safeStartActivity(intentHelper.openUriIntent(uri), onFailure)

    override fun launchFileReader(context: Context, contentUri: Uri, fileContentType: String?, onFailure: () -> Unit) =
        context.safeStartActivity(intentHelper.openFileIntent(contentUri, fileContentType), onFailure)

    override fun launchShareImage(activity: Activity, fileName: String) = activity.startActivity(intentHelper.shareImageIntent(activity, fileName))

    override fun launchEntryWidget(activity: Activity) = activity.startActivity(intentHelper.entryWidgetIntent(activity))

    override fun launchSurvey(activity: Activity, survey: Survey) = activity.startActivity(intentHelper.surveyIntent(activity, survey))
}
