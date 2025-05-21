package com.glia.widgets.helper

import android.app.Activity
import android.app.PendingIntent
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.call.CallActivity
import com.glia.widgets.callvisualizer.EndScreenSharingActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.chat.Intention
import com.glia.widgets.entrywidget.EntryWidgetActivity
import com.glia.widgets.filepreview.ui.ImagePreviewActivity
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.messagecenter.MessageCenterActivity
import com.glia.widgets.push.notifications.PushClickHandlerActivity
import com.glia.widgets.survey.SurveyActivity
import com.glia.widgets.webbrowser.WebBrowserActivity
import java.io.File

internal object ExtraKeys {
    const val WEB_BROWSER_TITLE = "web_browser_title"
    const val WEB_BROWSER_URL = "web_browser_url"

    const val IMAGE_PREVIEW_IMAGE_ID = "image_preview_image_id"
    const val IMAGE_PREVIEW_IMAGE_NAME = "image_preview_image_name"
    const val IMAGE_PREVIEW_LOCAL_IMAGE_URI = "image_preview_local_image_uri"

    const val OPEN_CHAT_INTENTION = "open_chat_intention"

    const val MEDIA_TYPE = "media_type"
    const val SURVEY = "survey"
    const val IS_UPGRADE_TO_CALL = "call_screen_is_upgrade_to_call"

    const val PN_QUEUE_ID = "queue_id"
    const val PN_VISITOR_ID = "visitor_id"
}

internal interface IntentHelper {
    fun chatIntent(context: Context, intention: Intention): Intent

    fun secureMessagingWelcomeScreenIntent(context: Context): Intent

    fun callIntent(context: Context, mediaType: MediaType?, upgradeToCall: Boolean): Intent

    fun imagePreviewIntent(context: Context, attachment: AttachmentFile): Intent

    fun imagePreviewIntent(context: Context, attachment: LocalAttachment): Intent

    fun shareImageIntent(context: Context, fileName: String): Intent

    fun surveyIntent(context: Context, survey: Survey): Intent

    fun endScreenSharingIntent(context: Context): Intent

    fun webBrowserIntent(context: Context, title: LocaleString, url: String): Intent

    fun overlayPermissionIntent(context: Context): Intent

    fun openEmailIntent(uri: Uri): Intent

    fun dialerIntent(uri: Uri): Intent

    fun openUriIntent(uri: Uri): Intent

    fun openFileIntent(contentUri: Uri, fileContentType: String?): Intent

    fun entryWidgetIntent(activity: Activity): Intent

    fun pushClickHandlerPendingIntent(context: Context, queueId: String?, visitorId: String): PendingIntent
}

internal class IntentHelperImpl : IntentHelper {

    override fun chatIntent(context: Context, intention: Intention): Intent = Intent(context, ChatActivity::class.java).apply {
        putEnumExtra(ExtraKeys.OPEN_CHAT_INTENTION, intention)
        setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        setSafeFlags(context)
    }

    override fun secureMessagingWelcomeScreenIntent(context: Context): Intent = Intent(context, MessageCenterActivity::class.java)
        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        .setSafeFlags(context)

    override fun callIntent(context: Context, mediaType: MediaType?, upgradeToCall: Boolean): Intent = Intent(context, CallActivity::class.java)
        .putEnumExtra(ExtraKeys.MEDIA_TYPE, mediaType)
        .putExtra(ExtraKeys.IS_UPGRADE_TO_CALL, upgradeToCall)
        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        .setSafeFlags(context)

    override fun imagePreviewIntent(context: Context, attachment: AttachmentFile): Intent {
        return Intent(context, ImagePreviewActivity::class.java)
            .putExtra(ExtraKeys.IMAGE_PREVIEW_IMAGE_ID, attachment.id)
            .putExtra(ExtraKeys.IMAGE_PREVIEW_IMAGE_NAME, attachment.name)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    override fun imagePreviewIntent(context: Context, attachment: LocalAttachment): Intent {
        return Intent(context, ImagePreviewActivity::class.java)
            .putExtra(ExtraKeys.IMAGE_PREVIEW_LOCAL_IMAGE_URI, attachment.uri)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    override fun shareImageIntent(context: Context, fileName: String): Intent {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString(),
            fileName
        )
        val contentUri = FileProvider.getUriForFile(context, context.fileProviderAuthority, file)
        return Intent(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_STREAM, contentUri)
            .setType("image/jpeg")
    }

    override fun surveyIntent(context: Context, survey: Survey): Intent = Intent(context, SurveyActivity::class.java)
        .putExtra(ExtraKeys.SURVEY, survey as Parcelable)
        .setSafeFlags(context)

    override fun endScreenSharingIntent(context: Context): Intent = Intent(context, EndScreenSharingActivity::class.java)
        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        .setSafeFlags(context)

    override fun webBrowserIntent(context: Context, title: LocaleString, url: String): Intent = Intent(context, WebBrowserActivity::class.java)
        .putExtra(ExtraKeys.WEB_BROWSER_TITLE, title)
        .putExtra(ExtraKeys.WEB_BROWSER_URL, url)
        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

    override fun overlayPermissionIntent(context: Context): Intent = context.run {
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${packageName}".toUri()).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setSafeFlags(this)
    }

    override fun openEmailIntent(uri: Uri): Intent = Intent(Intent.ACTION_SENDTO)
        .setData("mailto:".toUri()) // This step makes sure that only email apps handle this.
        .putExtra(Intent.EXTRA_EMAIL, arrayOf(uri.schemeSpecificPart))

    override fun dialerIntent(uri: Uri): Intent = Intent(Intent.ACTION_DIAL).setData(uri)

    override fun openUriIntent(uri: Uri): Intent = Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)

    override fun openFileIntent(contentUri: Uri, fileContentType: String?): Intent = with(Intent(Intent.ACTION_VIEW)) {
        clipData = ClipData.newRawUri("", contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        setDataAndType(contentUri, fileContentType)
    }

    override fun entryWidgetIntent(activity: Activity): Intent = Intent(activity, EntryWidgetActivity::class.java)
        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

    override fun pushClickHandlerPendingIntent(context: Context, queueId: String?, visitorId: String): PendingIntent = PendingIntent.getActivity(
        context, 0, pushClickHandlerIntent(context, queueId, visitorId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun pushClickHandlerIntent(context: Context, queueId: String?, visitorId: String): Intent =
        Intent(context, PushClickHandlerActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .putExtra(ExtraKeys.PN_QUEUE_ID, queueId)
            .putExtra(ExtraKeys.PN_VISITOR_ID, visitorId)
}
