package com.glia.widgets.helper

import android.app.Activity
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
import com.glia.widgets.chat.ChatType
import com.glia.widgets.entrywidget.EntryWidgetActivity
import com.glia.widgets.filepreview.ui.ImagePreviewActivity
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.messagecenter.MessageCenterActivity
import com.glia.widgets.survey.SurveyActivity
import com.glia.widgets.webbrowser.WebBrowserActivity
import java.io.File

internal object ExtraKeys {
    const val WEB_BROWSER_TITLE = "web_browser_title"
    const val WEB_BROWSER_URL = "web_browser_url"

    const val IMAGE_PREVIEW_IMAGE_ID = "image_preview_image_id"
    const val IMAGE_PREVIEW_IMAGE_NAME = "image_preview_image_name"

    const val CHAT_TYPE = "chat_screen_chat_type"
    const val MEDIA_TYPE = "media_type"
    const val SURVEY = "survey"
    const val IS_UPGRADE_TO_CALL = "call_screen_is_upgrade_to_call"
}

internal interface IntentHelper {
    fun chatIntent(context: Context): Intent

    fun secureMessagingChatIntent(activity: Activity): Intent

    fun secureMessagingWelcomeScreenIntent(activity: Activity): Intent

    fun callIntent(context: Context, mediaType: MediaType?, upgradeToCall: Boolean): Intent

    fun imagePreviewIntent(context: Context, attachment: AttachmentFile): Intent

    fun shareImageIntent(context: Context, fileName: String): Intent

    fun surveyIntent(context: Context, survey: Survey): Intent

    fun endScreenSharingIntent(context: Context): Intent

    fun webBrowserIntent(context: Context, title: LocaleString, url: String): Intent

    fun overlayPermissionIntent(context: Context): Intent

    fun openEmailIntent(uri: Uri): Intent

    fun dialerIntent(uri: Uri): Intent

    fun openUriIntent(uri: Uri): Intent

    fun openFileIntent(contentUri: Uri, fileContentType: String): Intent
    fun entryWidgetIntent(activity: Activity): Intent
}

internal class IntentHelperImpl : IntentHelper {

    override fun chatIntent(context: Context): Intent =
        Intent(context, ChatActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            setSafeFlags(context)
        }

    override fun secureMessagingChatIntent(activity: Activity): Intent = Intent(activity, ChatActivity::class.java)
        .putEnumExtra(ExtraKeys.CHAT_TYPE, ChatType.SECURE_MESSAGING)
        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

    override fun secureMessagingWelcomeScreenIntent(activity: Activity): Intent = Intent(activity, MessageCenterActivity::class.java)
        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

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
        .setData(Uri.parse("mailto:")) // This step makes sure that only email apps handle this.
        .putExtra(Intent.EXTRA_EMAIL, arrayOf(uri.schemeSpecificPart))

    override fun dialerIntent(uri: Uri): Intent = Intent(Intent.ACTION_DIAL).setData(uri)

    override fun openUriIntent(uri: Uri): Intent = Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE)

    override fun openFileIntent(contentUri: Uri, fileContentType: String): Intent = with(Intent(Intent.ACTION_VIEW)) {
        clipData = ClipData.newRawUri("", contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        setDataAndType(contentUri, fileContentType)
    }

    override fun entryWidgetIntent(activity: Activity): Intent = Intent(activity, EntryWidgetActivity::class.java)
        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
}
