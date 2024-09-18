package com.glia.widgets.navigation

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.call.CallConfiguration
import com.glia.widgets.core.configuration.EngagementConfiguration
import com.glia.widgets.helper.IntentHelper
import com.glia.widgets.helper.safeStartActivity
import com.glia.widgets.locale.LocaleString


internal interface ActivityLauncher {
    fun launchChat(activity: Activity)
    fun launchChat(context: Context, engagementConfiguration: EngagementConfiguration)
    fun launchCall(context: Context, callConfiguration: CallConfiguration)
    fun launchCall(activity: Activity, mediaType: MediaType)
    fun launchSecureMessagingChat(activity: Activity)
    fun launchSecureMessagingWelcomeScreen(activity: Activity)
    fun launchEndScreenSharing(context: Context)
    fun launchWebBrowser(context: Context, title: LocaleString, url: String)
    fun launchOverlayPermission(context: Context, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {})
    fun launchImagePreview(context: Context, attachment: AttachmentFile, options: Bundle? = null)
    fun launchEmailClient(context: Context, uri: Uri, onFailure: () -> Unit)
    fun launchDialer(context: Context, uri: Uri, onFailure: () -> Unit)
    fun launchUri(context: Context, uri: Uri, onFailure: () -> Unit)
    fun launchFileReader(context: Context, contentUri: Uri, fileContentType: String, onFailure: () -> Unit)
    fun launchShareImage(activity: Activity, fileName: String)
}

internal class ActivityLauncherImpl(private val intentHelper: IntentHelper) : ActivityLauncher {

    override fun launchChat(activity: Activity) = activity.startActivity(intentHelper.chatIntent(activity, emptyList()))

    override fun launchChat(context: Context, engagementConfiguration: EngagementConfiguration) =
        context.startActivity(intentHelper.chatIntent(context, engagementConfiguration))

    override fun launchCall(context: Context, callConfiguration: CallConfiguration) =
        context.startActivity(intentHelper.callIntent(context, callConfiguration))

    override fun launchCall(activity: Activity, mediaType: MediaType) = activity.startActivity(intentHelper.callIntent(activity, mediaType))

    override fun launchSecureMessagingChat(activity: Activity) = activity.startActivity(intentHelper.secureMessagingChatIntent(activity))

    override fun launchSecureMessagingWelcomeScreen(activity: Activity) =
        activity.startActivity(intentHelper.secureMessagingWelcomeScreenIntent(activity))

    override fun launchEndScreenSharing(context: Context) = context.startActivity(intentHelper.endScreenSharingIntent(context))

    override fun launchWebBrowser(context: Context, title: LocaleString, url: String) =
        context.startActivity(intentHelper.webBrowserIntent(context, title, url))

    override fun launchOverlayPermission(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) =
        context.safeStartActivity(intentHelper.overlayPermissionIntent(context), onFailure, onSuccess)

    override fun launchImagePreview(context: Context, attachment: AttachmentFile, options: Bundle?) =
        context.startActivity(intentHelper.imagePreviewIntent(context, attachment), options)

    override fun launchEmailClient(context: Context, uri: Uri, onFailure: () -> Unit) =
        context.safeStartActivity(intentHelper.openEmailIntent(uri), onFailure)

    override fun launchDialer(context: Context, uri: Uri, onFailure: () -> Unit) =
        context.safeStartActivity(intentHelper.dialerIntent(uri), onFailure)

    override fun launchUri(context: Context, uri: Uri, onFailure: () -> Unit) = context.safeStartActivity(intentHelper.openUriIntent(uri), onFailure)

    override fun launchFileReader(context: Context, contentUri: Uri, fileContentType: String, onFailure: () -> Unit) =
        context.safeStartActivity(intentHelper.openFileIntent(contentUri, fileContentType), onFailure)

    override fun launchShareImage(activity: Activity, fileName: String) = activity.startActivity(intentHelper.shareImageIntent(activity, fileName))

}
