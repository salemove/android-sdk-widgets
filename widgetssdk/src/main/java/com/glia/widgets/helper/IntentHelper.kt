@file:Suppress("DEPRECATION")

package com.glia.widgets.helper

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
import com.glia.widgets.GliaWidgets
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.CallConfiguration
import com.glia.widgets.callvisualizer.EndScreenSharingActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.configuration.EngagementConfiguration
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.filepreview.ui.FilePreviewActivity
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.survey.SurveyActivity
import com.glia.widgets.webbrowser.WebBrowserActivity
import java.io.File

internal object ExtraKeys {
    const val WEB_BROWSER_TITLE = "web_browser_title"
    const val WEB_BROWSER_URL = "web_browser_url"

    const val FILE_PREVIEW_IMAGE_ID = "file_preview_image_id"
    const val FILE_PREVIEW_IMAGE_NAME = "file_preview_image_name"
}

internal interface IntentHelper {
    fun chatIntent(context: Context, queueIds: List<String>, chatType: ChatType? = null, contextId: String? = null): Intent

    fun chatIntent(context: Context, engagementConfiguration: EngagementConfiguration): Intent

    fun secureConversationsChatIntent(context: Context): Intent

    fun callIntent(context: Context, mediaType: MediaType, upgradeToCall: Boolean = true): Intent

    fun callIntent(context: Context, callConfiguration: CallConfiguration): Intent

    fun filePreviewIntent(context: Context, attachment: AttachmentFile): Intent

    fun shareImageIntent(context: Context, fileName: String): Intent

    fun surveyIntent(context: Context, survey: Survey, uiTheme: UiTheme): Intent

    fun endScreenSharingIntent(context: Context): Intent

    fun webBrowserIntent(context: Context, title: LocaleString, url: String): Intent

    fun overlayPermissionIntent(context: Context): Intent

    fun openEmailIntent(uri: Uri): Intent

    fun dialerIntent(uri: Uri): Intent

    fun openUriIntent(uri: Uri): Intent

    fun openFileIntent(contentUri: Uri, fileContentType: String): Intent
}

internal class IntentHelperImpl(private val configurationManager: GliaSdkConfigurationManager) : IntentHelper {
    private val defaultEngagementConfiguration: EngagementConfiguration
        get() = configurationManager.buildEngagementConfiguration()


    override fun chatIntent(context: Context, queueIds: List<String>, chatType: ChatType?, contextId: String?): Intent =
        Intent(context, ChatActivity::class.java).apply {
            (chatType as? Parcelable)?.also { putExtra(GliaWidgets.CHAT_TYPE, it) }
            contextId?.also { putExtra(GliaWidgets.CONTEXT_ASSET_ID, it) }
            putExtra(GliaWidgets.QUEUE_IDS, ArrayList(queueIds))
            setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            setSafeFlags(context)
        }

    override fun chatIntent(context: Context, engagementConfiguration: EngagementConfiguration): Intent = Intent(context, ChatActivity::class.java)
        .putExtra(GliaWidgets.QUEUE_IDS, engagementConfiguration.queueIds?.let { ArrayList(it) })
        .putExtra(GliaWidgets.CONTEXT_ASSET_ID, engagementConfiguration.contextAssetId)
        .putExtra(GliaWidgets.UI_THEME, engagementConfiguration.runTimeTheme)
        .putExtra(GliaWidgets.SCREEN_SHARING_MODE, engagementConfiguration.screenSharingMode)
        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        .setSafeFlags(context)

    override fun secureConversationsChatIntent(context: Context): Intent = Intent(context, ChatActivity::class.java)
        .putExtra(GliaWidgets.CHAT_TYPE, ChatType.SECURE_MESSAGING as Parcelable)
        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        .setSafeFlags(context)

    override fun callIntent(context: Context, mediaType: MediaType, upgradeToCall: Boolean): Intent = callIntent(
        context,
        CallConfiguration(
            engagementConfiguration = defaultEngagementConfiguration,
            mediaType = mediaType,
            isUpgradeToCall = upgradeToCall
        )
    )

    override fun callIntent(context: Context, callConfiguration: CallConfiguration): Intent {
        val engagementConfiguration =
            callConfiguration.engagementConfiguration ?: throw NullPointerException("WidgetsSdk Configuration can't be null")

        return Intent(context, CallActivity::class.java)
            .putExtra(GliaWidgets.QUEUE_IDS, engagementConfiguration.queueIds?.let(::ArrayList))
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, engagementConfiguration.contextAssetId)
            .putExtra(GliaWidgets.UI_THEME, engagementConfiguration.runTimeTheme)
            .putExtra(GliaWidgets.SCREEN_SHARING_MODE, engagementConfiguration.screenSharingMode)
            .putExtra(GliaWidgets.MEDIA_TYPE, callConfiguration.mediaType)
            .putExtra(GliaWidgets.IS_UPGRADE_TO_CALL, callConfiguration.isUpgradeToCall)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .setSafeFlags(context)
    }

    override fun filePreviewIntent(context: Context, attachment: AttachmentFile): Intent {
        return Intent(context, FilePreviewActivity::class.java)
            .putExtra(ExtraKeys.FILE_PREVIEW_IMAGE_ID, attachment.id)
            .putExtra(ExtraKeys.FILE_PREVIEW_IMAGE_NAME, attachment.name)
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

    override fun surveyIntent(context: Context, survey: Survey, uiTheme: UiTheme): Intent = Intent(context, SurveyActivity::class.java)
        .putExtra(GliaWidgets.UI_THEME, uiTheme)
        .putExtra(GliaWidgets.SURVEY, survey as Parcelable)
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
}
